export interface AchievementToastEntry {
  id: number;
  title: string;
  description: string;
}

export function AchievementToastLayer({
  entries,
  onEntryDone,
}: {
  entries: AchievementToastEntry[];
  onEntryDone: (id: number) => void;
}) {
  if (entries.length === 0) return null;

  return (
    <div className="hud-achievement-stack">
      {entries.map((entry) => (
        <div key={entry.id} className="achievement-toast" onAnimationEnd={() => onEntryDone(entry.id)}>
          <span className="achievement-toast-icon">🏆</span>
          <div>
            <p className="achievement-toast-title">{entry.title}</p>
            <p className="achievement-toast-description">{entry.description}</p>
          </div>
        </div>
      ))}
    </div>
  );
}
