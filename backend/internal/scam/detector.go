package scam

import "strings"

type Result struct {
	RiskScore        int
	RiskLevel        string
	DetectedKeywords []string
}

// weighted scam signals; higher weight = stronger indicator
var signalWeights = map[string]int{
	"otp":               35,
	"verification code": 35,
	"transfer money":    30,
	"bank account":      25,
	"remote access":     40,
	"urgent":            15,
	"crypto":            20,
	"password":          20,
}

func Analyze(text string) Result {
	lower := strings.ToLower(text)

	var detected []string
	score := 0

	for keyword, weight := range signalWeights {
		if strings.Contains(lower, keyword) {
			detected = append(detected, keyword)
			score += weight
		}
	}

	if score > 100 {
		score = 100
	}

	return Result{
		RiskScore:        score,
		RiskLevel:        riskLevel(score),
		DetectedKeywords: detected,
	}
}

func riskLevel(score int) string {
	switch {
	case score <= 30:
		return "low"
	case score <= 70:
		return "medium"
	default:
		return "high"
	}
}
