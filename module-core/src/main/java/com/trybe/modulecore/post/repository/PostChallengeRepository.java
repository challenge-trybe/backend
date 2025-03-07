package com.trybe.modulecore.post.repository;

import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.post.entity.PostChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostChallengeRepository extends JpaRepository<PostChallenge, Long> {
    List<PostChallenge> findAllByPostId(Long postId);
    void deleteAllByPostId(Long postId);
}
