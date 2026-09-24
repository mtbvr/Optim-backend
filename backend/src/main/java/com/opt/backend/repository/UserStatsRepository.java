package com.opt.backend.repository;

import com.opt.backend.entity.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface UserStatsRepository extends JpaRepository<UserStats, UUID> {

    @Query("select coalesce(sum(s.pixelsPlaced), 0) from UserStats s")
    long sumPixelsPlaced();
}
