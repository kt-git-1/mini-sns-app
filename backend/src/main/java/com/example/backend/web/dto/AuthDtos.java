package com.example.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 認証系のDTOをひとまとめにしたクラス群。
 * 小規模プロジェクトではこの形が保守しやすい。
 */
public class AuthDtos {

    public record SignupRequest(
        @NotBlank @Size(min=3, max=30) String username,
        @NotBlank @Size(min=8, max=128) String password
    ) {}

    public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
    ) {}

    public record SignupResponse(Long id, String username) {}
    public record LoginOkResponse(String status) {}
}
