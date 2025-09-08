package com.example.backend.web;

import com.example.backend.web.dto.TimelineDtos;
import com.example.backend.security.AuthUser;
import com.example.backend.service.TimelineService;
import com.example.backend.web.dto.PostDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/timeline")
public class TimelineController {
    private final TimelineService timelineService;

    @GetMapping
    public ResponseEntity<TimelineDtos.TimelineResponse> getTimeline(
            @AuthenticationPrincipal AuthUser me,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ){
        TimelineDtos.TimelineResponse resp =
                timelineService.getCompositeTimeline(me.id(), cursor, size);
        return ResponseEntity.ok(resp);
    }
}
