package com.example.backend.web;

import com.example.backend.service.PostService;
import com.example.backend.web.dto.PostDtos;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
public class PostController {

    private final PostService posts;
    public PostController(PostService posts) { this.posts = posts; }

    @PostMapping("/posts")
    public PostDtos.PostResponse create(Authentication auth, @RequestBody @Valid PostDtos.CreatePostRequest req) {
        return posts.create(auth.getName(), req.content());
    }

    @GetMapping("/timeline")
    public PostDtos.TimelineResponse timeline(
            Authentication auth,
            @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(50)
            @RequestParam(value = "limit", required = false)
            Integer limit,
            @RequestParam(value = "cursor", required = false) String cursor
    ) {
        return posts.timeline(auth.getName(), limit, cursor);
    }
}
