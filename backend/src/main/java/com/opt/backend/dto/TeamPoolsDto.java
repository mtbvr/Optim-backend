package com.opt.backend.dto;

import com.opt.backend.entity.Team;

import java.util.Map;

public record TeamPoolsDto(Map<Team, Integer> pools, int threshold) {
}
