package com.opt.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_skins")
@Getter
@Setter
@NoArgsConstructor
public class UserSkin {

    @EmbeddedId
    private UserSkinId id;

    @CreationTimestamp
    @Column(name = "purchased_at", nullable = false, updatable = false)
    private OffsetDateTime purchasedAt;

    public UserSkin(UserSkinId id) {
        this.id = id;
    }
}
