package com.recordmanagement.habitlog.domain.goal.infrastructure.repository;

import com.recordmanagement.habitlog.domain.goal.domain.model.Goal;
import com.recordmanagement.habitlog.domain.goal.domain.model.GoalId;
import com.recordmanagement.habitlog.domain.goal.domain.model.GoalStatus;
import com.recordmanagement.habitlog.domain.goal.domain.repository.GoalRepository;
import com.recordmanagement.habitlog.domain.goal.infrastructure.entity.GoalEntity;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 목표 Repository 구현체
 *
 * @author 전우선
 * @since 2025.11.04
 * @version 1.0.0
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class GoalRepositoryImpl implements GoalRepository {

    private final GoalJpaRepository goalJpaRepository;

    @Override
    public Goal save(Goal goal) {
        GoalEntity entity = GoalEntity.from(goal);
        GoalEntity saved = goalJpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Goal> findById(GoalId goalId) {
        return goalJpaRepository.findById(goalId.getValue())
                .map(GoalEntity::toDomain);
    }

    @Override
    public Optional<Goal> findByIdAndUserId(GoalId goalId, UserId userId) {
        return goalJpaRepository.findByGoalIdAndUserId(goalId.getValue(), userId.getValue())
                .map(GoalEntity::toDomain);
    }

    @Override
    public Optional<Goal> findCurrentGoalByUserId(UserId userId) {
        // 중복 IN_PROGRESS 목표 문제를 처리하기 위해 전체 조회 후 필터링
        List<GoalEntity> inProgressGoals = goalJpaRepository.findAllByUserIdAndStatus(
                userId.getValue(),
                GoalStatus.IN_PROGRESS);

        // 현재 날짜 기준으로 유효한 목표만 필터링
        LocalDate now = LocalDate.now();
        List<GoalEntity> validGoals = inProgressGoals.stream()
                .filter(goal -> !goal.getEndDate().isBefore(now))
                .toList();

        if (validGoals.isEmpty()) {
            return Optional.empty();
        }

        if (validGoals.size() > 1) {
            log.warn("사용자 {}에게 IN_PROGRESS 상태의 목표가 {}개 존재합니다. 가장 최근 목표를 반환합니다.",
                    userId.getValue(), validGoals.size());

            // 가장 최근에 생성된 목표 반환 (createdAt 기준 내림차순)
            return validGoals.stream()
                    .sorted((g1, g2) -> g2.getCreatedAt().compareTo(g1.getCreatedAt()))
                    .findFirst()
                    .map(GoalEntity::toDomain);
        }

        return Optional.of(validGoals.get(0).toDomain());
    }

    @Override
    public List<Goal> findByUserIdOrderByCreatedAtDesc(UserId userId) {
        return goalJpaRepository.findByUserIdOrderByCreatedAtDesc(userId.getValue())
                .stream()
                .map(GoalEntity::toDomain)
                .toList();
    }

    @Override
    public List<Goal> findByUserIdOrderByEndDateDesc(UserId userId) {
        return goalJpaRepository.findByUserIdOrderByEndDateDesc(userId.getValue())
                .stream()
                .map(GoalEntity::toDomain)
                .toList();
    }

    @Override
    public List<Goal> findByUserIdAndStatus(UserId userId, GoalStatus status) {
        return goalJpaRepository.findAllByUserIdAndStatus(userId.getValue(), status)
                .stream()
                .map(GoalEntity::toDomain)
                .toList();
    }

    @Override
    public long countCompletedGoalsByUserId(UserId userId) {
        return goalJpaRepository.countByUserIdAndStatus(userId.getValue(), GoalStatus.COMPLETED);
    }

    @Override
    public void deleteById(GoalId goalId) {
        goalJpaRepository.deleteById(goalId.getValue());
    }
}