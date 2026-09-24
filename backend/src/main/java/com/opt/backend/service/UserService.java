package com.opt.backend.service;

import com.opt.backend.dto.SignupRequest;
import com.opt.backend.entity.User;

import java.util.UUID;

public interface UserService {

    User createUser(SignupRequest request);

    User getByEmail(String email);

    User getById(UUID id);
}
