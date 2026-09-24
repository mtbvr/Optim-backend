package com.opt.backend.service;

import com.opt.backend.dto.AchievementDefinition;
import com.opt.backend.entity.AchievementId;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AchievementCatalog {

    private static final List<AchievementDefinition> CATALOG = List.of(
            new AchievementDefinition(AchievementId.PREMIER_PIXEL, "Premier pixel", "Place ton tout premier pixel."),
            new AchievementDefinition(AchievementId.CENTURION, "Centurion", "Place 100 pixels au total."),
            new AchievementDefinition(AchievementId.CONQUERANT, "Conquerant", "Capture 10 pixels adverses au total."),
            new AchievementDefinition(AchievementId.COMBO_MASTER, "Combo Master", "Declenche 5 combos."),
            new AchievementDefinition(AchievementId.ARTIFICIER, "Artificier", "Utilise 10 bombes."),
            new AchievementDefinition(AchievementId.STRATEGE, "Stratege", "Achete au moins une fois chaque type de perk."),
            new AchievementDefinition(AchievementId.COLLECTIONNEUR, "Collectionneur", "Possede tous les skins."),
            new AchievementDefinition(AchievementId.VETERAN_EQUIPE, "Veteran d'equipe", "Contribue 3 fois a la cagnotte d'equipe.")
    );

    public List<AchievementDefinition> all() {
        return CATALOG;
    }

    public AchievementDefinition get(AchievementId id) {
        return CATALOG.stream()
                .filter(definition -> definition.id() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Succes inconnu: " + id));
    }
}
