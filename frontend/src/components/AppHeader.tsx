import type { ReactNode } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../hooks/useAuth";
import { LanguageSwitcher } from "./LanguageSwitcher";

export function AppHeader({ actions }: { actions?: ReactNode }) {
  const { t } = useTranslation();
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  async function handleLogout() {
    await logout();
    navigate("/login", { replace: true });
  }

  return (
    <header className="app-header">
      <Link to="/" className="brand">
        <span className="brand-mark" aria-hidden="true" />
        {t("nav.brand")}
      </Link>

      <div className="app-header-actions">
        {actions}
        <LanguageSwitcher />
        {user && (
          <>
            <Link to="/profile" className="btn btn-info btn-sm">
              {t("nav.profile")}
            </Link>
            <button type="button" className="btn btn-neutral btn-sm" onClick={handleLogout}>
              {t("nav.logout")}
            </button>
          </>
        )}
      </div>
    </header>
  );
}
