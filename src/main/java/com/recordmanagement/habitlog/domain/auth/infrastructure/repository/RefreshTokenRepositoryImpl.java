package com.recordmanagement.habitlog.domain.auth.infrastructure.auth.repository;

import com.recordmanagement.habitlog.domain.auth.domain.model.RefreshToken;
import com.recordmanagement.habitlog.domain.auth.domain.repository.RefreshTokenRepository;
import com.recordmanagement.habitlog.domain.auth.infrastructure.entity.RefreshTokenEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 리프레시 토큰 저장소 구현체
 *
 * - 도메인 모델 <-> JPA Entity 변환 처리
 * - 기존 사용자 토큰 존재 시 중복 저장 방지
 */
@Repository
@RequiredArgsConstructor
@Transactional
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final JpaRefreshTokenRepository jpaRefreshTokenRepository;

    /**
     * 리프레시 토큰 저장 (기존 사용자 토큰은 삭제 후 재저장)
     */
    @Override
    public void save(RefreshToken refreshToken) {
        deleteByUserId(refreshToken.getUserId());
        jpaRefreshTokenRepository.save(RefreshTokenEntity.from(refreshToken));
    }

    /**
     * 토큰으로 리프레시 토큰 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRefreshTokenRepository.findByToken(token)
                .map(RefreshTokenEntity::toDomain);
    }

    /**
     * 사용자 ID로 리프레시 토큰 조회
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByUserId(String userId) {
        return jpaRefreshTokenRepository.findByUserId(userId)
                .map(RefreshTokenEntity::toDomain);
    }

    /**
     * 토큰으로 삭제
     */
    @Override
    public void deleteByToken(String token) {
        jpaRefreshTokenRepository.deleteByToken(token);
    }

    /**
     * 사용자 ID로 삭제
     */
    @Override
    public void deleteByUserId(String userId) {
        jpaRefreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * 현재 시점 기준 만료된 리프레시 토큰 일괄 삭제
     */
    @Override
    public void deleteExpiredTokens() {
        jpaRefreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}