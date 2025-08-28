package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=30)
    private String username;

    @Column(name="password_hash", nullable=false)
    private String passwordHash;

    @Column(name="created_at", nullable=false, columnDefinition="timestamptz")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }
}