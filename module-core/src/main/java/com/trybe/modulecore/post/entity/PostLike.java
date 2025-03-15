package com.trybe.modulecore.post.entity;

import com.trybe.modulecore.common.entity.BaseEntity;
import com.trybe.modulecore.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "postLikes")
@SQLDelete(sql = "UPDATE postLikes SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class PostLike extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, updatable = false)
    private Post post;

    @Builder
    public PostLike(User user, Post post) {
        this.user = user;
        this.post = post;
    }
}
