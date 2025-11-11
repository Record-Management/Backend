package com.recordmanagement.habitlog.domain.goal.application.dto;

import com.recordmanagement.habitlog.domain.goal.domain.model.Goal;
import com.recordmanagement.habitlog.domain.goal.domain.model.TreeStage;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

/**
 * 현재 목표 응답 DTO
 *
 * @author 전우선
 * @since 2025.11.04
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public class CurrentGoalResponse {

    private final String goalId;
    private final RecordType recordType;
    private final int goalDays;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int completedDays;
    private final double achievementRate;
    private final TreeStage treeStage;
    private final boolean canCreateNew;

    /**
     * Goal 도메인으로부터 응답 DTO 생성
     *
     * @param goal 목표 도메인
     * @param canCreateNew 새 목표 생성 가능 여부
     * @return 응답 DTO
     */
    public static CurrentGoalResponse from(Goal goal, boolean canCreateNew) {
        return new CurrentGoalResponse(
                goal.getId().getValue(),
                goal.getRecordType(),
                goal.getGoalDays(),
                goal.getStartDate(),
                goal.getEndDate(),
                goal.getCompletedDays(),
                goal.getAchievementRate(),
                goal.getCurrentTreeStage(),
                canCreateNew
        );
    }

}