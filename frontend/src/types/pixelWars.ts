export type Team = "RED" | "BLUE";

export type PerkType = "BOMB" | "CAFE" | "RAFALE" | "SHIELD" | "FRENZY" | "FORTRESS";

export type SkinId = "ORANGE" | "PURPLE" | "TEAL" | "GOLD" | "PINK" | "LIME" | "BORDER_DASHED" | "BORDER_THICK" | "CURSOR_TARGET";

export type SkinKind = "COLOR" | "BORDER" | "CURSOR";

export type AchievementId =
  | "PREMIER_PIXEL"
  | "CENTURION"
  | "CONQUERANT"
  | "COMBO_MASTER"
  | "ARTIFICIER"
  | "STRATEGE"
  | "COLLECTIONNEUR"
  | "VETERAN_EQUIPE";

export interface Pixel {
  x: number;
  y: number;
  team: Team;
}

export interface LeaderboardEntry {
  userId: string;
  fullName: string;
  team: Team;
  pixelCount: number;
}

export interface Leaderboard {
  topPlayers: LeaderboardEntry[];
  teamTotals: Record<Team, number>;
}

export interface PlayerState {
  team: Team;
  points: number;
  bombCharges: number;
  fortressCharges: number;
  selectedSkin: SkinId | null;
}

export interface BonusZone {
  x: number;
  y: number;
  until: number;
}

export interface TeamPools {
  pools: Record<Team, number>;
  threshold: number;
}

export interface Board {
  width: number;
  height: number;
  cooldownSeconds: number;
  teamColors: Record<Team, string>;
  pixels: Pixel[];
  leaderboard: Leaderboard;
  me: PlayerState;
  bonusZone: BonusZone | null;
  teamPools: TeamPools;
}

export interface AchievementUnlock {
  id: AchievementId;
  name: string;
  description: string;
}

export interface PlacePixelResult {
  placedCells: Pixel[];
  capturedCells: Pixel[];
  pointsAwarded: number;
  comboPoints: number;
  capturePoints: number;
  bonusApplied: boolean;
  me: PlayerState;
  cooldownSeconds: number;
  newAchievements: AchievementUnlock[];
}

export interface PixelsPlacedEvent {
  type: "PIXELS_PLACED";
  cells: Pixel[];
}

export interface PixelsCapturedEvent {
  type: "PIXELS_CAPTURED";
  cells: Pixel[];
}

export interface LeaderboardUpdateEvent {
  type: "LEADERBOARD_UPDATE";
  topPlayers: LeaderboardEntry[];
  teamTotals: Record<Team, number>;
}

export interface BonusZoneActiveEvent {
  type: "BONUS_ZONE_ACTIVE";
  x: number;
  y: number;
  until: number;
}

export interface BonusZoneClearedEvent {
  type: "BONUS_ZONE_CLEARED";
}

export interface TeamPoolUpdateEvent {
  type: "TEAM_POOL_UPDATE";
  team: Team;
  amount: number;
  threshold: number;
}

export interface TeamPoolTriggeredEvent {
  type: "TEAM_POOL_TRIGGERED";
  team: Team;
  until: number;
}

export interface EmoteEvent {
  type: "EMOTE";
  fullName: string;
  team: Team;
  emoji: string;
}

export type PixelWarsEvent =
  | PixelsPlacedEvent
  | PixelsCapturedEvent
  | LeaderboardUpdateEvent
  | BonusZoneActiveEvent
  | BonusZoneClearedEvent
  | TeamPoolUpdateEvent
  | TeamPoolTriggeredEvent
  | EmoteEvent;

export interface PerkDefinition {
  type: PerkType;
  name: string;
  description: string;
  cost: number;
  tier: number;
  purchaseCount: number;
}

export interface PerkCatalog {
  perks: PerkDefinition[];
  me: PlayerState;
}

export interface PerkPurchaseResult {
  me: PlayerState;
  tier: number;
  newAchievements: AchievementUnlock[];
}

export interface SkinDefinition {
  id: SkinId;
  name: string;
  kind: SkinKind;
  hexColor: string;
  cost: number;
  requiresAchievement: AchievementId | null;
}

export interface SkinCatalog {
  skins: SkinDefinition[];
  owned: SkinId[];
  selected: SkinId | null;
  me: PlayerState;
}

export interface SkinPurchaseResult {
  me: PlayerState;
  newAchievements: AchievementUnlock[];
}

export interface OwnedPerk {
  type: PerkType;
  tier: number;
  purchaseCount: number;
}

export interface UnlockedAchievement {
  id: AchievementId;
  name: string;
  description: string;
  unlockedAt: string;
}

export interface ProfileStats {
  pixelsPlaced: number;
  capturesMade: number;
  combosTriggered: number;
  bombsUsed: number;
  teamPoolContributions: number;
}

export interface Profile {
  fullName: string;
  team: Team;
  createdAt: string;
  stats: ProfileStats;
  ownedPerks: OwnedPerk[];
  ownedSkins: SkinId[];
  achievements: UnlockedAchievement[];
}

export interface GlobalStats {
  totalPlayers: number;
  totalPixelsPlacedLifetime: number;
  teamTotals: Record<Team, number>;
  boardWidth: number;
  boardHeight: number;
  boardPreviewPixels: Pixel[];
}

export interface ContributeResult {
  me: PlayerState;
  teamPools: TeamPools;
  newAchievements: AchievementUnlock[];
}
