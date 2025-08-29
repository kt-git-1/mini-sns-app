package com.example.backend.util;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

public class CursorUtil {

    /** createdAt(UTC) と id を URL-safe Base64 でエンコード（パディングなし） */
    public static String encode(OffsetDateTime createdAt, Long id){
        long epochMillis = createdAt.toInstant().toEpochMilli();
        String raw = epochMillis + ":" + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    /** 失敗時は null（＝“1ページ目扱い”にフォールバック） */
    public static Record decode(String cursor){
        if (cursor == null || cursor.isBlank()) return null;
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split(":");
            long ms = Long.parseLong(parts[0]);
            long id = Long.parseLong(parts[1]);
            OffsetDateTime ts = OffsetDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneOffset.UTC);
            return new Record(ts, id);
        } catch (Exception e) {
            return null; // 無効カーソルは無視して1ページ目扱い
        }
    }

    /** 次ページクエリの境界にそのまま使う値 */
    public record Record(OffsetDateTime createdAt, Long id) {}
}
