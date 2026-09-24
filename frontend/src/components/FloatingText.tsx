export interface FloatingTextEntry {
  id: number;
  x: number;
  y: number;
  text: string;
  tone: "combo" | "capture" | "bonus";
}

export function FloatingTextLayer({
  entries,
  onEntryDone,
}: {
  entries: FloatingTextEntry[];
  onEntryDone: (id: number) => void;
}) {
  return (
    <>
      {entries.map((entry) => (
        <div
          key={entry.id}
          className={`floating-text floating-text-${entry.tone}`}
          style={{ left: entry.x, top: entry.y }}
          onAnimationEnd={() => onEntryDone(entry.id)}
        >
          {entry.text}
        </div>
      ))}
    </>
  );
}
