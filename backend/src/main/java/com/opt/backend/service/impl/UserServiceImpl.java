package com.opt.backend.service.impl;

import com.opt.backend.common.exception.EmailAlreadyUsedException;
import com.opt.backend.dto.SignupRequest;
import com.opt.backend.entity.Team;
import com.opt.backend.entity.User;
import com.opt.backend.repository.UserRepository;
import com.opt.backend.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User createUser(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyUsedException(request.email());
        }

        User user = new User();
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setTeam(assignTeam());

        return userRepository.save(user);
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Aucun utilisateur pour l'email " + email));
    }

    @Override
    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Aucun utilisateur pour l'id " + id));
    }

    private Team assignTeam() {
        long redCount = userRepository.countByTeam(Team.RED);
        long blueCount = userRepository.countByTeam(Team.BLUE);
        if (redCount == blueCount) {
            return ThreadLocalRandom.current().nextBoolean() ? Team.RED : Team.BLUE;
        }
        return redCount < blueCount ? Team.RED : Team.BLUE;
    }
}
