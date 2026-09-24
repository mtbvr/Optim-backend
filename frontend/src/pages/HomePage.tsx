import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../hooks/useAuth";
import { AppHeader } from "../components/AppHeader";
import { statsApi } from "../api/statsApi";
import type { GlobalStats, Team } from "../types/pixelWars";

const TEAM_COLORS: Record<Team, string> = { RED: "#E50000", BLUE: "#0083C7" };
const NEUTRAL_COLOR = "#1a2030";
const PREVIEW_CELL_SIZE = 3;

export function HomePage() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const firstName = user?.fullName.split(" ")[0];
  const [stats, setStats] = useState<GlobalStats | null>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    statsApi.getGlobalStats().then(setStats).catch(() => {
      /* les stats globales ne sont pas critiques pour afficher l'accueil */
    });
  }, []);

  useEffect(() => {
    if (!stats) return;
    const ctx = canvasRef.current?.getContext("2d");
    if (!ctx) return;

    ctx.fillStyle = NEUTRAL_COLOR;
    ctx.fillRect(0, 0, stats.boardWidth * PREVIEW_CELL_SIZE, stats.boardHeight * PREVIEW_CELL_SIZE);
    stats.boardPreviewPixels.forEach((pixel) => {
      ctx.fillStyle = TEAM_COLORS[pixel.team];
      ctx.fillRect(pixel.x * PREVIEW_CELL_SIZE, pixel.y * PREVIEW_CELL_SIZE, PREVIEW_CELL_SIZE, PREVIEW_CELL_SIZE);
    });
  }, [stats]);

  return (
    <div>
      <AppHeader />

      <main className="home-content">
        <h1>{t("home.greeting", { name: firstName })}</h1>
        <p>{t("home.connectedAs", { email: user?.email })}</p>

        <div className="hero-card">
          <p>{t("home.heroText")}</p>
          <Link to="/pixel-wars" className="btn btn-shop btn-cta">
            {t("home.cta")}
          </Link>
        </div>

        {stats && (
          <div className="hero-card home-stats-card">
            <canvas
              ref={canvasRef}
              width={stats.boardWidth * PREVIEW_CELL_SIZE}
              height={stats.boardHeight * PREVIEW_CELL_SIZE}
              className="home-board-preview"
            />
            <div className="home-stats-grid">
              <div className="profile-stat">
                <span className="profile-stat-value">{stats.totalPlayers}</span>
                <span className="profile-stat-label">{t("home.players")}</span>
              </div>
              <div className="profile-stat">
                <span className="profile-stat-value">{stats.totalPixelsPlacedLifetime}</span>
                <span className="profile-stat-label">{t("home.totalPixels")}</span>
              </div>
              <div className="profile-stat">
                <span className="profile-stat-value" style={{ color: TEAM_COLORS.RED }}>
                  {stats.teamTotals.RED ?? 0}
                </span>
                <span className="profile-stat-label">{t("home.redTerritory")}</span>
              </div>
              <div className="profile-stat">
                <span className="profile-stat-value" style={{ color: TEAM_COLORS.BLUE }}>
                  {stats.teamTotals.BLUE ?? 0}
                </span>
                <span className="profile-stat-label">{t("home.blueTerritory")}</span>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
