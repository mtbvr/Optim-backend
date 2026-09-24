import { useState } from "react";
import { useTranslation } from "react-i18next";
import type { Team, TeamPools } from "../types/pixelWars";

export function TeamPoolBar({
  team,
  teamPools,
  myPoints,
  teamColor,
  busy,
  onContribute,
}: {
  team: Team;
  teamPools: TeamPools;
  myPoints: number;
  teamColor: string;
  busy: boolean;
  onContribute: (amount: number) => void;
}) {
  const { t } = useTranslation();
  const [amount, setAmount] = useState(10);
  const current = teamPools.pools[team] ?? 0;
  const progress = Math.min(100, (current / teamPools.threshold) * 100);

  return (
    <div className="team-pool-bar-wrap">
      <div className="team-pool-bar-header">
        <span>{t("teamPool.title")}</span>
        <span>{current} / {teamPools.threshold}</span>
      </div>
      <div className="team-pool-bar">
        <div className="team-pool-bar-fill" style={{ width: `${progress}%`, background: teamColor }} />
      </div>
      <p className="team-pool-reward">{t("teamPool.reward")}</p>
      <div className="team-pool-contribute">
        <input
          type="number"
          min={1}
          max={Math.max(1, myPoints)}
          value={amount}
          onChange={(event) => setAmount(Math.max(1, Number(event.target.value) || 1))}
        />
        <button
          type="button"
          className="btn btn-shop btn-sm"
          disabled={busy || myPoints < 1}
          onClick={() => onContribute(Math.min(amount, myPoints))}
        >
          {t("teamPool.contribute")}
        </button>
      </div>
    </div>
  );
}
