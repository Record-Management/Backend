package com.recordmanagement.habitlog.domain.auth.domain.model;

/**
 * 토큰 페어 (액세스 토큰 + 리프레시 토큰)
 * 
 * 토큰 로테이션 시 새로운 액세스 토큰과 리프레시 토큰을 함께 반환하기 위한 DTO
 * 
 * @param accessToken 새로 발급된 액세스 토큰
 * @param refreshToken 새로 발급된 리프레시 토큰
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0
 */
public record TokenPair(
    String accessToken,
    String refreshToken
) {
    /**
     * 토큰 페어 생성
     * 
     * @param accessToken 액세스 토큰 (null 불가)
     * @param refreshToken 리프레시 토큰 (null 불가)
     * @throws IllegalArgumentException 토큰이 null이거나 빈 문자열인 경우
     */
    public TokenPair {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Access token cannot be null or empty");
        }
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Refresh token cannot be null or empty");
        }
    }
    
    /**
     * 액세스 토큰 반환
     * 
     * @return 액세스 토큰 문자열
     */
    public String getAccessToken() {
        return accessToken;
    }
    
    /**
     * 리프레시 토큰 반환
     * 
     * @return 리프레시 토큰 문자열
     */
    public String getRefreshToken() {
        return refreshToken;
    }
}