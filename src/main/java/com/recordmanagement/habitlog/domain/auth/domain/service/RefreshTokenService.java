package com.recordmanagement.habitlog.domain.auth.domain.service;

import com.recordmanagement.habitlog.domain.auth.domain.model.RefreshToken;
import com.recordmanagement.habitlog.domain.auth.domain.model.TokenPair;
import com.recordmanagement.habitlog.domain.auth.domain.repository.RefreshTokenRepository;
import com.recordmanagement.habitlog.global.config.jwt.JwtTokenProvider;
import com.recordmanagement.habitlog.domain.auth.exception.JwtAuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 리프레시 토큰 도메인 서비스
 *
 * 리프레시 토큰의 생성, 검증, 갱신, 무효화 등 토큰 생명주기 관리 책임을 가집니다.
 * JWT 액세스 토큰 갱신 프로세스에서 핵심 비즈니스 로직을 수행하며 보안 정책을 적용합니다.
 *
 * 주요 기능
 * - 리프레시 토큰 생성 및 저장
 * - 액세스 토큰 갱신 (리프레시 토큰 기반)
 * - 로그아웃 시 토큰 무효화
 * - 만료된 토큰 자동 정리
 *
 * 보안 정책
 * - 사용자당 하나의 활성 리프레시 토큰 유지
 * - 토큰 사용 시 만료 및 서명 검증 필수
 * - 의심스러운 토큰 즉시 삭제 후 재로그인 유도
 *
 * 트랜잭션 정책
 * - 모든 공개 메서드는 트랜잭션 처리로 데이터 무결성 보장
 * - 토큰 생성 및 삭제는 원자적 작업으로 수행
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Service
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final long refreshTokenExpireTime;

    /**
     * 생성자
     *
     * @param refreshTokenRepository 리프레시 토큰 저장소
     * @param jwtTokenProvider JWT 토큰 생성 및 검증 서비스
     * @param refreshTokenExpireTime 리프레시 토큰 만료 시간 (밀리초)
     */
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               JwtTokenProvider jwtTokenProvider,
                               @Value("${jwt.refresh-token-expire-time}") long refreshTokenExpireTime) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenExpireTime = refreshTokenExpireTime;
    }

    /**
     * 리프레시 토큰 생성 및 저장
     *
     * 기존 사용자 토큰을 삭제하고 새 토큰을 저장하여 중복 방지 및 보안 강화
     *
     * @param userId 토큰을 발급할 사용자 ID
     * @return 새로 생성된 리프레시 토큰 문자열
     */
    public String createRefreshToken(String userId) {
        String token = jwtTokenProvider.generateRefreshToken(userId);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpireTime / 1000);

        // 사용자당 하나의 토큰만 허용: 기존 토큰 삭제
        refreshTokenRepository.deleteByUserId(userId);

        RefreshToken refreshToken = RefreshToken.of(token, userId, expiresAt);
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    /**
     * 리프레시 토큰을 사용하여 액세스 토큰 갱신 (토큰 로테이션 적용)
     *
     * 토큰 존재 여부, 만료 여부, JWT 서명 검증 후 액세스 토큰 재발급
     * 보안 강화를 위해 새로운 리프레시 토큰도 함께 발급 (토큰 로테이션)
     *
     * @param refreshTokenValue 검증할 리프레시 토큰 문자열
     * @return 새 액세스 토큰과 리프레시 토큰을 포함한 토큰 페어
     * @throws JwtAuthenticationException 토큰이 유효하지 않거나 만료된 경우
     */
    public TokenPair refreshTokens(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(JwtAuthenticationException::refreshTokenInvalid);

        if (refreshToken.isExpired()) {
            refreshTokenRepository.deleteByToken(refreshTokenValue);
            throw JwtAuthenticationException.refreshTokenExpired();
        }

        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            refreshTokenRepository.deleteByToken(refreshTokenValue);
            throw JwtAuthenticationException.refreshTokenInvalid();
        }

        String userId = refreshToken.getUserId();
        
        // 기존 리프레시 토큰 삭제 (토큰 로테이션)
        refreshTokenRepository.deleteByToken(refreshTokenValue);
        
        // 새로운 토큰 페어 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);
        String newRefreshToken = createRefreshToken(userId);
        
        return new TokenPair(newAccessToken, newRefreshToken);
    }

    /**
     * 리프레시 토큰을 사용하여 액세스 토큰만 갱신 (하위 호환성)
     *
     * @deprecated 보안상 refreshTokens() 사용 권장
     * @param refreshTokenValue 검증할 리프레시 토큰 문자열
     * @return 새 액세스 토큰 문자열
     * @throws JwtAuthenticationException 토큰이 유효하지 않거나 만료된 경우
     */
    @Deprecated
    public String refreshAccessToken(String refreshTokenValue) {
        return refreshTokens(refreshTokenValue).accessToken();
    }

    /**
     * 단일 리프레시 토큰 무효화 (단일 디바이스 로그아웃)
     *
     * @param refreshTokenValue 무효화할 리프레시 토큰 문자열
     */
    public void invalidateRefreshToken(String refreshTokenValue) {
        refreshTokenRepository.deleteByToken(refreshTokenValue);
    }

    /**
     * 사용자 ID 기준 모든 리프레시 토큰 무효화 (전체 디바이스 로그아웃)
     *
     * @param userId 모든 토큰을 삭제할 사용자 ID
     */
    public void invalidateAllRefreshTokens(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * 리프레시 토큰 유효성 검사
     *
     * DB 존재 여부, 만료 여부, JWT 검증을 모두 체크하며
     * 만료된 토큰은 자동으로 삭제 처리
     *
     * @param refreshTokenValue 검증할 리프레시 토큰 문자열
     * @return true - 유효, false - 무효
     */
    public boolean isValidRefreshToken(String refreshTokenValue) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(refreshTokenValue);

        if (refreshToken.isEmpty()) {
            return false;
        }

        if (refreshToken.get().isExpired()) {
            refreshTokenRepository.deleteByToken(refreshTokenValue);
            return false;
        }

        return jwtTokenProvider.validateToken(refreshTokenValue);
    }

    /**
     * 만료된 리프레시 토큰 일괄 삭제
     *
     * 스케줄러 혹은 수동으로 호출하여 DB 부하를 최소화하며 청소 수행
     */
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens();
    }
}