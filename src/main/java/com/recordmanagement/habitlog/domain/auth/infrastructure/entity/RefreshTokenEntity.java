package com.recordmanagement.habitlog.domain.auth.infrastructure.entity;

import com.recordmanagement.habitlog.domain.auth.domain.model.RefreshToken;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 리프레시 토큰 JPA 엔터티
 *
 * DB 테이블: refresh_tokens
 * 도메인 모델 ↔ JPA 엔터티 변환 책임
 */
@Getter
@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자
@AllArgsConstructor
public class RefreshTokenEntity {

    @Id
    @Column(name = "token", length = 512)
    private String token;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 도메인 모델 → JPA 엔터티 변환
     *
     * @param refreshToken 도메인 모델 객체
     * @return JPA 엔터티
     */
    public static RefreshTokenEntity from(RefreshToken refreshToken) {
        return new RefreshTokenEntity(
                refreshToken.getToken(),
                refreshToken.getUserId(),
                refreshToken.getExpiresAt(),
                refreshToken.getCreatedAt()
        );
    }

    /**
     * JPA 엔터티 → 도메인 모델 변환
     *
     * @return 도메인 모델 객체
     */
    public RefreshToken toDomain() {
        RefreshToken refreshToken = RefreshToken.of(token, userId, expiresAt);
        injectCreatedAt(refreshToken);
        return refreshToken;
    }

    /**
     * createdAt 값 도메인 객체에 리플렉션으로 주입
     * 도메인 모델은 생성자 외 set 메서드가 없어야 하므로 리플렉션 사용
     */
    private void injectCreatedAt(RefreshToken refreshToken) {
        try {
            var field = RefreshToken.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(refreshToken, this.createdAt);
        } catch (Exception e) {
            throw new RuntimeException("리프레시 토큰의 생성 시간 설정에 실패했습니다", e);
        }
    }
}
