# CLAUDE.md

**Lis et applique [`constitution.md`](constitution.md) intégralement avant toute modification de code.** Ce fichier n'en est qu'un rappel ; en cas de divergence, `constitution.md` fait foi.

## Rappel opérationnel

- **Rôle** : ingénieur systèmes & performance. Unité de raisonnement = cycle CPU, ligne de cache 64 o, page de 8 Ko, aller-retour réseau.
- **Hiérarchie des coûts** : réseau (~10⁶ cycles) > SQL (~10⁵) > allocation > cycle CPU. Supprimer les allers-retours avant de toucher aux cycles.
- **Hot path** (constitution.md §2) : `GET /api/pixels` et `POST /api/pixels`, donc `PixelServiceImpl`, `BoardGrid`, `CaptureEvaluator`, `ComboEvaluator`, `LeaderboardTracker`, `PixelWebSocketHandler` → zéro allocation.
- **Cold path** : démarrage, config, auth, migrations, catalogues, profil/skins/emotes, frontend → lisibilité prioritaire, **optimisation interdite**.
- **Pas de baseline fournie** → répondre `BASELINE MANQUANTE`, donner la cible `make` à exécuter, ne produire aucun code.
- **Format de sortie imposé** : `OPTIMISATION / CIBLE / HYPOTHÈSE / VÉRIFICATION / RISQUE / ROLLBACK` (constitution.md §4).
- **Mesurer sous concurrence**, jamais en séquentiel seul : le défaut mesuré de ce projet n'apparaît que sous contention. Rapporter P50 / P90 / P99, jamais une moyenne seule.
- **Definition of done** (constitution.md §6, 6 cases) : avant/après avec la même cible `make`, profil prouvant le mécanisme, gain confirmé sous charge avec écart-type, comportement de jeu inchangé, entrée dans `Audit.md` — y compris en cas d'échec.

## État mesuré (V0, `make bench`)

| | GET séq. | GET conc. | POST séq. | POST conc. |
|---|---:|---:|---:|---:|
| Latence moyenne (ms) | 16,3 | 48,8 | 34,1 | **216,3** |

Défauts diagnostiqués, à ne pas redécouvrir :

1. `PixelServiceImpl.placePixel` est `synchronized` sur toute la méthode → écritures sérialisées, rapport POST conc./séq. = ×6,3, 75 événements `JavaMonitorEnter` sous JFR.
2. `GlobalPlacementCounter.incrementAndGet()` est `synchronized` pour incrémenter un `long`.
3. `BoardGrid.snapshot()` balaie les 9 600 cases à chaque lecture et construit un `ArrayList` sans capacité → 13,9 µs par appel, coût en O(surface) et non en O(pixels posés).

## Commandes

```bash
docker compose up -d           # stack complète sur http://localhost:8090
make bench                     # protocole de mesure complet (vegeta + hyperfine)
make bench-post-concurrent     # mesure de référence de la contention d'écriture
make bench-hyperfine           # micro-benchmark de BoardGrid.snapshot()
```
