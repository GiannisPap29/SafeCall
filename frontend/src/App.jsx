import { useState } from 'react'
import VoiceRecorder      from './components/VoiceRecorder'
import TranscriptionPanel from './components/TranscriptionPanel'
import RiskPanel          from './components/RiskPanel'
import WarningBanner      from './components/WarningBanner'
import LoadingSpinner     from './components/LoadingSpinner'

export default function App() {
  const [result,  setResult]  = useState(null)  // TranscribeResponse
  const [loading, setLoading] = useState(false)
  const [error,   setError]   = useState(null)

  return (
    <div className="min-h-screen flex flex-col">
      {/* Header */}
      <header className="border-b border-cyber-border bg-cyber-surface/60 backdrop-blur-sm sticky top-0 z-10">
        <div className="max-w-5xl mx-auto px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded border border-cyber-accent/50 bg-cyber-accent/10 flex items-center justify-center glow-accent">
              <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4 text-cyber-accent" viewBox="0 0 24 24" fill="currentColor">
                <path d="M6.6 10.8c1.4 2.8 3.8 5.1 6.6 6.6l2.2-2.2c.3-.3.7-.4 1-.2 1.1.4 2.3.6 3.6.6.6 0 1 .4 1 1V20c0 .6-.4 1-1 1-9.4 0-17-7.6-17-17 0-.6.4-1 1-1h3.5c.6 0 1 .4 1 1 0 1.3.2 2.5.6 3.6.1.3 0 .7-.2 1L6.6 10.8z"/>
              </svg>
            </div>
            <div>
              <h1 className="text-white font-bold text-lg tracking-wider">SAFECALL</h1>
              <p className="text-cyber-muted text-xs tracking-widest">SCAM DETECTION SYSTEM</p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-cyber-green animate-blink" />
            <span className="text-cyber-green text-xs tracking-widest">ONLINE</span>
          </div>
        </div>
      </header>

      {/* Body */}
      <main className="flex-1 max-w-5xl mx-auto w-full px-6 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">

          {/* Left column — recorder */}
          <div className="space-y-6">
            <div className="rounded-xl border border-cyber-border bg-cyber-card p-6">
              <div className="flex items-center gap-2 mb-6">
                <div className="w-1 h-4 bg-cyber-accent rounded-full" />
                <h2 className="text-white text-sm font-semibold tracking-widest uppercase">Voice Input</h2>
              </div>

              <VoiceRecorder
                onResult={setResult}
                onLoading={setLoading}
                onError={setError}
              />
            </div>

            {/* Error */}
            {error && (
              <div className="animate-fadeIn rounded-lg border border-cyber-red/40 bg-cyber-red/10 p-4 flex gap-3">
                <span className="text-cyber-red mt-0.5">✕</span>
                <p className="text-cyber-red text-sm">{error}</p>
              </div>
            )}

            {/* Loading */}
            {loading && <LoadingSpinner />}

            {/* Transcript */}
            {result && <TranscriptionPanel text={result.text} />}
          </div>

          {/* Right column — analysis */}
          <div className="space-y-6">
            {/* Placeholder when no result */}
            {!result && !loading && (
              <div className="rounded-xl border border-dashed border-cyber-border bg-cyber-card/40 p-8 flex flex-col items-center justify-center gap-3 min-h-[240px]">
                <svg xmlns="http://www.w3.org/2000/svg" className="w-10 h-10 text-cyber-muted" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <circle cx="12" cy="12" r="10" />
                  <path d="M12 8v4M12 16h.01" />
                </svg>
                <p className="text-cyber-muted text-sm text-center tracking-wide">
                  Record a call to begin scam analysis.
                </p>
              </div>
            )}

            {result && (
              <>
                <WarningBanner riskLevel={result.risk_level} />
                <RiskPanel
                  riskScore={result.risk_score}
                  riskLevel={result.risk_level}
                  detectedKeywords={result.detected_keywords}
                />
              </>
            )}
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="border-t border-cyber-border py-4 text-center">
        <p className="text-cyber-muted text-xs tracking-widest">
          SAFECALL — REAL-TIME SCAM PROTECTION
        </p>
      </footer>
    </div>
  )
}
