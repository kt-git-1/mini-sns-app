package com.example.backend.web.dto;

import com.example.backend.entity.Post;

import java.time.OffsetDateTime;
import java.util.List;

public class TimelineDtos {
public record TimelineItem(Long id, Long userId, OffsetDateTime createdAt, String content) {
        public static TimelineItem from(Post p) {
            Long userId = (p.getUser() != null) ? p.getUser().getId() : null;
            OffsetDateTime createdAt = (p.getCreatedAt() != null) ? p.getCreatedAt(): null;
            return new TimelineItem(p.getId(), userId, createdAt, p.getContent());
        }
    }

    /** APIで返す最終形は opaqueな nextCursor 文字列にするのが安定 */
    public record TimelineResponse(List<TimelineItem> items, String nextCursor) {
        public static TimelineResponse of(List<Post> posts, String nextCursor) {
            var list = posts.stream().map(TimelineItem::from).toList();
            return new TimelineResponse(list, nextCursor);
        }
    }
}
