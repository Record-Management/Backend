package com.recordmanagement.habitlog.domain.goal.infrastructure.repository;

import com.recordmanagement.habitlog.domain.goal.domain.model.GoalStatus;
import com.recordmanagement.habitlog.domain.goal.infrastructure.entity.GoalEntity;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 목표 JPA Repository
 *
 * @author 전우선
 * @since 2025.11.04
 * @version 1.0.0
 */
public interface GoalJpaRepository extends JpaRepository<GoalEntity, String> {

    /**
     * 사용자 ID와 목표 ID로 조회
     *
     * @param goalId 목표 ID
     * @param userId 사용자 ID
     * @return 목표 엔티티
     */
    Optional<GoalEntity> findByGoalIdAndUserId(String goalId, String userId);

    /**
     * 사용자의 현재 진행중인 목표 조회
     *
     * @param userId 사용자 ID
     * @param status 진행중 상태
     * @return 진행중인 목표
     */
    Optional<GoalEntity> findByUserIdAndStatus(String userId, GoalStatus status);

    /**
     * 사용자의 현재 유효한 목표 조회 (진행중이면서 기간 내)
     *
     * @param userId 사용자 ID
     * @param status 진행중 상태
     * @param currentDate 현재 날짜
     * @return 현재 유효한 목표
     */
    @Query("SELECT g FROM GoalEntity g WHERE g.userId = :userId AND g.status = :status AND g.endDate >= :currentDate")
    Optional<GoalEntity> findCurrentValidGoalByUserId(@Param("userId") String userId, 
                                                     @Param("status") GoalStatus status, 
                                                     @Param("currentDate") LocalDate currentDate);

    /**
     * 사용자의 목표 이력 조회 (최신순)
     *
     * @param userId 사용자 ID
     * @return 목표 이력
     */
    List<GoalEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * 사용자의 특정 상태 목표들 조회
     *
     * @param userId 사용자 ID
     * @param status 목표 상태
     * @return 목표 목록
     */
    List<GoalEntity> findAllByUserIdAndStatus(String userId, GoalStatus status);

    /**
     * 사용자의 완료된 목표 개수 조회
     *
     * @param userId 사용자 ID
     * @param status 완료 상태
     * @return 완료된 목표 개수
     */
    long countByUserIdAndStatus(String userId, GoalStatus status);
}