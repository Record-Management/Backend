package com.recordmanagement.habitlog.domain.goal.application.dto;

import com.recordmanagement.habitlog.domain.goal.domain.model.Goal;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 목표 강제 완료 응답 DTO
 * 
 * QA 테스트 시 목표 강제 완료 결과를 반환하기 위한 DTO
 *
 * @author 전우선
 * @since 2025.11.23
 * @version 1.0.0 (QA 테스트 전용)
 */
@Schema(description = "목표 강제 완료 응답")
public record CompleteGoalResponse(
        @Schema(description = "완료된 목표 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String completedGoalId,
        
        @Schema(description = "완료된 목표의 기록 타입", example = "HABIT")
        RecordType completedRecordType,
        
        @Schema(description = "완료된 목표의 일수", example = "30")
        Integer completedGoalDays,
        
        @Schema(description = "최종 달성률", example = "73.3")
        Double finalAchievementRate,
        
        @Schema(description = "새로운 목표 생성 가능 여부", example = "true")
        Boolean canCreateNewGoal,
        
        @Schema(description = "완료 메시지", example = "목표가 완료되었습니다. 달성 보고서에서 확인할 수 있습니다.")
        String message
) {
    
    /**
     * Goal 엔터티로부터 CompleteGoalResponse 생성
     *
     * @param completedGoal 완료된 목표 정보
     * @param canCreateNew 새 목표 생성 가능 여부
     * @return CompleteGoalResponse
     */
    public static CompleteGoalResponse from(Goal completedGoal, Boolean canCreateNew) {
        return new CompleteGoalResponse(
                completedGoal.getId().getValue(),
                completedGoal.getRecordType(),
                completedGoal.getGoalDays(),
                completedGoal.getAchievementRate(),
                canCreateNew,
                "목표가 완료되었습니다. 달성 보고서에서 확인할 수 있습니다."
        );
    }
}