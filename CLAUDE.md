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

## État mesuré

**Ne recopie aucun chiffre ici.** Avant toute affirmation chiffrée, lis le tableau de synthèse d'`Audit.md` et cite la version courante. Relevés bruts dans `benchmark/`.

Indicateur directeur : **rapport POST concurrent / POST séquentiel** (part sérialisée). Une latence absolue qui baisse pendant que ce rapport stagne ne prouve rien sur la contention.

Défauts structurels à ne pas redécouvrir — valables tant que le code n'a pas changé, à vérifier avant d'en parler :

1. `writeCell` fait `findById` + `save` **par cellule** et tourne sous la section critique (`applyPlacement`) : verrou tenu pendant l'I/O SQL → plafonne la part sérialisée. Cible prioritaire du POST.
2. `GlobalPlacementCounter.incrementAndGet()` est `synchronized` pour incrémenter un `long`, sur le chemin de diffusion.
3. `BoardGrid.snapshot()` balaie les 9 600 cases à chaque lecture et construit un `ArrayList` sans capacité → coût en O(surface) et non en O(pixels posés).

Acquis à ne pas régresser : hors section critique, tout ce qui ne concerne que le joueur courant (sauvegarde de son entité, statistiques, achievements, diffusion WebSocket).

## Commandes

```bash
docker compose up -d           # stack complète sur http://localhost:8090
make bench                     # protocole de mesure complet (vegeta + hyperfine)
make bench-post-concurrent     # mesure de référence de la contention d'écriture
make bench-hyperfine           # micro-benchmark de BoardGrid.snapshot()
```
