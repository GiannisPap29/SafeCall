const BANNERS = {
  high: {
    icon:  '⚠',
    title: 'HIGH SCAM RISK DETECTED',
    body:  'This call contains multiple scam indicators. Do not share personal details, OTPs, passwords, or transfer any money.',
    classes: 'border-cyber-red/50 bg-cyber-red/10 text-cyber-red glow-red',
  },
  medium: {
    icon:  '◈',
    title: 'SUSPICIOUS ACTIVITY DETECTED',
    body:  'Scam patterns detected in this call. Proceed with caution and verify the caller\'s identity through official channels.',
    classes: 'border-cyber-orange/50 bg-cyber-orange/10 text-cyber-orange glow-orange',
  },
  low: {
    icon:  '✓',
    title: 'CALL APPEARS SAFE',
    body:  'No significant scam indicators were found. Stay vigilant and never share sensitive information over calls.',
    classes: 'border-cyber-green/30 bg-cyber-green/5 text-cyber-green',
  },
}

export default function WarningBanner({ riskLevel }) {
  if (!riskLevel) return null

  const cfg = BANNERS[riskLevel] ?? BANNERS.low

  return (
    <div className={`animate-fadeIn rounded-lg border p-4 ${cfg.classes}`}>
      <div className="flex gap-3 items-start">
        <span className={`text-xl mt-0.5 ${riskLevel === 'high' ? 'animate-blink' : ''}`}>
          {cfg.icon}
        </span>
        <div>
          <p className="font-bold text-sm tracking-widest mb-1">{cfg.title}</p>
          <p className="text-xs leading-relaxed opacity-80">{cfg.body}</p>
        </div>
      </div>
    </div>
  )
}
