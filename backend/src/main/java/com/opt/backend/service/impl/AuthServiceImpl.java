package com.opt.backend.service.impl;

import com.opt.backend.common.security.JwtService;
import com.opt.backend.common.security.UserPrincipal;
import com.opt.backend.dto.AuthResult;
import com.opt.backend.dto.LoginRequest;
import com.opt.backend.dto.SignupRequest;
import com.opt.backend.dto.UserResponse;
import com.opt.backend.entity.User;
import com.opt.backend.service.AuthService;
import com.opt.backend.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResult signup(SignupRequest request) {
        User user = userService.createUser(request);
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResult(token, UserResponse.from(user));
    }

    @Override
    public AuthResult login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userService.getByEmail(principal.getUsername());

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResult(token, UserResponse.from(user));
    }
}
