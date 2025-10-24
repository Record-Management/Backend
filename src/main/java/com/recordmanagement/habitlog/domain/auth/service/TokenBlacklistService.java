package com.recordmanagement.habitlog.domain.auth.service;

import com.recordmanagement.habitlog.config.jwt.JwtTokenProvider;
import com.recordmanagement.habitlog.domain.auth.model.BlacklistedToken;
import com.recordmanagement.habitlog.domain.auth.repository.BlacklistedTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 토큰 블랙리스트 도메인 서비스
 * 
 * 로그아웃된 액세스 토큰을 블랙리스트에 등록하고 관리합니다.
 * 토큰 탈취 공격을 방어하기 위해 무효화된 토큰의 재사용을 차단합니다.
 * 
 * 주요 기능:
 * - 액세스 토큰 블랙리스트 등록
 * - 토큰 블랙리스트 상태 확인
 * - 만료된 블랙리스트 토큰 정리
 * - 사용자별 전체 토큰 무효화
 * 
 * 보안 정책:
 * - 로그아웃 시 즉시 토큰 블랙리스트 등록
 * - 블랙리스트된 토큰은 유효성 검증 실패 처리
 * - 토큰 만료 시간까지만 블랙리스트 유지 (메모리 효율성)
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0
 */
@Slf4j
@Service
@Transactional
public class TokenBlacklistService {
    
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    
    public TokenBlacklistService(BlacklistedTokenRepository blacklistedTokenRepository,
                                JwtTokenProvider jwtTokenProvider) {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    /**
     * 액세스 토큰을 블랙리스트에 등록
     * 
     * @param accessToken 블랙리스트에 등록할 액세스 토큰
     */
    public void blacklistToken(String accessToken) {
        try {
            // 토큰에서 정보 추출
            String tokenId = jwtTokenProvider.getTokenIdFromToken(accessToken);
            String userId = jwtTokenProvider.getUserIdAsStringFromToken(accessToken);
            long expirationMs = jwtTokenProvider.getExpiration(accessToken);
            
            // 이미 만료된 토큰은 블랙리스트에 등록하지 않음
            if (expirationMs <= 0) {
                log.debug("이미 만료된 토큰, 블랙리스트 등록 스킵: tokenId={}", tokenId);
                return;
            }
            
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expirationMs / 1000);
            
            // 이미 블랙리스트에 등록된 토큰인지 확인
            if (blacklistedTokenRepository.isBlacklisted(tokenId)) {
                log.debug("이미 블랙리스트에 등록된 토큰: tokenId={}", tokenId);
                return;
            }
            
            // 블랙리스트에 등록
            BlacklistedToken blacklistedToken = BlacklistedToken.of(tokenId, userId, expiresAt);
            blacklistedTokenRepository.save(blacklistedToken);
            
            log.info("토큰 블랙리스트 등록 완료: tokenId={}, userId={}, expiresAt={}", 
                    tokenId, userId, expiresAt);
                    
        } catch (Exception e) {
            log.error("토큰 블랙리스트 등록 실패: token={}, error={}", 
                    accessToken.substring(0, Math.min(20, accessToken.length())), e.getMessage());
            // 블랙리스트 등록 실패 시에도 로그아웃은 진행 (토큰 무효화)
        }
    }
    
    /**
     * 토큰이 블랙리스트에 등록되어 있는지 확인
     * 
     * @param accessToken 확인할 액세스 토큰
     * @return true: 블랙리스트됨(무효), false: 유효함
     */
    @Transactional(readOnly = true)
    public boolean isTokenBlacklisted(String accessToken) {
        try {
            String tokenId = jwtTokenProvider.getTokenIdFromToken(accessToken);
            boolean isBlacklisted = blacklistedTokenRepository.isBlacklisted(tokenId);
            
            log.debug("토큰 블랙리스트 확인: tokenId={}, isBlacklisted={}", tokenId, isBlacklisted);
            return isBlacklisted;
            
        } catch (Exception e) {
            log.warn("토큰 블랙리스트 확인 실패, 무효 토큰으로 처리: error={}", e.getMessage());
            return true; // 파싱 실패 시 블랙리스트된 것으로 간주
        }
    }
    
    /**
     * 특정 사용자의 모든 토큰을 블랙리스트에 추가
     * 전체 디바이스 로그아웃이나 보안 사고 시 사용
     * 
     * @param userId 사용자 ID
     */
    public void blacklistAllUserTokens(String userId) {
        // 현재 시간으로부터 액세스 토큰 최대 만료 시간까지 블랙리스트 유지
        LocalDateTime maxExpiresAt = LocalDateTime.now().plusHours(24); // 24시간 여유
        
        blacklistedTokenRepository.blacklistAllUserTokens(userId, maxExpiresAt);
        log.info("사용자 전체 토큰 블랙리스트 등록: userId={}", userId);
    }
    
    /**
     * 만료된 블랙리스트 토큰들 정리
     * 스케줄러에서 주기적으로 호출하여 저장소 정리
     */
    public void cleanupExpiredBlacklistedTokens() {
        blacklistedTokenRepository.deleteExpiredTokens();
        log.info("만료된 블랙리스트 토큰 정리 완료");
    }
    
    /**
     * 블랙리스트 토큰 통계 조회
     * 모니터링 및 디버깅 목적
     * 
     * @return 현재 블랙리스트 토큰 총 개수
     */
    @Transactional(readOnly = true)
    public long getBlacklistCount() {
        return blacklistedTokenRepository.count();
    }
}