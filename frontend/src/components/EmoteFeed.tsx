import type { Team } from "../types/pixelWars";

export interface EmoteFeedEntry {
  id: number;
  fullName: string;
  team: Team;
  emoji: string;
}

export function EmoteFeed({ entries, teamColors }: { entries: EmoteFeedEntry[]; teamColors: Record<Team, string> }) {
  if (entries.length === 0) return null;

  return (
    <div className="hud-emote-feed">
      {entries.map((entry) => (
        <div key={entry.id} className="emote-feed-entry">
          <span className="emote-feed-name" style={{ color: teamColors[entry.team] }}>
            {entry.fullName}
          </span>
          <span>{entry.emoji}</span>
        </div>
      ))}
    </div>
  );
}
