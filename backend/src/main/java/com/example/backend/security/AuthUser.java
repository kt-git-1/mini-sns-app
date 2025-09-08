package com.example.backend.security;

import java.io.Serializable;
import java.util.Set;

public record AuthUser(Long id, String username, Set<String> roles) implements Serializable {
    public AuthUser {
        if (id == null) throw new IllegalArgumentException("id is required");
        // usernameは空ならフォールバック（任意）
        if (username == null || username.isBlank()) username = "user-" + id;
        roles = (roles == null) ? Set.of() : Set.copyOf(roles);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
