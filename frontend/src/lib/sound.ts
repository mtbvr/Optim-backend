// Effets sonores synthetises via Web Audio (oscillateurs + enveloppe de gain) : pas de fichiers
// audio a sourcer/heberger/licencier pour un simple set de bips/impacts courts.

const MUTE_KEY = "pixelwars-muted";
let audioContext: AudioContext | null = null;

function getContext(): AudioContext | null {
  if (typeof window === "undefined") return null;
  const AudioContextCtor =
    window.AudioContext ?? (window as unknown as { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
  if (!AudioContextCtor) return null;
  if (!audioContext) {
    audioContext = new AudioContextCtor();
  }
  // Les navigateurs suspendent le contexte tant qu'aucun geste utilisateur n'a eu lieu ;
  // chaque appel tente de le relancer (no-op si deja actif).
  if (audioContext.state === "suspended") {
    audioContext.resume().catch(() => {
      /* ignore : sera retente au prochain son */
    });
  }
  return audioContext;
}

export function isMuted(): boolean {
  try {
    return localStorage.getItem(MUTE_KEY) === "1";
  } catch {
    return false;
  }
}

export function setMuted(muted: boolean): void {
  try {
    localStorage.setItem(MUTE_KEY, muted ? "1" : "0");
  } catch {
    /* preference non critique */
  }
}

function tone(freq: number, duration: number, options: { type?: OscillatorType; gain?: number; delay?: number } = {}) {
  if (isMuted()) return;
  const ctx = getContext();
  if (!ctx) return;
  const { type = "sine", gain = 0.15, delay = 0 } = options;
  const start = ctx.currentTime + delay;

  const oscillator = ctx.createOscillator();
  const gainNode = ctx.createGain();
  oscillator.type = type;
  oscillator.frequency.setValueAtTime(freq, start);
  gainNode.gain.setValueAtTime(0, start);
  gainNode.gain.linearRampToValueAtTime(gain, start + 0.01);
  gainNode.gain.exponentialRampToValueAtTime(0.0001, start + duration);

  oscillator.connect(gainNode);
  gainNode.connect(ctx.destination);
  oscillator.start(start);
  oscillator.stop(start + duration + 0.02);
}

function noiseBurst(duration: number, gain = 0.2) {
  if (isMuted()) return;
  const ctx = getContext();
  if (!ctx) return;

  const bufferSize = Math.floor(ctx.sampleRate * duration);
  const buffer = ctx.createBuffer(1, bufferSize, ctx.sampleRate);
  const data = buffer.getChannelData(0);
  for (let i = 0; i < bufferSize; i++) {
    data[i] = (Math.random() * 2 - 1) * (1 - i / bufferSize);
  }

  const source = ctx.createBufferSource();
  source.buffer = buffer;
  const gainNode = ctx.createGain();
  gainNode.gain.setValueAtTime(gain, ctx.currentTime);
  source.connect(gainNode);
  gainNode.connect(ctx.destination);
  source.start();
}

export const sound = {
  place() {
    tone(440, 0.08, { type: "square", gain: 0.08 });
  },
  combo() {
    tone(523.25, 0.12, { gain: 0.12 });
    tone(659.25, 0.12, { gain: 0.12, delay: 0.08 });
    tone(783.99, 0.16, { gain: 0.12, delay: 0.16 });
  },
  capture() {
    tone(300, 0.2, { type: "sawtooth", gain: 0.1 });
    tone(180, 0.25, { type: "sawtooth", gain: 0.1, delay: 0.05 });
  },
  bomb() {
    tone(80, 0.35, { type: "triangle", gain: 0.22 });
    noiseBurst(0.3, 0.18);
  },
  purchase() {
    tone(880, 0.1, { gain: 0.1 });
    tone(1174.66, 0.12, { gain: 0.1, delay: 0.07 });
  },
  cooldownReady() {
    tone(660, 0.1, { gain: 0.09 });
  },
  bonus() {
    tone(1046.5, 0.1, { gain: 0.14 });
    tone(1318.5, 0.14, { gain: 0.14, delay: 0.06 });
  },
  achievement() {
    tone(523.25, 0.1, { gain: 0.13 });
    tone(659.25, 0.1, { gain: 0.13, delay: 0.09 });
    tone(783.99, 0.1, { gain: 0.13, delay: 0.18 });
    tone(1046.5, 0.22, { gain: 0.15, delay: 0.27 });
  },
  emote() {
    tone(500, 0.06, { type: "square", gain: 0.06 });
  },
  contribute() {
    tone(660, 0.07, { type: "triangle", gain: 0.1 });
    tone(880, 0.09, { type: "triangle", gain: 0.1, delay: 0.05 });
  },
  teamRush() {
    tone(220, 0.18, { type: "sawtooth", gain: 0.16 });
    tone(440, 0.16, { type: "sawtooth", gain: 0.14, delay: 0.1 });
    tone(660, 0.2, { type: "sawtooth", gain: 0.16, delay: 0.2 });
    noiseBurst(0.25, 0.14);
  },
};
