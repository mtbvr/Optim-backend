package com.opt.backend.service;

import com.opt.backend.dto.SkinDefinition;
import com.opt.backend.entity.AchievementId;
import com.opt.backend.entity.SkinId;
import com.opt.backend.entity.SkinKind;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SkinCatalog {

    private static final List<SkinDefinition> CATALOG = List.of(
            new SkinDefinition(SkinId.ORANGE, "Orange", SkinKind.COLOR, "#F97316", 20, null),
            new SkinDefinition(SkinId.PURPLE, "Violet", SkinKind.COLOR, "#A855F7", 20, null),
            new SkinDefinition(SkinId.TEAL, "Turquoise", SkinKind.COLOR, "#14B8A6", 20, null),
            new SkinDefinition(SkinId.GOLD, "Or", SkinKind.COLOR, "#EAB308", 30, null),
            new SkinDefinition(SkinId.PINK, "Rose", SkinKind.COLOR, "#EC4899", 20, null),
            new SkinDefinition(SkinId.LIME, "Citron vert", SkinKind.COLOR, "#84CC16", 20, null),
            new SkinDefinition(SkinId.BORDER_DASHED, "Bordure pointillee", SkinKind.BORDER, "#eef0f6", 0, AchievementId.CENTURION),
            new SkinDefinition(SkinId.BORDER_THICK, "Bordure epaisse", SkinKind.BORDER, "#eef0f6", 0, AchievementId.CONQUERANT),
            new SkinDefinition(SkinId.CURSOR_TARGET, "Curseur viseur", SkinKind.CURSOR, "#eef0f6", 0, AchievementId.STRATEGE)
    );

    public List<SkinDefinition> all() {
        return CATALOG;
    }

    public SkinDefinition get(SkinId id) {
        return CATALOG.stream()
                .filter(definition -> definition.id() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Skin inconnu: " + id));
    }
}
