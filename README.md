# Pixel Wars

Jeu multijoueur de placement de pixels par équipes (façon *r/place*), sur un plateau
partagé 120×80 en temps réel. Monolithe conteneurisé : backend Spring Boot + frontend
React + PostgreSQL.

## Stack

- **Backend** — Java 21 / Spring Boot (architecture en couches), authentification JWT
  (cookie `httpOnly`), WebSocket pour le temps réel.
- **Frontend** — React 18 / TypeScript / Vite.
- **Base de données** — PostgreSQL.
- **Infra** — Docker Compose (backend, frontend, postgres).

## Démarrer le projet

Prérequis : Docker + Docker Compose.

```bash
cp .env.example .env   # a completer (secrets, identifiants Postgres)
docker compose up -d
```

L'application est servie sur `http://localhost:8090`.

## Structure

```
backend/    Spring Boot (Java 21, Maven) — controller/ service/ repository/ entity/ dto/
frontend/   React 18 + TypeScript + Vite
```

## Endpoints principaux

Toutes les routes `/api/*` (sauf `/api/auth/*`) nécessitent le cookie JWT posé par le login.

| Méthode | Route | Rôle |
|---|---|---|
| POST | `/api/auth/signup` | Création de compte |
| POST | `/api/auth/login` | Connexion (pose le cookie JWT) |
| POST | `/api/auth/logout` | Déconnexion |
| GET | `/api/auth/me` | Utilisateur courant |
| GET | `/api/profile` | Profil du joueur connecté |
| GET | `/api/pixels` | État du plateau — **hot path** |
| POST | `/api/pixels` | Placement d'un pixel — **hot path** |
| GET | `/api/perks` | Perks disponibles |
| POST | `/api/perks/{type}/purchase` | Achat d'un perk |
| GET | `/api/skins` | Skins disponibles |
| POST | `/api/skins/{skinId}/purchase` | Achat d'un skin |
| POST | `/api/skins/select` | Sélection d'un skin |
| POST | `/api/emotes` | Envoi d'une emote |
| POST | `/api/team-pool/contribute` | Contribution à la cagnotte d'équipe |
| GET | `/api/stats/global` | Statistiques globales |
| GET | `/api/stats/board-export` | Export texte du plateau |
| WS | `/ws/pixels` | Diffusion temps réel (placements, classement) |

