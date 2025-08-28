package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public User signup(String username, String rawPassword) {
        if (users.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username already taken");
        }
        var u = new User(username, encoder.encode(rawPassword));
        return users.save(u);
    }

    @Transactional(readOnly = true)
    public User authenticate(String username, String rawPassword) {
        var u = users.findByUsername(username)
                     .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials"));
        if (!encoder.matches(rawPassword, u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");
        }
        return u;
    }
}
