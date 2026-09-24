package com.opt.backend.repository;

import com.opt.backend.entity.SkinId;
import com.opt.backend.entity.UserSkin;
import com.opt.backend.entity.UserSkinId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserSkinRepository extends JpaRepository<UserSkin, UserSkinId> {

    List<UserSkin> findByIdUserId(UUID userId);

    boolean existsByIdUserIdAndIdSkinId(UUID userId, SkinId skinId);
}
