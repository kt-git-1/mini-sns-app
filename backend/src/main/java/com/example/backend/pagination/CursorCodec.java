package com.example.backend.pagination;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CursorCodec {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static String encode(Cursor c) {
        try {
            var json = MAPPER.writeValueAsString(c);
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("cursor encode error", e);
        }
    }

    public static Cursor decode(String token) {
        try {
            var json = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            return MAPPER.readValue(json, Cursor.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("cursor decode error", e);
        }
    }
}
