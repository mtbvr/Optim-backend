import { useTranslation } from "react-i18next";
import type { SkinDefinition, SkinId, PlayerState } from "../types/pixelWars";

export function SkinShop({
  skins,
  owned,
  selected,
  me,
  busySkinId,
  onPurchase,
  onSelect,
}: {
  skins: SkinDefinition[];
  owned: SkinId[];
  selected: SkinId | null;
  me: PlayerState;
  busySkinId: SkinId | null;
  onPurchase: (skinId: SkinId) => void;
  onSelect: (skinId: SkinId | null) => void;
}) {
  const { t } = useTranslation();

  return (
    <div className="perk-shop">
      <p className="perk-shop-points">{t("skins.intro", { points: me.points })}</p>

      <ul className="perk-list">
        <li className="perk-card skin-card">
          <div className="perk-card-header">
            <span className="perk-name">
              <span className="skin-swatch skin-swatch-default" />
              {t("skins.defaultOption")}
            </span>
          </div>
          <button
            type="button"
            className="btn btn-cosmetic perk-buy-btn"
            disabled={selected === null}
            onClick={() => onSelect(null)}
          >
            {selected === null ? t("skins.active") : t("skins.choose")}
          </button>
        </li>

        {skins.map((skin) => {
          const isOwned = owned.includes(skin.id);
          const isSelected = selected === skin.id;
          const affordable = me.points >= skin.cost;
          const locked = !isOwned && skin.requiresAchievement !== null;
          return (
            <li key={skin.id} className="perk-card skin-card">
              <div className="perk-card-header">
                <span className="perk-name">
                  <span
                    className={`skin-swatch skin-swatch-${skin.kind.toLowerCase()}`}
                    style={{ borderColor: skin.hexColor, background: skin.kind === "COLOR" ? skin.hexColor : undefined }}
                  />
                  {t(`skins.names.${skin.id}`)}
                  <span className="skin-kind-tag">{t(`skins.kind.${skin.kind}`)}</span>
                </span>
                {!isOwned && !locked && <span className="perk-cost">{skin.cost} {t("common.points")}</span>}
              </div>
              {locked ? (
                <p className="skin-locked-hint">
                  {t("skins.locked", { achievement: t(`achievements.names.${skin.requiresAchievement}`) })}
                </p>
              ) : isOwned ? (
                <button
                  type="button"
                  className="btn btn-cosmetic perk-buy-btn"
                  disabled={isSelected}
                  onClick={() => onSelect(skin.id)}
                >
                  {isSelected ? t("skins.active") : t("skins.choose")}
                </button>
              ) : (
                <button
                  type="button"
                  className="btn btn-cosmetic perk-buy-btn"
                  disabled={!affordable || busySkinId === skin.id}
                  onClick={() => onPurchase(skin.id)}
                >
                  {busySkinId === skin.id ? t("skins.buying") : t("skins.buy")}
                </button>
              )}
            </li>
          );
        })}
      </ul>
    </div>
  );
}
