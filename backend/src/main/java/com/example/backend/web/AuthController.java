package com.example.backend.web;

import com.example.backend.service.UserService;
import com.example.backend.web.dto.AuthDtos;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService users;
    public AuthController(UserService users){ this.users = users; }

    @PostMapping("/signup")
    public ResponseEntity<AuthDtos.SignupResponse> signup(@RequestBody @Valid AuthDtos.SignupRequest req) {
        var u = users.signup(req.username(), req.password());
        return ResponseEntity.ok(new AuthDtos.SignupResponse(u.getId(), u.getUsername()));
    }

    @PostMapping("/login")
    public AuthDtos.LoginOkResponse login(@RequestBody @Valid AuthDtos.LoginRequest req) {
        users.authenticate(req.username(), req.password());
        return new AuthDtos.LoginOkResponse("ok"); // ← JWT導入後にトークン返却へ差し替えます
    }
}