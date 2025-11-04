package com.recordmanagement.habitlog.domain.goal.application.dto;

import com.recordmanagement.habitlog.domain.goal.domain.model.Goal;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

/**
 * 목표 생성 응답 DTO
 *
 * @author 전우선
 * @since 2025.11.04
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public class CreateGoalResponse {

    private final String goalId;
    private final RecordType recordType;
    private final int goalDays;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String message;

    /**
     * Goal 도메인으로부터 응답 DTO 생성
     *
     * @param goal 생성된 목표
     * @return 응답 DTO
     */
    public static CreateGoalResponse from(Goal goal) {
        return new CreateGoalResponse(
                goal.getId().getValue(),
                goal.getRecordType(),
                goal.getGoalDays(),
                goal.getStartDate(),
                goal.getEndDate(),
                "목표가 성공적으로 생성되었습니다."
        );
    }
}