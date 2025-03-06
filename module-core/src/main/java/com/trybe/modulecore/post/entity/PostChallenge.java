package com.trybe.modulecore.post.entity;

import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "post_challenges")
@SQLDelete(sql = "UPDATE post_challenges SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class PostChallenge extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, updatable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false, updatable = false)
    private Challenge challenge;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public PostChallenge(Post post, Challenge challenge) {
        this.post = post;
        this.challenge = challenge;
    }
}
