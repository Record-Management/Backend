package com.recordmanagement.habitlog.domain.goal.application.dto;

import com.recordmanagement.habitlog.domain.goal.domain.model.Goal;
import com.recordmanagement.habitlog.domain.goal.domain.model.TreeStage;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 목표 달성 보고서 응답 DTO
 * 
 * recentHistory는 종료일(endDate) 기준 내림차순으로 정렬되어
 * 가장 최근 완료된 목표를 첫 번째로 제공합니다.
 *
 * @author 전우선
 * @since 2025.11.04
 * @version 1.1.0 (recentHistory 종료일 기준 정렬 추가)
 */
@Getter
@RequiredArgsConstructor
public class GoalAchievementReportResponse {

    private final CurrentPeriodInfo currentPeriod;
    private final long cumulativeAchievementCount;
    private final List<GoalHistoryInfo> recentHistory;

    /**
     * 현재 기간 목표 정보
     */
    @Getter
    @RequiredArgsConstructor
    public static class CurrentPeriodInfo {
        private final String goalId;
        private final RecordType recordType;
        private final int goalDays;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final int completedDays;
        private final double achievementRate;
        private final TreeStage treeStage;
        private final boolean isInProgress;

        public static CurrentPeriodInfo from(Goal goal) {
            return new CurrentPeriodInfo(
                    goal.getId().getValue(),
                    goal.getRecordType(),
                    goal.getGoalDays(),
                    goal.getStartDate(),
                    goal.getEndDate(),
                    goal.getCompletedDays(),
                    goal.getAchievementRate(),
                    goal.getCurrentTreeStage(),
                    goal.isInProgress()
            );
        }
    }

    /**
     * 목표 이력 정보
     */
    @Getter
    @RequiredArgsConstructor
    public static class GoalHistoryInfo {
        private final String goalId;
        private final RecordType recordType;
        private final int goalDays;
        private final LocalDate startDate;
        private final LocalDate endDate;
        private final int completedDays;
        private final double achievementRate;
        private final TreeStage finalTreeStage;
        private final String status;

        public static GoalHistoryInfo from(Goal goal) {
            return new GoalHistoryInfo(
                    goal.getId().getValue(),
                    goal.getRecordType(),
                    goal.getGoalDays(),
                    goal.getStartDate(),
                    goal.getEndDate(),
                    goal.getCompletedDays(),
                    goal.getAchievementRate(),
                    goal.getCurrentTreeStage(),
                    goal.getStatus().getDisplayName()
            );
        }
    }

    /**
     * Goal 도메인들로부터 응답 DTO 생성
     *
     * @param currentGoal 현재 목표 (null 가능)
     * @param cumulativeCount 누적 달성 횟수
     * @param recentGoals 최근 목표 이력 (종료일 기준 내림차순)
     * @return 응답 DTO
     */
    public static GoalAchievementReportResponse from(Goal currentGoal, long cumulativeCount, List<Goal> recentGoals) {
        CurrentPeriodInfo currentPeriod = currentGoal != null ? CurrentPeriodInfo.from(currentGoal) : null;
        
        List<GoalHistoryInfo> historyInfos = recentGoals.stream()
                .map(GoalHistoryInfo::from)
                .toList();

        return new GoalAchievementReportResponse(currentPeriod, cumulativeCount, historyInfos);
    }
}