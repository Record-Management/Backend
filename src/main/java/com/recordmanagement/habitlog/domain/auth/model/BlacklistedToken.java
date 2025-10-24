package com.recordmanagement.habitlog.domain.auth.model;

import java.time.LocalDateTime;

/**
 * 블랙리스트된 토큰 도메인 모델
 * 
 * 로그아웃된 액세스 토큰을 추적하여 토큰 탈취 시에도 안전하게 처리합니다.
 * 토큰의 고유 식별자(jti)를 저장하여 메모리 효율성을 높입니다.
 * 
 * @param tokenId JWT 토큰의 고유 식별자 (jti claim)
 * @param userId 토큰 소유자 ID
 * @param expiresAt 원본 토큰 만료 시간
 * @param blacklistedAt 블랙리스트 등록 시간
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0
 */
public record BlacklistedToken(
    String tokenId,
    String userId,
    LocalDateTime expiresAt,
    LocalDateTime blacklistedAt
) {
    /**
     * 블랙리스트 토큰 생성
     * 
     * @param tokenId JWT 토큰 ID (jti claim)
     * @param userId 사용자 ID
     * @param expiresAt 토큰 만료 시간
     * @return BlacklistedToken 인스턴스
     */
    public static BlacklistedToken of(String tokenId, String userId, LocalDateTime expiresAt) {
        return new BlacklistedToken(
            tokenId,
            userId,
            expiresAt,
            LocalDateTime.now()
        );
    }
    
    /**
     * 토큰이 이미 만료되었는지 확인
     * 
     * @return true: 만료됨, false: 유효함
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * 토큰 ID 반환
     * 
     * @return 토큰 ID
     */
    public String getTokenId() {
        return tokenId;
    }
    
    /**
     * 사용자 ID 반환
     * 
     * @return 사용자 ID
     */
    public String getUserId() {
        return userId;
    }
    
    /**
     * 만료 시간 반환
     * 
     * @return 만료 시간
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    /**
     * 블랙리스트 등록 시간 반환
     * 
     * @return 블랙리스트 등록 시간
     */
    public LocalDateTime getBlacklistedAt() {
        return blacklistedAt;
    }
}