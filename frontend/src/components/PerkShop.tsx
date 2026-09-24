import { useTranslation } from "react-i18next";
import type { PerkDefinition, PerkType, PlayerState } from "../types/pixelWars";

const SPEED_BUFF_PERKS: PerkType[] = ["CAFE", "RAFALE"];
const MAX_TIER = 3;
const PURCHASES_PER_TIER = 5;

export function PerkShop({
  perks,
  me,
  purchasingType,
  speedBuffActive,
  onPurchase,
}: {
  perks: PerkDefinition[];
  me: PlayerState;
  purchasingType: PerkType | null;
  speedBuffActive: boolean;
  onPurchase: (type: PerkType) => void;
}) {
  const { t } = useTranslation();

  return (
    <div className="perk-shop">
      <p className="perk-shop-points">{t("perks.points", { points: me.points })}</p>

      <ul className="perk-list">
        {perks.map((perk) => {
          const blockedBySpeedBuff = speedBuffActive && SPEED_BUFF_PERKS.includes(perk.type);
          const affordable = me.points >= perk.cost;
          const progressInTier = perk.purchaseCount % PURCHASES_PER_TIER;
          const tierProgressPct = perk.tier >= MAX_TIER ? 100 : (progressInTier / PURCHASES_PER_TIER) * 100;
          return (
            <li key={perk.type} className="perk-card">
              <div className="perk-card-header">
                <span className="perk-name">{t(`perks.names.${perk.type}`)}</span>
                <span className="perk-cost">{perk.cost} {t("common.points")}</span>
              </div>
              <p className="perk-description">{t(`perks.descriptions.${perk.type}`)}</p>
              <div className="perk-tier-row">
                <span className="perk-tier-label">{t("perks.tier", { tier: perk.tier })}</span>
                <div className="perk-tier-bar">
                  <div className="perk-tier-bar-fill" style={{ width: `${tierProgressPct}%` }} />
                </div>
              </div>
              <button
                type="button"
                className="btn btn-shop perk-buy-btn"
                disabled={!affordable || blockedBySpeedBuff || purchasingType === perk.type}
                onClick={() => onPurchase(perk.type)}
              >
                {purchasingType === perk.type
                  ? t("perks.buying")
                  : blockedBySpeedBuff
                    ? t("perks.alreadyActive")
                    : t("perks.buy")}
              </button>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
