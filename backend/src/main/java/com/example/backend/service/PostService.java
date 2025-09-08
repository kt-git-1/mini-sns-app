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
