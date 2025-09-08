package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "follows")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Follow {
    @EmbeddedId
    private FollowId id;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Long getFollowerId(){ return id.getFollowerId(); }
    public Long getFollowedId(){ return id.getFollowedId(); }

    public static Follow of(Long followerId, Long followedId){
        return Follow.builder()
                .id(new FollowId(followerId, followedId))
                .createdAt(OffsetDateTime.now())
                .build();
    }
}
