import { useTranslation } from "react-i18next";
import type { PerkType } from "../types/pixelWars";

const PERK_TYPES: PerkType[] = ["BOMB", "CAFE", "RAFALE", "SHIELD", "FRENZY", "FORTRESS"];

function ComboDiagram({ cols, rows, filled, label }: { cols: number; rows: number; filled: (x: number, y: number) => boolean; label: string }) {
  const cells = [];
  for (let y = 0; y < rows; y++) {
    for (let x = 0; x < cols; x++) {
      cells.push(
        <div key={`${x}-${y}`} className={`combo-diagram-cell ${filled(x, y) ? "combo-diagram-cell-on" : ""}`} />
      );
    }
  }
  return (
    <div className="combo-diagram">
      <div className="combo-diagram-grid" style={{ gridTemplateColumns: `repeat(${cols}, 1fr)` }}>
        {cells}
      </div>
      <span className="combo-diagram-label">{label}</span>
    </div>
  );
}

export function RulesPanel() {
  const { t } = useTranslation();

  return (
    <div className="rules-panel">
      <section>
        <h3>{t("rules.territory.title")}</h3>
        <p>{t("rules.territory.text")}</p>
      </section>

      <section>
        <h3>{t("rules.combos.title")}</h3>
        <p>{t("rules.combos.text")}</p>
        <div className="combo-diagrams">
          <ComboDiagram cols={5} rows={1} filled={() => true} label={t("rules.combos.line")} />
          <ComboDiagram cols={3} rows={3} filled={() => true} label={t("rules.combos.square")} />
        </div>
      </section>

      <section>
        <h3>{t("rules.capture.title")}</h3>
        <p>{t("rules.capture.text")}</p>
      </section>

      <section>
        <h3>{t("rules.perksTitle")}</h3>
        <ul>
          {PERK_TYPES.map((type) => (
            <li key={type}>
              <strong>{t(`perks.names.${type}`)}</strong> — {t(`perks.descriptions.${type}`)}
            </li>
          ))}
        </ul>
      </section>

      <section>
        <h3>{t("rules.appearance.title")}</h3>
        <p>{t("rules.appearance.text")}</p>
      </section>
    </div>
  );
}
