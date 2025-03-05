package com.trybe.modulecore.post.entity;

import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.common.entity.BaseEntity;
import com.trybe.modulecore.post.enums.PostCategory;
import com.trybe.modulecore.user.entity.User;
import jakarta.persistence.*;
import jdk.jfr.Category;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "posts")
@SQLDelete(sql = "UPDATE posts SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Post extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private PostCategory category;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Post(String title, String content, User user, PostCategory category) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.category = category;
    }

    public void updatePost(String title, String content, PostCategory category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }
}
