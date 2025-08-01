package com.recordmanagement.habitlog.domain.auth.repository;

import com.recordmanagement.habitlog.domain.auth.model.RefreshToken;

import java.util.Optional;

/**
 * 리프레시 토큰 저장소 인터페이스
 *
 * RefreshToken 도메인 객체의 저장, 조회, 삭제 기능을 정의합니다.
 * 실제 구현체는 JPA, Redis 등 저장 매체에 따라 다르게 작성될 수 있습니다.
 *
 * 주요 기능:
 * - 리프레시 토큰 저장 및 조회
 * - 토큰 또는 사용자 ID 기반 삭제
 * - 만료된 토큰 일괄 삭제
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
public interface RefreshTokenRepository {

    /**
     * 리프레시 토큰 저장
     *
     * @param refreshToken 저장할 RefreshToken 객체
     */
    void save(RefreshToken refreshToken);

    /**
     * 리프레시 토큰 문자열로 조회
     *
     * @param token JWT 리프레시 토큰 문자열
     * @return 토큰에 해당하는 RefreshToken 객체 (없으면 Optional.empty())
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 사용자 ID로 리프레시 토큰 조회
     *
     * @param userId 사용자 고유 ID
     * @return 해당 사용자의 RefreshToken 객체 (없으면 Optional.empty())
     */
    Optional<RefreshToken> findByUserId(String userId);

    /**
     * 토큰 문자열로 리프레시 토큰 삭제
     *
     * @param token 삭제할 JWT 리프레시 토큰 문자열
     */
    void deleteByToken(String token);

    /**
     * 사용자 ID로 리프레시 토큰 삭제
     *
     * @param userId 삭제할 사용자의 고유 ID
     */
    void deleteByUserId(String userId);

    /**
     * 만료된 리프레시 토큰 일괄 삭제
     * 주기적 청소 작업 시 사용
     */
    void deleteExpiredTokens();
}
