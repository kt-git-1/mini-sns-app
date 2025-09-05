package com.example.backend.security;

import com.example.backend.web.error.ErrorResponder;
import com.example.backend.web.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

public class SecurityJsonHandlers {

    public static AuthenticationEntryPoint authenticationEntryPoint() {
        return (HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) -> {
            var body = ErrorResponse.of("unauthorized", "authentication required", req.getRequestURI(), HttpStatus.UNAUTHORIZED.value());
            ErrorResponder.write(res, body);
        };
    }

    public static AccessDeniedHandler accessDeniedHandler() {
        return (HttpServletRequest req, HttpServletResponse res, org.springframework.security.access.AccessDeniedException ex) -> {
            var body = ErrorResponse.of("forbidden", "access denied", req.getRequestURI(), HttpStatus.FORBIDDEN.value());
            ErrorResponder.write(res, body);
        };
    }
}
