package com.opt.backend.service;

import com.opt.backend.dto.PerkDefinition;
import com.opt.backend.entity.PerkType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PerkCatalog {

    private static final List<PerkDefinition> CATALOG = List.of(
            new PerkDefinition(PerkType.BOMB, "Bombe",
                    "Pose un bloc 3x3 de la couleur de ton equipe en un seul placement.", 30, 0, 0),
            new PerkDefinition(PerkType.CAFE, "Cafe",
                    "-50% de cooldown sur tes 3 prochains placements (plus de placements par palier).", 12, 0, 0),
            new PerkDefinition(PerkType.RAFALE, "Rafale",
                    "Tes prochains placements ignorent totalement le cooldown (plus de charges par palier).", 20, 0, 0),
            new PerkDefinition(PerkType.SHIELD, "Bouclier",
                    "Toute ton equipe est immunisee contre la capture (duree plus longue par palier).", 50, 0, 0),
            new PerkDefinition(PerkType.FRENZY, "Frenzie",
                    "Double tes points de combo sur tes prochains placements (plus de charges par palier).", 25, 0, 0),
            new PerkDefinition(PerkType.FORTRESS, "Forteresse",
                    "Ta prochaine case posee devient increcapturable (duree plus longue par palier).", 45, 0, 0)
    );

    public List<PerkDefinition> all() {
        return CATALOG;
    }

    public PerkDefinition get(PerkType type) {
        return CATALOG.stream()
                .filter(definition -> definition.type() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Perk inconnu: " + type));
    }
}
