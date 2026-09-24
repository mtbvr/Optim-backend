package com.opt.backend.service;

import com.opt.backend.dto.AuthResult;
import com.opt.backend.dto.LoginRequest;
import com.opt.backend.dto.SignupRequest;

public interface AuthService {

    AuthResult signup(SignupRequest request);

    AuthResult login(LoginRequest request);
}
