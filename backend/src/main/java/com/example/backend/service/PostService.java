package com.example.backend.service;

import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.CursorUtil;
import com.example.backend.web.dto.PostDtos;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class PostService {

    private final PostRepository posts;
    private final UserRepository users;

    public PostService(PostRepository posts, UserRepository users) {
        this.posts = posts;
        this.users = users;
    }

    @Transactional
    public PostDtos.PostResponse create(String username, String content) {
        User me = users.findByUsername(username)
                    .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "user not found"));
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "content is blank");
        }
        var post = posts.save(new Post(me, content.strip()));
        return toDto(post);
    }

    @Transactional(readOnly = true)
    public PostDtos.TimelineResponse timeline(String username, Integer limit, String cursor) {
        User me = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "user not found"));

        // ページサイズ（limit）の正規化
        int lim = (limit == null || limit <= 0 || limit > 50) ? 20 : limit;

        // 将来: フォローしているIDも追加する（V2で follows を導入したらここを拡張）
        List<Long> scopeUserIds = List.of(me.getId());

        var cur = CursorUtil.decode(cursor);
        var page = (cur == null)
                ? posts.findFirstPage(scopeUserIds, PageRequest.of(0, lim))
                : posts.findNextPage(scopeUserIds, cur.createdAt(), cur.id(), PageRequest.of(0, lim));

        var items = page.stream().map(this::toDto).toList();

        String next = null;
        if (items.size() == lim) {
            var last = page.get(page.size() - 1);
            next = CursorUtil.encode(last.getCreatedAt(), last.getId());
        }
        return new PostDtos.TimelineResponse(items, next);
    }

    private PostDtos.PostResponse toDto(Post p) {
        return new PostDtos.PostResponse(
                p.getId(),
                p.getUser().getId(),
                p.getUser().getUsername(),
                p.getContent(),
                p.getCreatedAt()
        );
    }
}
