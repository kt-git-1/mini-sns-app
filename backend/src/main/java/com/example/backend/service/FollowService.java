package com.example.backend.service;

import com.example.backend.entity.Follow;
import com.example.backend.entity.FollowId;
import com.example.backend.repository.FollowRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public void follow(Long me, Long target) {
        // 自分はフォローできない
        if (me.equals(target)) throw new IllegalArgumentException("Cannot follow myself");
        // 対象ユーザーが存在しない場合、例外スロー
        if (!userRepository.existsById(target)) throw new NoSuchElementException("There is no such a user");
        // フォロー情報を保存する
        if (!followRepository.existsById_FollowerIdAndId_FollowedId(me, target)) {
            followRepository.save(Follow.of(me, target));
        }
    }

    @Transactional
    public void unfollow(Long me, Long target) {
        // フォローを外す
        followRepository.deleteById(new FollowId(me, target));
    }
}
