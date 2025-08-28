package com.example.backend.web;

import com.example.backend.security.JwtService;
import com.example.backend.service.UserService;
import com.example.backend.web.dto.AuthDtos;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService users;
    private final JwtService jwt;

    public AuthController(UserService users, JwtService jwt){
        this.users = users;
        this.jwt = jwt;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthDtos.SignupResponse> signup(@RequestBody @Valid AuthDtos.SignupRequest req) {
        var u = users.signup(req.username(), req.password());
        return ResponseEntity.ok(new AuthDtos.SignupResponse(u.getId(), u.getUsername()));
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody @Valid AuthDtos.LoginRequest req) {
        var u = users.authenticate(req.username(), req.password());
        return Map.of("token", jwt.generate(u.getUsername(), u.getId()));
    }
}