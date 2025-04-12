package com.trybe.modulecore.post.entity;

import com.trybe.modulecore.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "post_likes")
public class PostLike extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "post_id", nullable = false, updatable = false)
    private Long postId;

    @Builder
    public PostLike(Long userId, Long postId) {
        this.userId = userId;
        this.postId = postId;
    }
}
