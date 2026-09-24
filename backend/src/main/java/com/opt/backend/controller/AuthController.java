package com.opt.backend.controller;

import com.opt.backend.common.security.AuthCookieService;
import com.opt.backend.common.security.UserPrincipal;
import com.opt.backend.dto.AuthResult;
import com.opt.backend.dto.LoginRequest;
import com.opt.backend.dto.SignupRequest;
import com.opt.backend.dto.UserResponse;
import com.opt.backend.service.AuthService;
import com.opt.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final AuthCookieService authCookieService;

    public AuthController(AuthService authService, UserService userService, AuthCookieService authCookieService) {
        this.authService = authService;
        this.userService = userService;
        this.authCookieService = authCookieService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthResult result = authService.signup(request);
        return withAuthCookie(result, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = authService.login(request);
        return withAuthCookie(result, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, authCookieService.buildExpiredCookie().toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        var user = userService.getByEmail(principal.getUsername());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    private ResponseEntity<UserResponse> withAuthCookie(AuthResult result, HttpStatus status) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, authCookieService.buildAuthCookie(result.token()).toString())
                .body(result.user());
    }
}
