package com.recordmanagement.habitlog.domain.goal.infrastructure.repository;

import com.recordmanagement.habitlog.domain.goal.domain.model.GoalStatus;
import com.recordmanagement.habitlog.domain.goal.infrastructure.entity.GoalEntity;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
     * 사용자의 목표 이력 조회 (종료일 기준 최신순)
     *
     * @param userId 사용자 ID
     * @return 목표 이력 (endDate 기준 내림차순)
     */
    List<GoalEntity> findByUserIdOrderByEndDateDesc(String userId);

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

    /**
     * 만료된 IN_PROGRESS 목표 ID 목록 조회 (스케줄러용)
     *
     * @param endDate 기준 날짜 (이 날짜까지가 기간인 목표들)
     * @return 만료된 목표 ID 목록
     */
    @Query("SELECT g.goalId FROM GoalEntity g WHERE g.status = 'IN_PROGRESS' AND g.endDate <= :endDate")
    List<String> findExpiredInProgressGoalIds(@Param("endDate") LocalDate endDate);

    /**
     * 목표 상태를 COMPLETED로 업데이트 (스케줄러용)
     *
     * @param goalId 목표 ID
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE GoalEntity g SET g.status = 'COMPLETED' WHERE g.goalId = :goalId")
    int updateGoalStatusToCompleted(@Param("goalId") String goalId);

    /**
     * 목표 ID로 사용자 ID 조회 (스케줄러용)
     *
     * @param goalId 목표 ID
     * @return 사용자 ID
     */
    @Query("SELECT g.userId FROM GoalEntity g WHERE g.goalId = :goalId")
    String findUserIdByGoalId(@Param("goalId") String goalId);

    /**
     * 사용자의 진행중인 목표 존재 여부 확인 (스케줄러용)
     *
     * @param userId 사용자 ID
     * @param currentDate 현재 날짜
     * @return 진행중인 목표 존재 여부
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END " +
           "FROM GoalEntity g WHERE g.userId = :userId AND g.status = 'IN_PROGRESS' AND g.endDate >= :currentDate")
    boolean hasActiveGoalsByUserId(@Param("userId") String userId, @Param("currentDate") LocalDate currentDate);

    /**
     * 목표 ID로 기록 타입 조회 (스케줄러용)
     *
     * @param goalId 목표 ID
     * @return 기록 타입
     */
    @Query("SELECT g.recordType FROM GoalEntity g WHERE g.goalId = :goalId")
    RecordType findRecordTypeByGoalId(@Param("goalId") String goalId);
}