package com.opt.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "user_stats")
@Getter
@Setter
@NoArgsConstructor
public class UserStats {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "pixels_placed", nullable = false)
    private int pixelsPlaced = 0;

    @Column(name = "captures_made", nullable = false)
    private int capturesMade = 0;

    @Column(name = "combos_triggered", nullable = false)
    private int combosTriggered = 0;

    @Column(name = "bombs_used", nullable = false)
    private int bombsUsed = 0;

    @Column(name = "team_pool_contributions", nullable = false)
    private int teamPoolContributions = 0;

    public UserStats(UUID userId) {
        this.userId = userId;
    }
}
