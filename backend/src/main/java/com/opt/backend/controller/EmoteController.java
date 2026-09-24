package com.opt.backend.controller;

import com.opt.backend.common.exception.EmoteRateLimitException;
import com.opt.backend.common.exception.InvalidEmoteException;
import com.opt.backend.common.pixelwars.EmoteRateLimiter;
import com.opt.backend.common.security.UserPrincipal;
import com.opt.backend.common.websocket.PixelWebSocketHandler;
import com.opt.backend.dto.EmoteEvent;
import com.opt.backend.dto.EmoteRequest;
import com.opt.backend.entity.User;
import com.opt.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/emotes")
public class EmoteController {

    private static final Set<String> ALLOWED_EMOJIS = Set.of("👍", "😂", "😡", "🔥", "💀", "🎯");

    private final UserRepository userRepository;
    private final EmoteRateLimiter emoteRateLimiter;
    private final PixelWebSocketHandler webSocketHandler;

    public EmoteController(UserRepository userRepository, EmoteRateLimiter emoteRateLimiter, PixelWebSocketHandler webSocketHandler) {
        this.userRepository = userRepository;
        this.emoteRateLimiter = emoteRateLimiter;
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping
    public ResponseEntity<Void> sendEmote(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody EmoteRequest request) {
        if (!ALLOWED_EMOJIS.contains(request.emoji())) {
            throw new InvalidEmoteException();
        }
        if (!emoteRateLimiter.tryAcquire(principal.getId())) {
            throw new EmoteRateLimitException();
        }
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new EntityNotFoundException("Aucun utilisateur pour l'id " + principal.getId()));

        webSocketHandler.broadcast(EmoteEvent.of(user.getFullName(), user.getTeam(), request.emoji()));
        return ResponseEntity.noContent().build();
    }
}
