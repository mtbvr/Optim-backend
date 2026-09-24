import { useCallback, useEffect, useRef, useState } from "react";
import { Fragment } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { pixelWarsApi } from "../api/pixelWarsApi";
import { skinsApi } from "../api/skinsApi";
import { teamPoolApi } from "../api/teamPoolApi";
import { emotesApi } from "../api/emotesApi";
import { extractErrorMessage } from "../api/httpClient";
import { usePixelSocket } from "../hooks/usePixelSocket";
import { useMapViewport } from "../hooks/useMapViewport";
import { useAnimatedNumber } from "../hooks/useAnimatedNumber";
import { AppHeader } from "../components/AppHeader";
import { Leaderboard } from "../components/Leaderboard";
import { PerkShop } from "../components/PerkShop";
import { SkinShop } from "../components/SkinShop";
import { TeamPoolBar } from "../components/TeamPoolBar";
import { EmotePicker } from "../components/EmotePicker";
import { EmoteFeed } from "../components/EmoteFeed";
import type { EmoteFeedEntry } from "../components/EmoteFeed";
import { Modal } from "../components/Modal";
import { RulesPanel } from "../components/RulesPanel";
import { FloatingTextLayer } from "../components/FloatingText";
import type { FloatingTextEntry } from "../components/FloatingText";
import { AchievementToastLayer } from "../components/AchievementToast";
import type { AchievementToastEntry } from "../components/AchievementToast";
import { ConfettiBurstLayer } from "../components/ConfettiBurst";
import type { ConfettiBurstEntry } from "../components/ConfettiBurst";
import { useAuth } from "../hooks/useAuth";
import { isMuted, setMuted, sound } from "../lib/sound";
import type {
  AchievementUnlock,
  Board,
  BonusZone,
  Leaderboard as LeaderboardData,
  PerkDefinition,
  PerkType,
  Pixel,
  PlayerState,
  SkinDefinition,
  SkinId,
  Team,
  TeamPools,
} from "../types/pixelWars";

const CELL_SIZE = 9;
const FLASH_DURATION_MS = 350;
const CAPTURE_FLASH_DURATION_MS = 700;
const NEUTRAL_COLOR = "#1a2030";
const SHIELD_DURATION_MS = 20000;
const SHAKE_DURATION_MS = 220;
const RING_RADIUS = 15;
const RING_CIRCUMFERENCE = 2 * Math.PI * RING_RADIUS;
const EMOTE_FEED_TTL_MS = 4000;
const TEAM_RUSH_BANNER_MS = 4500;

type SpeedBuff = { type: "CAFE" | "RAFALE"; usesRemaining: number };
type FrenzyBuff = { usesRemaining: number };
type Shockwave = { id: number; x: number; y: number };
type ArmablePerk = "BOMB" | "FORTRESS";

