import { useTranslation } from "react-i18next";
import type { Leaderboard as LeaderboardData, Team } from "../types/pixelWars";

export function Leaderboard({
  leaderboard,
  teamColors,
  meUserId,
}: {
  leaderboard: LeaderboardData;
  teamColors: Record<Team, string>;
  meUserId?: string;
}) {
  const { t } = useTranslation();
  const redTotal = leaderboard.teamTotals.RED ?? 0;
  const blueTotal = leaderboard.teamTotals.BLUE ?? 0;
  const total = redTotal + blueTotal;
  const redShare = total === 0 ? 50 : (redTotal / total) * 100;

  return (
    <div className="leaderboard-panel">
      <h2>{t("leaderboard.title")}</h2>

      <div className="team-score-bar" aria-hidden="true">
        <div className="team-score-bar-fill" style={{ width: `${redShare}%`, background: teamColors.RED }} />
      </div>
      <div className="team-score-labels">
        <span style={{ color: teamColors.RED }}>{t("pixelWars.team.RED")} — {redTotal}</span>
        <span style={{ color: teamColors.BLUE }}>{t("pixelWars.team.BLUE")} — {blueTotal}</span>
      </div>

      <ol className="leaderboard-list">
        {leaderboard.topPlayers.map((entry, index) => (
          <li
            key={entry.userId}
            className={`leaderboard-entry ${entry.userId === meUserId ? "leaderboard-entry-me" : ""}`}
          >
            <span className="leaderboard-rank">{index + 1}</span>
            <span className="leaderboard-dot" style={{ background: teamColors[entry.team] }} />
            <span className="leaderboard-name">{entry.fullName}</span>
            <span className="leaderboard-count">{entry.pixelCount}</span>
          </li>
        ))}
        {leaderboard.topPlayers.length === 0 && (
          <li className="leaderboard-empty">{t("leaderboard.empty")}</li>
        )}
      </ol>
    </div>
  );
}
