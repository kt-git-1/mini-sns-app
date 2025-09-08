package com.example.backend.web;

import com.example.backend.security.AuthUser;
import com.example.backend.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/follows")
public class FollowController {
    private final FollowService followService;

    @PostMapping("/{targetId}")
    public ResponseEntity<Void> follow(@PathVariable Long targetId, @AuthenticationPrincipal AuthUser me){
        followService.follow(me.id(), targetId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{targetId}")
    public ResponseEntity<Void> unfollow(@PathVariable Long targetId, @AuthenticationPrincipal AuthUser me){
        followService.unfollow(me.id(), targetId);
        return ResponseEntity.noContent().build();
    }
}
