package com.example.backend.repository;

import com.example.backend.entity.Follow;
import com.example.backend.entity.FollowId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.time.OffsetDateTime;
import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {
    boolean existsById_FollowerIdAndId_FollowedId(Long followerId, Long followedId);

    // Keyset: createdAt, followedId で前方ページング
    @Query("""
    SELECT f FROM Follow f
     WHERE f.id.followerId = :userId
       AND ( (f.createdAt < :cursorAt)
          OR (f.createdAt = :cursorAt AND f.id.followedId < :cursorId) )
     ORDER BY f.createdAt DESC, f.id.followedId DESC
    """)
    List<Follow> pageFollowing(
            @Param("userId") Long userId,
            @Param("cursorAt") OffsetDateTime cursorAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    // フォロワー一覧（逆方向）
    @Query("""
    SELECT f FROM Follow f
     WHERE f.id.followedId = :userId
       AND ( (f.createdAt < :cursorAt)
          OR (f.createdAt = :cursorAt AND f.id.followerId < :cursorId) )
     ORDER BY f.createdAt DESC, f.id.followerId DESC
    """)
    List<Follow> pageFollowers(
            @Param("userId") Long userId,
            @Param("cursorAt") OffsetDateTime cursorAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
