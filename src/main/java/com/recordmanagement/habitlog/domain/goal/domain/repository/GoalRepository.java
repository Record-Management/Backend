package com.recordmanagement.habitlog.domain.goal.domain.repository;

import com.recordmanagement.habitlog.domain.goal.domain.model.Goal;
import com.recordmanagement.habitlog.domain.goal.domain.model.GoalId;
import com.recordmanagement.habitlog.domain.goal.domain.model.GoalStatus;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;

import java.util.List;
import java.util.Optional;

/**
 * 목표 Repository 인터페이스
 *
 * @author 전우선
 * @since 2025.11.04
 * @version 1.0.0
 */
public interface GoalRepository {

    /**
     * 목표 저장
     *
     * @param goal 목표
     * @return 저장된 목표
     */
    Goal save(Goal goal);

    /**
     * 목표 ID로 조회
     *
     * @param goalId 목표 ID
     * @return 목표
     */
    Optional<Goal> findById(GoalId goalId);

    /**
     * 사용자 ID와 목표 ID로 조회
     *
     * @param goalId 목표 ID
     * @param userId 사용자 ID
     * @return 목표
     */
    Optional<Goal> findByIdAndUserId(GoalId goalId, UserId userId);

    /**
     * 사용자의 현재 유효한 목표 조회 (진행중이면서 기간 내)
     *
     * @param userId 사용자 ID
     * @return 현재 유효한 목표 (기간 지난 목표는 제외)
     */
    Optional<Goal> findCurrentGoalByUserId(UserId userId);

    /**
     * 사용자의 목표 이력 조회 (최신순)
     *
     * @param userId 사용자 ID
     * @return 목표 이력
     */
    List<Goal> findByUserIdOrderByCreatedAtDesc(UserId userId);

    /**
     * 사용자의 특정 상태 목표들 조회
     *
     * @param userId 사용자 ID
     * @param status 목표 상태
     * @return 목표 목록
     */
    List<Goal> findByUserIdAndStatus(UserId userId, GoalStatus status);

    /**
     * 사용자의 완료된 목표 개수 조회
     *
     * @param userId 사용자 ID
     * @return 완료된 목표 개수
     */
    long countCompletedGoalsByUserId(UserId userId);

    /**
     * 목표 삭제
     *
     * @param goalId 목표 ID
     */
    void deleteById(GoalId goalId);
}