export function PixelWarsPage() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const gridRef = useRef<(Team | null)[][]>([]);
  const nextEffectIdRef = useRef(0);
  const shakeTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const teamRushBannerTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const [board, setBoard] = useState<Board | null>(null);
  const [me, setMe] = useState<PlayerState | null>(null);
  const [leaderboard, setLeaderboard] = useState<LeaderboardData | null>(null);
  const [perks, setPerks] = useState<PerkDefinition[]>([]);
  const [skins, setSkins] = useState<SkinDefinition[]>([]);
  const [ownedSkins, setOwnedSkins] = useState<SkinId[]>([]);
  const [purchasingType, setPurchasingType] = useState<PerkType | null>(null);
  const [busySkinId, setBusySkinId] = useState<SkinId | null>(null);
  const [armedPerk, setArmedPerk] = useState<ArmablePerk | null>(null);
  const [cooldownUntil, setCooldownUntil] = useState<number | null>(null);
  const [cooldownTotalSeconds, setCooldownTotalSeconds] = useState(0);
  const [remainingSeconds, setRemainingSeconds] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [shopOpen, setShopOpen] = useState(false);
  const [skinShopOpen, setSkinShopOpen] = useState(false);
  const [rulesOpen, setRulesOpen] = useState(false);
  const [floatingTexts, setFloatingTexts] = useState<FloatingTextEntry[]>([]);
  const [shockwaves, setShockwaves] = useState<Shockwave[]>([]);
  const [shaking, setShaking] = useState(false);
  const [speedBuff, setSpeedBuff] = useState<SpeedBuff | null>(null);
  const [frenzyBuff, setFrenzyBuff] = useState<FrenzyBuff | null>(null);
  const [shieldUntil, setShieldUntil] = useState<number | null>(null);
  const [shieldRemainingSeconds, setShieldRemainingSeconds] = useState(0);
  const [muted, setMutedState] = useState(() => isMuted());
  const [bonusZone, setBonusZone] = useState<BonusZone | null>(null);
  const [teamPools, setTeamPools] = useState<TeamPools | null>(null);
  const [contributing, setContributing] = useState(false);
  const [teamSpeedBuffUntil, setTeamSpeedBuffUntil] = useState<number | null>(null);
  const [teamSpeedBuffRemainingSeconds, setTeamSpeedBuffRemainingSeconds] = useState(0);
  const [teamRushBannerVisible, setTeamRushBannerVisible] = useState(false);
  const [emoteEntries, setEmoteEntries] = useState<EmoteFeedEntry[]>([]);
  const [achievementToasts, setAchievementToasts] = useState<AchievementToastEntry[]>([]);
  const [confettiEntries, setConfettiEntries] = useState<ConfettiBurstEntry[]>([]);

  const mapViewport = useMapViewport(board ? board.width * CELL_SIZE : 0, board ? board.height * CELL_SIZE : 0);
  const animatedPoints = useAnimatedNumber(me?.points ?? 0);

  const selectedSkinHex = skins.find((skin) => skin.id === me?.selectedSkin)?.hexColor;
  const effectiveTeamColors: Record<Team, string> = board
    ? { ...board.teamColors, ...(me && selectedSkinHex ? { [me.team]: selectedSkinHex } : {}) }
    : ({} as Record<Team, string>);

  const pushConfetti = useCallback(() => {
    setConfettiEntries((prev) => [...prev, { id: nextEffectIdRef.current++ }]);
  }, []);

  const pushAchievements = useCallback(
    (unlocks: AchievementUnlock[]) => {
      if (unlocks.length === 0) return;
      sound.achievement();
      pushConfetti();
      setAchievementToasts((prev) => [
        ...prev,
        ...unlocks.map((unlock) => ({
          id: nextEffectIdRef.current++,
          title: t("achievements.unlockedToast", { name: t(`achievements.names.${unlock.id}`) }),
          description: t(`achievements.descriptions.${unlock.id}`),
        })),
      ]);
    },
    [t, pushConfetti]
  );

  // Petit flash blanc qui s'estompe vers la couleur finale : donne un retour
  // tactile immediat, pour soi comme pour les pixels poses par les autres.
  const flashCell = useCallback((x: number, y: number, color: string, durationMs = FLASH_DURATION_MS) => {
    const start = performance.now();
    function frame(now: number) {
      const ctx = canvasRef.current?.getContext("2d");
      if (!ctx) return;
      const progress = Math.min(1, (now - start) / durationMs);
      ctx.fillStyle = color;
      ctx.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
      ctx.fillStyle = `rgba(255, 255, 255, ${(1 - progress) * 0.85})`;
      ctx.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
      if (progress < 1) requestAnimationFrame(frame);
    }
    requestAnimationFrame(frame);
  }, []);

  const applyCells = useCallback(
    (cells: Pixel[], teamColors: Record<Team, string>, durationMs?: number) => {
      cells.forEach((cell) => {
        if (!gridRef.current[cell.y]) return;
        gridRef.current[cell.y][cell.x] = cell.team;
        flashCell(cell.x, cell.y, teamColors[cell.team], durationMs);
      });
    },
    [flashCell]
  );

  // Chargement initial de la grille + de l'etat du joueur depuis le backend.
  useEffect(() => {
    pixelWarsApi
      .getBoard()
      .then((loadedBoard) => {
        const grid: (Team | null)[][] = Array.from({ length: loadedBoard.height }, () =>
          Array.from({ length: loadedBoard.width }, () => null)
        );
        loadedBoard.pixels.forEach((pixel) => {
          grid[pixel.y][pixel.x] = pixel.team;
        });
        gridRef.current = grid;
        setBoard(loadedBoard);
        setMe(loadedBoard.me);
        setLeaderboard(loadedBoard.leaderboard);
        setBonusZone(loadedBoard.bonusZone);
        setTeamPools(loadedBoard.teamPools);
      })
      .catch((err) => setError(extractErrorMessage(err, "pixelWars.errors.loadFailed")));

    pixelWarsApi
      .getPerks()
      .then((catalog) => setPerks(catalog.perks))
      .catch(() => {
        /* la boutique n'est pas critique pour afficher la grille */
      });

    skinsApi
      .getSkins()
      .then((catalog) => {
        setSkins(catalog.skins);
        setOwnedSkins(catalog.owned);
      })
      .catch(() => {
        /* l'apparence n'est pas critique pour afficher la grille */
      });
  }, []);

  // Peinture du canvas : au chargement initial, et a nouveau si le skin selectionne change
  // (recolore instantanement toutes les cases de ma propre equipe, rien que pour moi).
  useEffect(() => {
    if (!board) return;
    const ctx = canvasRef.current?.getContext("2d");
    if (!ctx) return;

    for (let y = 0; y < board.height; y++) {
      for (let x = 0; x < board.width; x++) {
        const team = gridRef.current[y][x];
        ctx.fillStyle = team ? effectiveTeamColors[team] : NEUTRAL_COLOR;
        ctx.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
      }
    }
    // effectiveTeamColors n'est pas dans les dependances : c'est un objet recalcule a chaque
    // rendu, seuls ses ingredients (board/skin choisi/catalogue) doivent redeclencher la peinture.
  }, [board, me?.selectedSkin, skins]);

  // Mises a jour en temps reel envoyees par les autres joueurs.
  const { connected } = usePixelSocket({
    onPixelsPlaced: useCallback(
      (event) => {
        if (!board) return;
        applyCells(event.cells, effectiveTeamColors);
      },
      [board, applyCells, me?.selectedSkin, skins]
    ),
    onPixelsCaptured: useCallback(
      (event) => {
        if (!board) return;
        applyCells(event.cells, effectiveTeamColors, CAPTURE_FLASH_DURATION_MS);
      },
      [board, applyCells, me?.selectedSkin, skins]
    ),
    onLeaderboardUpdate: useCallback((event) => {
      setLeaderboard({ topPlayers: event.topPlayers, teamTotals: event.teamTotals });
    }, []),
    onBonusZoneActive: useCallback((event) => {
      setBonusZone({ x: event.x, y: event.y, until: event.until });
    }, []),
    onBonusZoneCleared: useCallback(() => {
      setBonusZone(null);
    }, []),
    onTeamPoolUpdate: useCallback((event) => {
      setTeamPools((prev) => (prev ? { ...prev, pools: { ...prev.pools, [event.team]: event.amount } } : prev));
    }, []),
    onTeamPoolTriggered: useCallback(
      (event) => {
        if (me && event.team === me.team) {
          setTeamSpeedBuffUntil(event.until);
          setTeamRushBannerVisible(true);
          sound.teamRush();
          pushConfetti();
          if (teamRushBannerTimeoutRef.current) clearTimeout(teamRushBannerTimeoutRef.current);
          teamRushBannerTimeoutRef.current = setTimeout(() => setTeamRushBannerVisible(false), TEAM_RUSH_BANNER_MS);
        }
      },
      [me, pushConfetti]
    ),
    onEmote: useCallback((event) => {
      const id = nextEffectIdRef.current++;
      setEmoteEntries((prev) => [...prev.slice(-4), { id, fullName: event.fullName, team: event.team, emoji: event.emoji }]);
      sound.emote();
      setTimeout(() => {
        setEmoteEntries((prev) => prev.filter((entry) => entry.id !== id));
      }, EMOTE_FEED_TTL_MS);
    }, []),
  });

  // Decompte du cooldown local (evite d'envoyer des requetes vouees a echouer).
  useEffect(() => {
    if (!cooldownUntil) return;
    const interval = setInterval(() => {
      const remaining = Math.max(0, Math.ceil((cooldownUntil - Date.now()) / 1000));
      setRemainingSeconds(remaining);
      if (remaining <= 0) {
        setCooldownUntil(null);
        clearInterval(interval);
        sound.cooldownReady();
      }
    }, 250);
    return () => clearInterval(interval);
  }, [cooldownUntil]);

  // Decompte du bouclier d'equipe actif.
  useEffect(() => {
    if (!shieldUntil) return;
    const interval = setInterval(() => {
      const remaining = Math.max(0, Math.ceil((shieldUntil - Date.now()) / 1000));
      setShieldRemainingSeconds(remaining);
      if (remaining <= 0) {
        setShieldUntil(null);
        clearInterval(interval);
      }
    }, 250);
    return () => clearInterval(interval);
  }, [shieldUntil]);

  // Decompte de l'assaut d'equipe (cagnotte pleine).
  useEffect(() => {
    if (!teamSpeedBuffUntil) return;
    const interval = setInterval(() => {
      const remaining = Math.max(0, Math.ceil((teamSpeedBuffUntil - Date.now()) / 1000));
      setTeamSpeedBuffRemainingSeconds(remaining);
      if (remaining <= 0) {
        setTeamSpeedBuffUntil(null);
        clearInterval(interval);
      }
    }, 250);
    return () => clearInterval(interval);
  }, [teamSpeedBuffUntil]);

  useEffect(() => {
    return () => {
      if (shakeTimeoutRef.current) clearTimeout(shakeTimeoutRef.current);
      if (teamRushBannerTimeoutRef.current) clearTimeout(teamRushBannerTimeoutRef.current);
    };
  }, []);

  function removeFloatingText(id: number) {
    setFloatingTexts((prev) => prev.filter((entry) => entry.id !== id));
  }

  function removeShockwave(id: number) {
    setShockwaves((prev) => prev.filter((entry) => entry.id !== id));
  }

  function removeAchievementToast(id: number) {
    setAchievementToasts((prev) => prev.filter((entry) => entry.id !== id));
  }

  function removeConfetti(id: number) {
    setConfettiEntries((prev) => prev.filter((entry) => entry.id !== id));
  }

  async function handleCanvasClick(event: React.MouseEvent<HTMLCanvasElement>) {
    if (!board || !me || cooldownUntil) return;

    const rect = event.currentTarget.getBoundingClientRect();
    const x = Math.floor(((event.clientX - rect.left) * (board.width * CELL_SIZE)) / rect.width / CELL_SIZE);
    const y = Math.floor(((event.clientY - rect.top) * (board.height * CELL_SIZE)) / rect.height / CELL_SIZE);
    if (x < 0 || x >= board.width || y < 0 || y >= board.height) return;

    const usedBomb = armedPerk === "BOMB";
    setError(null);
    try {
      const result = await pixelWarsApi.placePixel(x, y, armedPerk ?? undefined);
      applyCells(result.placedCells, effectiveTeamColors);
      applyCells(result.capturedCells, effectiveTeamColors, CAPTURE_FLASH_DURATION_MS);
      setMe(result.me);
      setCooldownUntil(Date.now() + result.cooldownSeconds * 1000);
      setCooldownTotalSeconds(result.cooldownSeconds);
      setArmedPerk(null);

      const centerX = x * CELL_SIZE + CELL_SIZE / 2;
      const centerY = y * CELL_SIZE + CELL_SIZE / 2;
      const newTexts: FloatingTextEntry[] = [];
      if (result.comboPoints > 0) {
        newTexts.push({
          id: nextEffectIdRef.current++,
          x: centerX,
          y: centerY,
          text: t("pixelWars.comboText", { points: result.comboPoints }),
          tone: "combo",
        });
      }
      if (result.capturePoints > 0) {
        newTexts.push({
          id: nextEffectIdRef.current++,
          x: centerX,
          y: centerY + CELL_SIZE * 1.6,
          text: t("pixelWars.captureText", { points: result.capturePoints }),
          tone: "capture",
        });
      }
      if (result.bonusApplied) {
        newTexts.push({
          id: nextEffectIdRef.current++,
          x: centerX,
          y: centerY - CELL_SIZE * 1.6,
          text: t("pixelWars.bonusText"),
          tone: "bonus",
        });
      }
      if (newTexts.length > 0) {
        setFloatingTexts((prev) => [...prev, ...newTexts]);
      }

      if (usedBomb) {
        setShockwaves((prev) => [...prev, { id: nextEffectIdRef.current++, x: centerX, y: centerY }]);
        setShaking(true);
        if (shakeTimeoutRef.current) clearTimeout(shakeTimeoutRef.current);
        shakeTimeoutRef.current = setTimeout(() => setShaking(false), SHAKE_DURATION_MS);
        sound.bomb();
      } else {
        sound.place();
      }
      if (result.comboPoints > 0) sound.combo();
      if (result.capturePoints > 0) sound.capture();
      if (result.bonusApplied) sound.bonus();

      setSpeedBuff((prev) => {
        if (!prev) return prev;
        const remaining = prev.usesRemaining - 1;
        return remaining > 0 ? { ...prev, usesRemaining: remaining } : null;
      });
      setFrenzyBuff((prev) => {
        if (!prev) return prev;
        const remaining = prev.usesRemaining - 1;
        return remaining > 0 ? { usesRemaining: remaining } : null;
      });

      pushAchievements(result.newAchievements);
    } catch (err) {
      setError(extractErrorMessage(err, "pixelWars.errors.placeFailed"));
    }
  }

  async function handlePurchase(type: PerkType) {
    setPurchasingType(type);
    setError(null);
    try {
      const result = await pixelWarsApi.purchasePerk(type);
      setMe(result.me);
      sound.purchase();
      if (type === "CAFE") setSpeedBuff({ type: "CAFE", usesRemaining: 3 });
      if (type === "RAFALE") setSpeedBuff({ type: "RAFALE", usesRemaining: 1 });
      if (type === "SHIELD") setShieldUntil(Date.now() + SHIELD_DURATION_MS);
      if (type === "FRENZY") setFrenzyBuff({ usesRemaining: 3 });
      pushAchievements(result.newAchievements);
      const catalog = await pixelWarsApi.getPerks();
      setPerks(catalog.perks);
    } catch (err) {
      setError(extractErrorMessage(err, "pixelWars.errors.purchaseFailed"));
    } finally {
      setPurchasingType(null);
    }
  }

  async function handlePurchaseSkin(skinId: SkinId) {
    setBusySkinId(skinId);
    setError(null);
    try {
      const result = await skinsApi.purchaseSkin(skinId);
      setMe(result.me);
      setOwnedSkins((prev) => (prev.includes(skinId) ? prev : [...prev, skinId]));
      sound.purchase();
      pushAchievements(result.newAchievements);
    } catch (err) {
      setError(extractErrorMessage(err, "pixelWars.errors.purchaseFailed"));
    } finally {
      setBusySkinId(null);
    }
  }

  async function handleSelectSkin(skinId: SkinId | null) {
    setError(null);
    try {
      const updated = await skinsApi.selectSkin(skinId);
      setMe(updated);
    } catch (err) {
      setError(extractErrorMessage(err, "pixelWars.errors.selectFailed"));
    }
  }

  async function handleContribute(amount: number) {
    setContributing(true);
    setError(null);
    try {
      const result = await teamPoolApi.contribute(amount);
      setMe(result.me);
      setTeamPools(result.teamPools);
      sound.contribute();
      pushAchievements(result.newAchievements);
    } catch (err) {
      setError(extractErrorMessage(err, "pixelWars.errors.contributeFailed"));
    } finally {
      setContributing(false);
    }
  }

  async function handleSendEmote(emoji: string) {
    try {
      await emotesApi.send(emoji);
    } catch (err) {
      setError(extractErrorMessage(err, "pixelWars.errors.reactionFailed"));
    }
  }

  function togglePerk(type: ArmablePerk) {
    setArmedPerk((current) => (current === type ? null : type));
  }

  function toggleMute() {
    const next = !muted;
    setMuted(next);
    setMutedState(next);
  }

  if (!board || !me) {
    return <div className="page-loader">{t("common.loading")}</div>;
  }

  const myColor = effectiveTeamColors[me.team];
  const viewport = mapViewport.viewport;
  const cooldownProgress = cooldownTotalSeconds > 0 ? 1 - remainingSeconds / cooldownTotalSeconds : 1;

  return (
    <div>
      <AppHeader
        actions={
          <span className="live-indicator">
            <span className={`live-dot ${connected ? "" : "offline"}`} />
            {connected ? t("nav.live") : t("nav.reconnecting")}
          </span>
        }
      />

      <div className="map-stage">
        <div
          ref={mapViewport.containerRef}
          className={[
            "map-viewport",
            mapViewport.isPanning ? "map-viewport-panning" : "",
            speedBuff ? "speed-active" : "",
            shieldUntil ? "shield-active" : "",
            shaking ? "map-viewport-shake" : "",
          ]
            .filter(Boolean)
            .join(" ")}
          onPointerDown={mapViewport.onPointerDown}
          onPointerMove={mapViewport.onPointerMove}
          onPointerUp={mapViewport.onPointerUp}
          onPointerLeave={mapViewport.onPointerUp}
          onContextMenu={mapViewport.onContextMenu}
        >
          <div
            className="map-canvas-layer"
            style={{
              width: board.width * CELL_SIZE,
              height: board.height * CELL_SIZE,
              transform: `translate(${viewport.panX}px, ${viewport.panY}px) scale(${viewport.zoom})`,
            }}
          >
            <canvas
              ref={canvasRef}
              width={board.width * CELL_SIZE}
              height={board.height * CELL_SIZE}
              onClick={handleCanvasClick}
              className={cooldownUntil ? "canvas-cooldown" : ""}
            />
            {bonusZone && (
              <div
                className="bonus-zone-marker"
                style={{ left: bonusZone.x * CELL_SIZE, top: bonusZone.y * CELL_SIZE, width: CELL_SIZE, height: CELL_SIZE }}
              >
                🔥
              </div>
            )}
            <FloatingTextLayer entries={floatingTexts} onEntryDone={removeFloatingText} />
            {shockwaves.map((wave) => (
              <Fragment key={wave.id}>
                <div className="explosion-flash" style={{ left: wave.x, top: wave.y }} />
                <div className="shockwave" style={{ left: wave.x, top: wave.y }} onAnimationEnd={() => removeShockwave(wave.id)} />
              </Fragment>
            ))}
          </div>
        </div>

        <div className="hud-cluster hud-top-left">
          <div className="hud-badges-row">
            <span className="team-badge" style={{ borderColor: myColor, color: myColor }}>
              <span className="team-badge-dot" style={{ background: myColor }} />
              {t("pixelWars.team.badge", { team: t(`pixelWars.team.${me.team}`) })}
            </span>
            <span className="points-badge">{animatedPoints} {t("common.points")}</span>
          </div>
          {(speedBuff || frenzyBuff || shieldUntil || teamSpeedBuffUntil) && (
            <div className="hud-badges-row">
              {speedBuff && (
                <span className="active-buff-badge">
                  {t(`pixelWars.buffs.${speedBuff.type === "CAFE" ? "cafe" : "rafale"}`, { count: speedBuff.usesRemaining })}
                </span>
              )}
              {frenzyBuff && (
                <span className="active-buff-badge active-buff-frenzy">
                  {t("pixelWars.buffs.frenzy", { count: frenzyBuff.usesRemaining })}
                </span>
              )}
              {shieldUntil && (
                <span className="active-buff-badge active-buff-shield">
                  {t("pixelWars.buffs.shield", { seconds: shieldRemainingSeconds })}
                </span>
              )}
              {teamSpeedBuffUntil && (
                <span className="active-buff-badge active-buff-team">
                  {t("pixelWars.buffs.teamRush", { seconds: teamSpeedBuffRemainingSeconds })}
                </span>
              )}
            </div>
          )}
          {cooldownUntil && (
            <div className="cooldown-ring-wrap">
              <svg width="36" height="36" viewBox="0 0 36 36" className="cooldown-ring">
                <circle cx="18" cy="18" r={RING_RADIUS} className="cooldown-ring-track" />
                <circle
                  cx="18"
                  cy="18"
                  r={RING_RADIUS}
                  className="cooldown-ring-progress"
                  strokeDasharray={RING_CIRCUMFERENCE}
                  strokeDashoffset={RING_CIRCUMFERENCE * (1 - cooldownProgress)}
                />
              </svg>
              <span className="cooldown-ring-label">{remainingSeconds}s</span>
            </div>
          )}
        </div>

        {leaderboard && (
          <div className="hud-top-right">
            <Leaderboard leaderboard={leaderboard} teamColors={effectiveTeamColors} meUserId={user?.id} />
          </div>
        )}

        {teamPools && (
          <div className="hud-cluster hud-bottom-left">
            <TeamPoolBar
              team={me.team}
              teamPools={teamPools}
              myPoints={me.points}
              teamColor={myColor}
              busy={contributing}
              onContribute={handleContribute}
            />
          </div>
        )}

        <EmoteFeed entries={emoteEntries} teamColors={effectiveTeamColors} />

        {error && (
          <div className="hud-error-toast">
            <p className="form-error" role="alert">{error}</p>
          </div>
        )}

        {teamRushBannerVisible && <div className="team-rush-banner">{t("pixelWars.teamRushBanner")}</div>}

        <AchievementToastLayer entries={achievementToasts} onEntryDone={removeAchievementToast} />
        <ConfettiBurstLayer entries={confettiEntries} onEntryDone={removeConfetti} />

        <div className="hud-bottom">
          {me.bombCharges > 0 && (
            <button
              type="button"
              className={`btn btn-combat ${armedPerk === "BOMB" ? "btn-armed" : ""}`}
              onClick={() => togglePerk("BOMB")}
            >
              {t("pixelWars.toolbar.bomb", { count: me.bombCharges })}
            </button>
          )}
          {me.fortressCharges > 0 && (
            <button
              type="button"
              className={`btn btn-combat ${armedPerk === "FORTRESS" ? "btn-armed" : ""}`}
              onClick={() => togglePerk("FORTRESS")}
            >
              {t("pixelWars.toolbar.fortress", { count: me.fortressCharges })}
            </button>
          )}
          <button type="button" className="btn btn-shop" onClick={() => setShopOpen(true)}>
            {t("pixelWars.toolbar.shop")}
          </button>
          <button type="button" className="btn btn-cosmetic" onClick={() => setSkinShopOpen(true)}>
            {t("pixelWars.toolbar.appearance")}
          </button>
          <Link to="/profile" className="btn btn-info">
            {t("pixelWars.toolbar.achievements")}
          </Link>
          <button type="button" className="btn btn-info" onClick={() => setRulesOpen(true)}>
            {t("pixelWars.toolbar.rules")}
          </button>
          <button type="button" className="btn btn-neutral" onClick={mapViewport.resetView}>
            {t("pixelWars.toolbar.recenter")}
          </button>
          <button type="button" className="btn btn-neutral" onClick={toggleMute}>
            {muted ? t("pixelWars.toolbar.soundOff") : t("pixelWars.toolbar.soundOn")}
          </button>
          <EmotePicker onSend={handleSendEmote} />
        </div>
      </div>

      {shopOpen && (
        <Modal title={t("pixelWars.toolbar.shop")} onClose={() => setShopOpen(false)}>
          <PerkShop
            perks={perks}
            me={me}
            purchasingType={purchasingType}
            speedBuffActive={!!speedBuff}
            onPurchase={handlePurchase}
          />
        </Modal>
      )}

      {skinShopOpen && (
        <Modal title={t("pixelWars.toolbar.appearance")} onClose={() => setSkinShopOpen(false)}>
          <SkinShop
            skins={skins}
            owned={ownedSkins}
            selected={me.selectedSkin}
            me={me}
            busySkinId={busySkinId}
            onPurchase={handlePurchaseSkin}
            onSelect={handleSelectSkin}
          />
        </Modal>
      )}

      {rulesOpen && (
        <Modal title={t("pixelWars.toolbar.rules")} onClose={() => setRulesOpen(false)}>
          <RulesPanel />
        </Modal>
      )}
    </div>
  );
}
