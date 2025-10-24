package com.recordmanagement.habitlog.domain.auth.infrastructure.auth.repository;

import com.recordmanagement.habitlog.domain.auth.infrastructure.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 리프레시 토큰 JPA 저장소
 *
 * - 사용자 ID, 토큰 기준 조회 및 삭제
 * - 만료된 토큰 일괄 삭제 지원
 */
public interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    /**
     * 사용자 ID로 리프레시 토큰 조회
     *
     * @param userId 사용자 ID
     * @return Optional<RefreshTokenEntity>
     */
    Optional<RefreshTokenEntity> findByUserId(String userId);

    /**
     * 토큰 값으로 조회
     *
     * @param token 리프레시 토큰 문자열
     * @return Optional<RefreshTokenEntity>
     */
    Optional<RefreshTokenEntity> findByToken(String token);

    /**
     * 사용자 ID 기준 삭제
     *
     * @param userId 사용자 ID
     */
    void deleteByUserId(String userId);

    /**
     * 토큰 값으로 삭제
     *
     * @param token 리프레시 토큰
     */
    void deleteByToken(String token);

    /**
     * 현재 시간 기준 만료된 토큰 일괄 삭제
     *
     * @param now 현재 시간
     */
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
