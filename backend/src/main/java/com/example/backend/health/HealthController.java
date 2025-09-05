package com.example.backend.health;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {

    private final JdbcTemplate jdbc;

    public HealthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/health")
    public Map<String, String> ok() {
        return Map.of("status", "ok");
    }

    @GetMapping("/health/db")
    public ResponseEntity<Map<String, Object>> db() {
        try {
            Integer one = jdbc.queryForObject("SELECT 1", Integer.class);
            return ResponseEntity.ok(Map.of("status", "ok", "db", "up", "check", one));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(503).body(
                    Map.of("status","degraded","db","down","path","/health/db")
            );
        }
    }
}