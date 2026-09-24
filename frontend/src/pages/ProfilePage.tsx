import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { profileApi } from "../api/profileApi";
import { AppHeader } from "../components/AppHeader";
import type { Profile } from "../types/pixelWars";

export function ProfilePage() {
  const { t, i18n } = useTranslation();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    profileApi
      .getProfile()
      .then(setProfile)
      .catch(() => setError(t("profile.loadError")));
  }, [t]);

  return (
    <div>
      <AppHeader />
      <main className="profile-content">
        {error && <p className="form-error" role="alert">{error}</p>}
        {!profile && !error && <p className="page-loader">{t("common.loading")}</p>}

        {profile && (
          <>
            <div className="auth-card profile-header-card">
              <h1>{profile.fullName}</h1>
              <p className="auth-subtitle">
                {t("profile.memberSince", {
                  team: t(`pixelWars.team.${profile.team}`),
                  date: new Date(profile.createdAt).toLocaleDateString(i18n.language),
                })}
              </p>
            </div>

            <div className="leaderboard-panel profile-section">
              <h2>{t("profile.statsTitle")}</h2>
              <div className="profile-stats-grid">
                <div className="profile-stat">
                  <span className="profile-stat-value">{profile.stats.pixelsPlaced}</span>
                  <span className="profile-stat-label">{t("profile.stats.pixelsPlaced")}</span>
                </div>
                <div className="profile-stat">
                  <span className="profile-stat-value">{profile.stats.capturesMade}</span>
                  <span className="profile-stat-label">{t("profile.stats.capturesMade")}</span>
                </div>
                <div className="profile-stat">
                  <span className="profile-stat-value">{profile.stats.combosTriggered}</span>
                  <span className="profile-stat-label">{t("profile.stats.combosTriggered")}</span>
                </div>
                <div className="profile-stat">
                  <span className="profile-stat-value">{profile.stats.bombsUsed}</span>
                  <span className="profile-stat-label">{t("profile.stats.bombsUsed")}</span>
                </div>
                <div className="profile-stat">
                  <span className="profile-stat-value">{profile.stats.teamPoolContributions}</span>
                  <span className="profile-stat-label">{t("profile.stats.teamPoolContributions")}</span>
                </div>
              </div>
            </div>

            <div className="leaderboard-panel profile-section">
              <h2>{t("profile.perksTitle")}</h2>
              {profile.ownedPerks.length === 0 ? (
                <p className="leaderboard-empty">{t("profile.noPerks")}</p>
              ) : (
                <ul className="profile-perk-list">
                  {profile.ownedPerks.map((perk) => (
                    <li key={perk.type}>
                      <span>{t(`perks.names.${perk.type}`)}</span>
                      <span className="profile-perk-tier">
                        {t("perks.tier", { tier: perk.tier })} ({t("profile.purchases", { count: perk.purchaseCount })})
                      </span>
                    </li>
                  ))}
                </ul>
              )}
            </div>

            <div className="leaderboard-panel profile-section">
              <h2>{t("profile.skinsTitle")}</h2>
              {profile.ownedSkins.length === 0 ? (
                <p className="leaderboard-empty">{t("profile.noSkins")}</p>
              ) : (
                <div className="profile-skin-grid">
                  {profile.ownedSkins.map((skinId) => (
                    <span key={skinId} className="profile-skin-chip">{t(`skins.names.${skinId}`)}</span>
                  ))}
                </div>
              )}
            </div>

            <div className="leaderboard-panel profile-section">
              <h2>{t("profile.achievementsTitle")}</h2>
              {profile.achievements.length === 0 ? (
                <p className="leaderboard-empty">{t("profile.noAchievements")}</p>
              ) : (
                <ul className="profile-achievement-list">
                  {profile.achievements.map((achievement) => (
                    <li key={achievement.id}>
                      <span className="profile-achievement-icon">🏆</span>
                      <div>
                        <p className="profile-achievement-name">{t(`achievements.names.${achievement.id}`)}</p>
                        <p className="profile-achievement-date">
                          {t(`achievements.descriptions.${achievement.id}`)} —{" "}
                          {new Date(achievement.unlockedAt).toLocaleDateString(i18n.language)}
                        </p>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </>
        )}
      </main>
    </div>
  );
}
