package com.recordmanagement.habitlog.domain.goal.infrastructure.repository;

import com.recordmanagement.habitlog.domain.goal.domain.model.Goal;
import com.recordmanagement.habitlog.domain.goal.domain.model.GoalId;
import com.recordmanagement.habitlog.domain.goal.domain.model.GoalStatus;
import com.recordmanagement.habitlog.domain.goal.domain.repository.GoalRepository;
import com.recordmanagement.habitlog.domain.goal.infrastructure.entity.GoalEntity;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 목표 Repository 구현체
 *
 * @author 전우선
 * @since 2025.11.04
 * @version 1.0.0
 */
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
        return goalJpaRepository.findByUserIdAndStatus(userId.getValue(), GoalStatus.IN_PROGRESS)
                .map(GoalEntity::toDomain);
    }

    @Override
    public List<Goal> findByUserIdOrderByCreatedAtDesc(UserId userId) {
        return goalJpaRepository.findByUserIdOrderByCreatedAtDesc(userId.getValue())
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