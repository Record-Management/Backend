package com.recordmanagement.habitlog.domain.auth.infrastructure.entity;

import com.recordmanagement.habitlog.domain.auth.domain.model.BlacklistedToken;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 블랙리스트 토큰 JPA 엔티티
 * 
 * 로그아웃된 액세스 토큰을 데이터베이스에 저장하여 토큰 탈취 공격을 방어합니다.
 * 토큰의 jti(JWT ID) claim을 기본키로 사용하여 중복 방지 및 빠른 조회를 보장합니다.
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0
 */
@Entity
@Table(
    name = "blacklisted_tokens",
    indexes = {
        @Index(name = "idx_blacklisted_tokens_user_id", columnList = "user_id"),
        @Index(name = "idx_blacklisted_tokens_expires_at", columnList = "expires_at"),
        @Index(name = "idx_blacklisted_tokens_blacklisted_at", columnList = "blacklisted_at")
    }
)
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlacklistedTokenEntity {
    
    /**
     * JWT 토큰 ID (jti claim)
     * 토큰의 고유 식별자로 사용
     */
    @Id
    @Column(name = "token_id", length = 100, nullable = false)
    private String tokenId;
    
    /**
     * 토큰 소유자 사용자 ID
     * 전체 디바이스 로그아웃 시 사용자별 토큰 무효화에 활용
     */
    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;
    
    /**
     * 원본 토큰 만료 시간
     * 이 시간이 지나면 블랙리스트에서 자동 정리
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    /**
     * 블랙리스트 등록 시간
     * 감사 및 디버깅 목적
     */
    @Column(name = "blacklisted_at", nullable = false)
    private LocalDateTime blacklistedAt;
    
    /**
     * 도메인 모델로 변환
     * 
     * @return BlacklistedToken 도메인 객체
     */
    public BlacklistedToken toDomain() {
        return new BlacklistedToken(
            tokenId,
            userId,
            expiresAt,
            blacklistedAt
        );
    }
    
    /**
     * 도메인 모델에서 엔티티 생성
     * 
     * @param blacklistedToken 도메인 객체
     * @return BlacklistedTokenEntity
     */
    public static BlacklistedTokenEntity from(BlacklistedToken blacklistedToken) {
        return BlacklistedTokenEntity.builder()
            .tokenId(blacklistedToken.getTokenId())
            .userId(blacklistedToken.getUserId())
            .expiresAt(blacklistedToken.getExpiresAt())
            .blacklistedAt(blacklistedToken.getBlacklistedAt())
            .build();
    }
    
    // Getters
    public String getTokenId() {
        return tokenId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public LocalDateTime getBlacklistedAt() {
        return blacklistedAt;
    }
}