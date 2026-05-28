package handlers

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"path/filepath"

	"safecall/internal/models"
	"safecall/internal/openai"
	"safecall/internal/scam"
)

type TranscribeHandler struct {
	openai *openai.Client
}

func NewTranscribeHandler(client *openai.Client) *TranscribeHandler {
	return &TranscribeHandler{openai: client}
}

func (h *TranscribeHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		writeError(w, "method not allowed", http.StatusMethodNotAllowed)
		return
	}

	if err := r.ParseMultipartForm(32 << 20); err != nil {
		writeError(w, "failed to parse multipart form", http.StatusBadRequest)
		return
	}

	file, header, err := r.FormFile("audio")
	if err != nil {
		writeError(w, "field 'audio' is required", http.StatusBadRequest)
		return
	}
	defer file.Close()

	ext := filepath.Ext(header.Filename)
	if ext == "" {
		ext = ".webm"
	}

	tmp, err := os.CreateTemp("", fmt.Sprintf("safecall-*%s", ext))
	if err != nil {
		log.Printf("create temp file: %v", err)
		writeError(w, "internal server error", http.StatusInternalServerError)
		return
	}
	defer os.Remove(tmp.Name())
	defer tmp.Close()

	if _, err = io.Copy(tmp, file); err != nil {
		log.Printf("write temp file: %v", err)
		writeError(w, "failed to save audio", http.StatusInternalServerError)
		return
	}
	tmp.Close()

	text, err := h.openai.Transcribe(tmp.Name())
	if err != nil {
		log.Printf("transcription error: %v", err)
		writeError(w, fmt.Sprintf("transcription failed: %v", err), http.StatusInternalServerError)
		return
	}

	analysis := scam.Analyze(text)

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(models.TranscribeResponse{
		Text:             text,
		RiskScore:        analysis.RiskScore,
		RiskLevel:        analysis.RiskLevel,
		DetectedKeywords: analysis.DetectedKeywords,
	})
}

func writeError(w http.ResponseWriter, msg string, status int) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	json.NewEncoder(w).Encode(models.ErrorResponse{Error: msg})
}
