package com.recordmanagement.habitlog.domain.auth.repository;

import com.recordmanagement.habitlog.domain.auth.model.BlacklistedToken;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 블랙리스트 토큰 저장소 인터페이스
 * 
 * 로그아웃된 액세스 토큰을 블랙리스트에 등록하고 조회하는 기능을 제공합니다.
 * 토큰 탈취 공격을 방어하기 위해 무효화된 토큰을 추적합니다.
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0
 */
public interface BlacklistedTokenRepository {
    
    /**
     * 블랙리스트 토큰 저장
     * 
     * @param blacklistedToken 저장할 블랙리스트 토큰
     * @return 저장된 블랙리스트 토큰
     */
    BlacklistedToken save(BlacklistedToken blacklistedToken);
    
    /**
     * 토큰 ID로 블랙리스트 토큰 조회
     * 
     * @param tokenId 조회할 토큰 ID (jti)
     * @return 블랙리스트 토큰 (없으면 empty)
     */
    Optional<BlacklistedToken> findByTokenId(String tokenId);
    
    /**
     * 토큰이 블랙리스트에 등록되어 있는지 확인
     * 
     * @param tokenId 확인할 토큰 ID (jti)
     * @return true: 블랙리스트됨, false: 유효함
     */
    boolean isBlacklisted(String tokenId);
    
    /**
     * 특정 사용자의 모든 토큰을 블랙리스트에 추가
     * (전체 디바이스 로그아웃 시 사용)
     * 
     * @param userId 사용자 ID
     * @param expiresAt 토큰 만료 시간
     */
    void blacklistAllUserTokens(String userId, LocalDateTime expiresAt);
    
    /**
     * 특정 토큰 ID 블랙리스트에서 제거
     * 
     * @param tokenId 제거할 토큰 ID
     */
    void deleteByTokenId(String tokenId);
    
    /**
     * 만료된 블랙리스트 토큰들 일괄 삭제
     * 스케줄러에서 주기적으로 호출하여 저장소 정리
     */
    void deleteExpiredTokens();
    
    /**
     * 전체 블랙리스트 토큰 개수 조회
     * 모니터링 및 디버깅 목적
     * 
     * @return 블랙리스트 토큰 총 개수
     */
    long count();
}