export default function LoadingSpinner() {
  return (
    <div className="flex flex-col items-center gap-3 py-4 animate-fadeIn">
      <div className="relative w-10 h-10">
        <div className="absolute inset-0 rounded-full border-2 border-cyber-border" />
        <div className="absolute inset-0 rounded-full border-2 border-transparent border-t-cyber-accent animate-spin" />
      </div>
      <p className="text-cyber-muted text-xs tracking-widest uppercase animate-blink">
        Analysing call...
      </p>
    </div>
  )
}
