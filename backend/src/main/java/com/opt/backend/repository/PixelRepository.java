package com.opt.backend.repository;

import com.opt.backend.entity.Pixel;
import com.opt.backend.entity.PixelId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PixelRepository extends JpaRepository<Pixel, PixelId> {
}
