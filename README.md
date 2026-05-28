# SafeCall — Real-Time Scam Detection

Voice-to-text transcription with AI-powered scam risk scoring.

## Stack

- **Frontend** — React 18 + Vite + TailwindCSS
- **Backend** — Go 1.21, `gorilla/mux`, `rs/cors`
- **AI** — OpenAI `gpt-4o-transcribe`

## Project Structure

```
SafeCall/
├── backend/
│   ├── cmd/server/main.go          # Entry point
│   ├── internal/
│   │   ├── handlers/transcribe.go  # POST /transcribe
│   │   ├── openai/client.go        # OpenAI transcription client
│   │   ├── scam/detector.go        # Keyword-weighted risk scoring
│   │   └── models/response.go      # Shared types
│   ├── go.mod
│   └── .env.example
└── frontend/
    ├── src/
    │   ├── App.jsx
    │   └── components/
    │       ├── VoiceRecorder.jsx    # MediaRecorder + auto-upload
    │       ├── TranscriptionPanel.jsx
    │       ├── RiskPanel.jsx        # Score bar + keyword tags
    │       ├── WarningBanner.jsx    # Contextual alert
    │       └── LoadingSpinner.jsx
    ├── index.html
    ├── package.json
    └── vite.config.js
```

## Setup

### 1. Backend

```bash
cd backend
cp .env.example .env
# Edit .env — set OPENAI_API_KEY
go mod tidy
go run ./cmd/server
# Listening on :8080
```

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
# Open http://localhost:5173
```

## API

### `POST /transcribe`

Upload a `.webm` audio file as `multipart/form-data`.

**Request**
```
Content-Type: multipart/form-data
field: audio (file)
```

**Response**
```json
{
  "text": "Please send me the OTP code urgently",
  "risk_score": 50,
  "risk_level": "medium",
  "detected_keywords": ["otp", "urgent"]
}
```

**Risk levels**

| Score  | Level  |
|--------|--------|
| 0–30   | low    |
| 31–70  | medium |
| 71–100 | high   |

## Scam Keyword Weights

| Keyword           | Weight |
|-------------------|--------|
| remote access     | 40     |
| otp               | 35     |
| verification code | 35     |
| transfer money    | 30     |
| bank account      | 25     |
| password          | 20     |
| crypto            | 20     |
| urgent            | 15     |
