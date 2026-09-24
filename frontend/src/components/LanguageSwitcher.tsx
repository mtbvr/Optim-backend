import { useTranslation } from "react-i18next";
import { setLanguage, SUPPORTED_LANGUAGES } from "../i18n";
import type { SupportedLanguage } from "../i18n";

export function LanguageSwitcher() {
  const { i18n } = useTranslation();
  const current = (i18n.language?.slice(0, 2) as SupportedLanguage) || "fr";

  return (
    <div className="lang-switcher" role="group" aria-label={i18n.t("nav.language")}>
      {SUPPORTED_LANGUAGES.map((lang) => (
        <button
          key={lang}
          type="button"
          className={`lang-switcher-btn ${current === lang ? "lang-switcher-btn-active" : ""}`}
          onClick={() => setLanguage(lang)}
        >
          {lang.toUpperCase()}
        </button>
      ))}
    </div>
  );
}
