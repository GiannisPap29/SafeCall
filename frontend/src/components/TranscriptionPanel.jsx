export default function TranscriptionPanel({ text }) {
  if (!text) return null

  return (
    <div className="animate-fadeIn space-y-2">
      <div className="flex items-center gap-2">
        <span className="text-xs text-cyber-muted uppercase tracking-widest">Transcript</span>
        <div className="flex-1 h-px bg-cyber-border" />
      </div>

      <div className="relative rounded-lg border border-cyber-border bg-cyber-surface p-4 overflow-hidden">
        {/* Subtle scanline effect */}
        <div
          className="pointer-events-none absolute inset-x-0 h-8 opacity-5"
          style={{
            background: 'linear-gradient(to bottom, transparent, rgba(0,212,255,0.4), transparent)',
            animation: 'scanline 3s linear infinite',
          }}
        />

        <p className="relative text-sm leading-relaxed text-white/90 font-mono whitespace-pre-wrap break-words">
          {text}
        </p>
      </div>
    </div>
  )
}
