package com.example.backend.repository;

import com.example.backend.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 1ページ目（カーソル無し）
    @Query("""
    SELECT p FROM Post p
    WHERE p.user.id IN :userIds
    ORDER BY p.createdAt DESC, p.id DESC
    """)
    List<Post> findFirstPage(Collection<Long> userIds, Pageable pageable);

    // 2ページ目以降（createdAt, id の複合キーでKeyset）
    @Query("""
    SELECT p FROM Post p
    WHERE p.user.id IN :userIds
      AND (
        p.createdAt < :createdAt OR
        (p.createdAt = :createdAt AND p.id < :id)
      )
    ORDER BY p.createdAt DESC, p.id DESC
    """)
    List<Post> findNextPage(Collection<Long> userIds, OffsetDateTime createdAt, Long id, Pageable pageable);

    // Keysetページング
    @Query(value = """
    SELECT p.*
      FROM posts p
     WHERE (p.user_id = :me
            OR p.user_id IN (SELECT followed_id FROM follows WHERE follower_id = :me))
       AND (p.created_at, p.id) < (:cursorAt, :cursorId)
     ORDER BY p.created_at DESC, p.id DESC
     LIMIT :size
    """, nativeQuery = true)
    List<Post> compositeTimeline(
            @Param("me") Long me,
            @Param("cursorAt") OffsetDateTime cursorAt,
            @Param("cursorId") Long cursorId,
            @Param("size") int size
    );
}
