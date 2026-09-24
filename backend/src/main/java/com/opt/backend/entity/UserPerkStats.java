package com.opt.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_perk_stats")
@Getter
@Setter
@NoArgsConstructor
public class UserPerkStats {

    @EmbeddedId
    private UserPerkStatsId id;

    @Column(name = "purchase_count", nullable = false)
    private int purchaseCount = 0;

    public UserPerkStats(UserPerkStatsId id) {
        this.id = id;
    }
}
