package com.opt.backend.repository;

import com.opt.backend.entity.PerkType;
import com.opt.backend.entity.UserPerkStats;
import com.opt.backend.entity.UserPerkStatsId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPerkStatsRepository extends JpaRepository<UserPerkStats, UserPerkStatsId> {

    List<UserPerkStats> findByIdUserId(UUID userId);

    Optional<UserPerkStats> findByIdUserIdAndIdPerkType(UUID userId, PerkType perkType);
}
