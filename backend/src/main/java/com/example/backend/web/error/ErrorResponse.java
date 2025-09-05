package com.example.backend.web.error;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record ErrorResponse(
        String error,          // 例: "invalid_request", "unauthorized"
        String message,        // 人間向け詳細
        String path,           // リクエストパス
        int status,            // HTTPステータスコード
        String timestamp       // ISO-8601 UTC
) {
    public static ErrorResponse of(String error, String message, String path, int status) {
        return new ErrorResponse(
                error,
                message,
                path,
                status,
                OffsetDateTime.now(ZoneOffset.UTC).toString()
        );
    }
}
