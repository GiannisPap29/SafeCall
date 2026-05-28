const LEVEL_CONFIG = {
  low: {
    label:    'LOW RISK',
    color:    'text-cyber-green',
    bar:      'bg-cyber-green',
    glow:     'glow-green',
    border:   'border-cyber-green/30',
    bg:       'bg-cyber-green/5',
    tagBg:    'bg-cyber-green/10 text-cyber-green border-cyber-green/30',
  },
  medium: {
    label:    'MEDIUM RISK',
    color:    'text-cyber-yellow',
    bar:      'bg-cyber-yellow',
    glow:     'glow-orange',
    border:   'border-cyber-yellow/30',
    bg:       'bg-cyber-yellow/5',
    tagBg:    'bg-cyber-yellow/10 text-cyber-yellow border-cyber-yellow/30',
  },
  high: {
    label:    'HIGH RISK',
    color:    'text-cyber-red',
    bar:      'bg-cyber-red',
    glow:     'glow-red',
    border:   'border-cyber-red/30',
    bg:       'bg-cyber-red/5',
    tagBg:    'bg-cyber-red/10 text-cyber-red border-cyber-red/30',
  },
}

export default function RiskPanel({ riskScore, riskLevel, detectedKeywords }) {
  if (riskScore === undefined || riskScore === null) return null

  const cfg = LEVEL_CONFIG[riskLevel] ?? LEVEL_CONFIG.low

  return (
    <div className={`animate-fadeIn rounded-lg border p-4 space-y-4 ${cfg.border} ${cfg.bg} ${cfg.glow}`}>
      {/* Header */}
      <div className="flex items-center justify-between">
        <span className="text-xs text-cyber-muted uppercase tracking-widest">Risk Analysis</span>
        <span className={`text-xs font-bold tracking-widest ${cfg.color} ${riskLevel === 'high' ? 'animate-blink' : ''}`}>
          {cfg.label}
        </span>
      </div>

      {/* Score + bar */}
      <div className="space-y-2">
        <div className="flex items-baseline justify-between">
          <span className={`text-4xl font-bold tabular-nums ${cfg.color}`}>{riskScore}</span>
          <span className="text-cyber-muted text-xs">/100</span>
        </div>

        <div className="h-2 rounded-full bg-cyber-border overflow-hidden">
          <div
            className={`h-full rounded-full transition-all duration-700 ease-out ${cfg.bar}`}
            style={{ width: `${riskScore}%` }}
          />
        </div>

        <div className="flex justify-between text-[10px] text-cyber-muted">
          <span>LOW</span>
          <span>MEDIUM</span>
          <span>HIGH</span>
        </div>
      </div>

      {/* Detected keywords */}
      {detectedKeywords && detectedKeywords.length > 0 && (
        <div className="space-y-2">
          <p className="text-xs text-cyber-muted uppercase tracking-widest">Detected Signals</p>
          <div className="flex flex-wrap gap-2">
            {detectedKeywords.map((kw) => (
              <span
                key={kw}
                className={`text-xs px-2 py-1 rounded border font-mono ${cfg.tagBg}`}
              >
                {kw}
              </span>
            ))}
          </div>
        </div>
      )}

      {detectedKeywords && detectedKeywords.length === 0 && (
        <p className="text-xs text-cyber-muted">No scam signals detected.</p>
      )}
    </div>
  )
}
