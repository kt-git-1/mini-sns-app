package com.example.backend.service;

import com.example.backend.repository.PostRepository;
import com.example.backend.web.dto.TimelineDtos.*;
import com.example.backend.pagination.Cursor;
import com.example.backend.pagination.CursorCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TimelineService {
    private final PostRepository postRepository; // compositeTimeline(...) を想定

    private static final int MAX_SIZE = 100;

    public TimelineResponse getCompositeTimeline(Long meId, String cursorToken, int size) {
        // 1〜MAX_SIZE にクリップ
        final int pageSize = Math.max(1, Math.min(size, MAX_SIZE));

        // 1ページ目の既定カーソル
        OffsetDateTime at = OffsetDateTime.now();
        Long cid = Long.MAX_VALUE;

        if (cursorToken != null && !cursorToken.isBlank()) {
            Cursor c = CursorCodec.decode(cursorToken);
            at = c.at();
            cid = c.id();
        }

        var posts = postRepository.compositeTimeline(meId, at, cid, pageSize);

        // 次カーソル作成（createdAt の型が OffsetDateTime の想定）
        String nextCursor = null;
        if (!posts.isEmpty()) {
            var last = posts.get(posts.size() - 1);
            // last.getCreatedAt() が OffsetDateTime の場合:
            nextCursor = CursorCodec.encode(new Cursor(last.getCreatedAt(), last.getId()));
        }

        return TimelineResponse.of(posts, nextCursor);
    }
}
