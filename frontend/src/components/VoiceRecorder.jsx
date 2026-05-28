import { useRef, useState, useCallback } from 'react'

const BACKEND_URL = 'http://localhost:8080'

export default function VoiceRecorder({ onResult, onLoading, onError }) {
  const mediaRecorderRef = useRef(null)
  const chunksRef        = useRef([])
  const streamRef        = useRef(null)

  const [recording, setRecording]   = useState(false)
  const [audioUrl,  setAudioUrl]    = useState(null)
  const [duration,  setDuration]    = useState(0)
  const timerRef = useRef(null)

  const startRecording = useCallback(async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
      streamRef.current = stream

      const mediaRecorder = new MediaRecorder(stream, { mimeType: 'audio/webm' })
      mediaRecorderRef.current = mediaRecorder
      chunksRef.current = []

      mediaRecorder.ondataavailable = (e) => {
        if (e.data.size > 0) chunksRef.current.push(e.data)
      }

      mediaRecorder.onstop = async () => {
        clearInterval(timerRef.current)
        stream.getTracks().forEach((t) => t.stop())

        const blob = new Blob(chunksRef.current, { type: 'audio/webm' })
        const url  = URL.createObjectURL(blob)
        setAudioUrl(url)
        await uploadAudio(blob)
      }

      mediaRecorder.start(250) // collect data every 250ms
      setRecording(true)
      setAudioUrl(null)
      setDuration(0)
      onError(null)
      onResult(null)

      timerRef.current = setInterval(() => setDuration((d) => d + 1), 1000)
    } catch (err) {
      onError('Microphone access denied. Please allow microphone access and try again.')
    }
  }, [onError, onResult])

  const stopRecording = useCallback(() => {
    if (mediaRecorderRef.current && recording) {
      mediaRecorderRef.current.stop()
      setRecording(false)
    }
  }, [recording])

  const uploadAudio = async (blob) => {
    onLoading(true)
    try {
      const form = new FormData()
      form.append('audio', blob, 'recording.webm')

      const res = await fetch(`${BACKEND_URL}/transcribe`, {
        method: 'POST',
        body: form,
      })

      const data = await res.json()
      if (!res.ok) throw new Error(data.error || `Server error ${res.status}`)
      onResult(data)
    } catch (err) {
      onError(err.message)
    } finally {
      onLoading(false)
    }
  }

  const formatDuration = (s) =>
    `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`

  return (
    <div className="flex flex-col items-center gap-6">
      {/* Main mic button */}
      <div className="relative flex items-center justify-center">
        {recording && (
          <>
            <span className="absolute w-40 h-40 rounded-full bg-cyber-red opacity-20 animate-ripple" />
            <span className="absolute w-40 h-40 rounded-full bg-cyber-red opacity-10 animate-ripple [animation-delay:0.5s]" />
          </>
        )}

        <button
          onClick={recording ? stopRecording : startRecording}
          className={[
            'relative z-10 w-32 h-32 rounded-full flex items-center justify-center transition-all duration-300',
            'border-2 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-cyber-bg',
            recording
              ? 'bg-cyber-red/20 border-cyber-red text-cyber-red glow-red focus:ring-cyber-red hover:bg-cyber-red/30'
              : 'bg-cyber-accent/10 border-cyber-accent text-cyber-accent glow-accent focus:ring-cyber-accent hover:bg-cyber-accent/20',
          ].join(' ')}
          aria-label={recording ? 'Stop recording' : 'Start recording'}
        >
          {recording ? (
            /* Stop icon */
            <svg xmlns="http://www.w3.org/2000/svg" className="w-12 h-12" viewBox="0 0 24 24" fill="currentColor">
              <rect x="6" y="6" width="12" height="12" rx="1" />
            </svg>
          ) : (
            /* Microphone icon */
            <svg xmlns="http://www.w3.org/2000/svg" className="w-12 h-12" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 1a4 4 0 0 1 4 4v6a4 4 0 0 1-8 0V5a4 4 0 0 1 4-4zm0 2a2 2 0 0 0-2 2v6a2 2 0 1 0 4 0V5a2 2 0 0 0-2-2zm-7 8a7 7 0 0 0 14 0h2a9 9 0 0 1-8 8.94V22h-2v-2.06A9 9 0 0 1 3 11h2z" />
            </svg>
          )}
        </button>
      </div>

      {/* Status label */}
      <div className="text-center space-y-1">
        {recording ? (
          <div className="flex items-center gap-2 justify-center">
            <span className="w-2 h-2 rounded-full bg-cyber-red animate-blink" />
            <span className="text-cyber-red font-semibold tracking-widest text-sm uppercase">
              Recording — {formatDuration(duration)}
            </span>
          </div>
        ) : (
          <p className="text-cyber-muted text-sm tracking-widest uppercase">
            {audioUrl ? 'Recording complete' : 'Press to record'}
          </p>
        )}
      </div>

      {/* Playback */}
      {audioUrl && !recording && (
        <div className="w-full animate-fadeIn">
          <p className="text-cyber-muted text-xs uppercase tracking-widest mb-2">Playback</p>
          <audio
            controls
            src={audioUrl}
            className="w-full h-8 rounded"
            style={{ accentColor: '#00d4ff' }}
          />
        </div>
      )}
    </div>
  )
}
