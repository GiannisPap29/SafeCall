package models

type TranscribeResponse struct {
	Text             string   `json:"text"`
	RiskScore        int      `json:"risk_score"`
	RiskLevel        string   `json:"risk_level"`
	DetectedKeywords []string `json:"detected_keywords"`
}

type ErrorResponse struct {
	Error string `json:"error"`
}
