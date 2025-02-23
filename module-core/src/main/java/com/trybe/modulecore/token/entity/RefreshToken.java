package com.trybe.modulecore.token.entity;

import com.trybe.modulecore.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "refresh_token")
@NoArgsConstructor
@Getter
@SQLDelete(sql = "UPDATE refresh_token SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class RefreshToken extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name="refresh_token", nullable = false)
    private String refreshToken;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public RefreshToken(String userId, String refreshToken) {
        this.userId = userId;
        this.refreshToken = refreshToken;
    }

    public void updateRefreshToken(String refreshToken){
        this.refreshToken = refreshToken;
    }
}
