import type { CSSProperties } from "react";

export interface ConfettiBurstEntry {
  id: number;
}

type ParticleStyle = CSSProperties & { "--dx": string; "--dy": string; "--rot": string };

const COLORS = ["#fbbf24", "#f87171", "#34d399", "#60a5fa", "#c084fc", "#fb923c"];
const PARTICLES_PER_BURST = 18;

function particleStyle(index: number): ParticleStyle {
  const angle = (360 / PARTICLES_PER_BURST) * index + Math.random() * 12;
  const distance = 60 + Math.random() * 90;
  const rad = (angle * Math.PI) / 180;
  return {
    background: COLORS[index % COLORS.length],
    "--dx": `${Math.cos(rad) * distance}px`,
    "--dy": `${Math.sin(rad) * distance}px`,
    "--rot": `${Math.round(Math.random() * 480 - 240)}deg`,
  };
}

export function ConfettiBurstLayer({ entries, onEntryDone }: { entries: ConfettiBurstEntry[]; onEntryDone: (id: number) => void }) {
  if (entries.length === 0) return null;

  return (
    <div className="confetti-burst-layer" aria-hidden="true">
      {entries.map((entry) => (
        <div key={entry.id} className="confetti-burst" onAnimationEnd={() => onEntryDone(entry.id)}>
          {Array.from({ length: PARTICLES_PER_BURST }, (_, index) => (
            <span key={index} className="confetti-particle" style={particleStyle(index)} />
          ))}
        </div>
      ))}
    </div>
  );
}
