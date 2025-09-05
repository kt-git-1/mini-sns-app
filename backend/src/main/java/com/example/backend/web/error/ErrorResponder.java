package com.example.backend.web.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.nio.charset.StandardCharsets;

public class ErrorResponder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void write(HttpServletResponse res, ErrorResponse body) {
        try {
            res.setStatus(body.status());
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.setContentType("application/json");
            res.getWriter().write(MAPPER.writeValueAsString(body));
        } catch (Exception ignored) {}
    }
}
