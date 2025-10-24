package com.recordmanagement.habitlog.domain.auth.infrastructure.auth.repository;

import com.recordmanagement.habitlog.domain.auth.infrastructure.entity.BlacklistedTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

/**
 * 블랙리스트 토큰 JPA 레포지토리
 * 
 * 블랙리스트된 토큰의 데이터베이스 접근을 담당합니다.
 * 대용량 토큰 처리를 위한 최적화된 쿼리를 제공합니다.
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0
 */
public interface JpaBlacklistedTokenRepository extends JpaRepository<BlacklistedTokenEntity, String> {
    
    /**
     * 토큰 ID 존재 여부 확인
     * 
     * @param tokenId 확인할 토큰 ID
     * @return true: 존재함(블랙리스트됨), false: 존재하지 않음
     */
    boolean existsByTokenId(String tokenId);
    
    /**
     * 특정 사용자의 모든 활성 토큰을 만료 처리하기 위한 벌크 삽입
     * 실제로는 사용자별 토큰 무효화를 위해 별도 메서드에서 처리
     * 
     * @param userId 사용자 ID
     * @return 삭제된 레코드 수
     */
    long deleteByUserId(String userId);
    
    /**
     * 만료된 블랙리스트 토큰 일괄 삭제
     * 
     * @param now 현재 시간
     * @return 삭제된 레코드 수
     */
    @Modifying
    @Query("DELETE FROM BlacklistedTokenEntity b WHERE b.expiresAt < :now")
    int deleteByExpiresAtBefore(@Param("now") LocalDateTime now);
    
    /**
     * 특정 사용자의 블랙리스트 토큰 개수 조회
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자의 블랙리스트 토큰 개수
     */
    long countByUserId(String userId);
}