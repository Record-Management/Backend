package com.recordmanagement.habitlog.domain.goal.application.dto;

import com.recordmanagement.habitlog.domain.goal.domain.model.Goal;
import com.recordmanagement.habitlog.domain.goal.domain.model.TreeStage;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 목표 달성 이력 응답 DTO
 *
 * @author 전우선
 * @since 2025.11.04
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public class GoalAchievementHistoryResponse {

    private final long totalCount;
    private final long completedCount;
    private final List<GoalHistoryItem> goals;

    /**
     * 목표 이력 항목
     */
    @Getter
    @RequiredArgsConstructor
    public static class GoalHistoryItem {
        private final String goalId;
        private final RecordType recordType;
        private final int goalDays;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final int completedDays;
        private final double achievementRate;
        private final TreeStage finalTreeStage;
        private final String status;
        private final LocalDateTime createdAt;
        private final LocalDateTime completedAt;

        public static GoalHistoryItem from(Goal goal) {
            return new GoalHistoryItem(
                    goal.getId().getValue(),
                    goal.getRecordType(),
                    goal.getGoalDays(),
                    goal.getStartDate(),
                    goal.getEndDate(),
                    goal.getCompletedDays(),
                    goal.getAchievementRate(),
                    goal.getCurrentTreeStage(),
                    goal.getStatus().getDisplayName(),
                    goal.getCreatedAt(),
                    goal.getCompletedAt()
            );
        }
    }

    /**
     * Goal 도메인들로부터 응답 DTO 생성
     *
     * @param allGoals 모든 목표 이력
     * @param completedGoals 완료된 목표들
     * @return 응답 DTO
     */
    public static GoalAchievementHistoryResponse from(List<Goal> allGoals, List<Goal> completedGoals) {
        List<GoalHistoryItem> historyItems = allGoals.stream()
                .map(GoalHistoryItem::from)
                .toList();

        return new GoalAchievementHistoryResponse(
                allGoals.size(),
                completedGoals.size(),
                historyItems
        );
    }
}