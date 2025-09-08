package com.example.backend.pagination;

import java.time.OffsetDateTime;

public record Cursor(OffsetDateTime at, Long id) {}