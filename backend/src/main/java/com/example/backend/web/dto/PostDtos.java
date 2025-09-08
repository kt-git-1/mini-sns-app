package com.example.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

public class PostDtos {

    public record CreatePostRequest(
            @NotBlank
            @Size(max = 280)
            String content
    ) {}

    public record PostResponse(
            Long id,
            Long userId,
            String username,
            String content,
            OffsetDateTime createdAt
    ) {}
}
