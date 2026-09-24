package com.opt.backend.controller;

import com.opt.backend.common.security.UserPrincipal;
import com.opt.backend.dto.ContributeRequest;
import com.opt.backend.dto.ContributeResponse;
import com.opt.backend.service.TeamPoolService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/team-pool")
public class TeamPoolController {

    private final TeamPoolService teamPoolService;

    public TeamPoolController(TeamPoolService teamPoolService) {
        this.teamPoolService = teamPoolService;
    }

    @PostMapping("/contribute")
    public ResponseEntity<ContributeResponse> contribute(@AuthenticationPrincipal UserPrincipal principal,
                                                            @Valid @RequestBody ContributeRequest request) {
        return ResponseEntity.ok(teamPoolService.contribute(principal.getId(), request.amount()));
    }
}
