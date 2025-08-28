package com.example.backend.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class MyPageController {

    @GetMapping("/mypage")
    public Map<String, Object> mypage(Authentication auth) {
        return Map.of("username", auth.getName());
    }
}
