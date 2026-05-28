/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        mono: ['JetBrains Mono', 'Fira Code', 'Consolas', 'monospace'],
      },
      colors: {
        cyber: {
          bg:     '#080d1a',
          surface:'#0d1526',
          card:   '#111d35',
          border: '#1b2e52',
          accent: '#00d4ff',
          dim:    '#0a9ab8',
          text:   '#8bafd4',
          muted:  '#3d5a7a',
          red:    '#ff2d55',
          orange: '#ff6b35',
          yellow: '#ffc300',
          green:  '#00ff9d',
          purple: '#bf5af2',
        },
      },
      keyframes: {
        ripple: {
          '0%':    { transform: 'scale(1)',   opacity: '0.6' },
          '100%':  { transform: 'scale(2.2)', opacity: '0' },
        },
        scanline: {
          '0%':   { transform: 'translateY(-100%)' },
          '100%': { transform: 'translateY(400%)' },
        },
        blink: {
          '0%, 100%': { opacity: '1' },
          '50%':      { opacity: '0.2' },
        },
        fadeIn: {
          from: { opacity: '0', transform: 'translateY(8px)' },
          to:   { opacity: '1', transform: 'translateY(0)' },
        },
      },
      animation: {
        ripple:   'ripple 1.5s ease-out infinite',
        scanline: 'scanline 3s linear infinite',
        blink:    'blink 1s ease-in-out infinite',
        fadeIn:   'fadeIn 0.4s ease-out',
      },
    },
  },
  plugins: [],
}
