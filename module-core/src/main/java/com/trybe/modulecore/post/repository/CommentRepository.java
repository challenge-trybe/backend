package com.trybe.modulecore.post.repository;

import com.trybe.modulecore.post.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findAllByPostId(Long postId, Pageable pageable);
    Page<Comment> findAllByUserId(Long userId, Pageable pageable);
    void deleteAllByPostId(Long postId);
}
