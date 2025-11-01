package com.recordmanagement.habitlog.domain.user.presentation.dto;

import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * 목표 재설정 요청 DTO
 * 
 * 기존 사용자가 기록 타입과 목표 일수만 변경하기 위한 간소화된 요청 객체
 * 
 * @author 전우선
 * @since 2025.11.01
 * @version 1.0.0
 */
@Getter
@Schema(description = "목표 재설정 요청")
public class GoalResetRequest {

    @Schema(
        description = "메인 기록 타입",
        example = "HABIT",
        required = true,
        allowableValues = {"DAILY", "EXERCISE", "HABIT", "SCHEDULE"}
    )
    @NotNull(message = "메인 기록 타입은 필수입니다.")
    private RecordType mainRecordType;

    @Schema(
        description = "목표 일수",
        example = "10",
        required = true,
        minimum = "1",
        maximum = "365"
    )
    @NotNull(message = "목표 일수는 필수입니다.")
    @Min(value = 1, message = "목표 일수는 1일 이상이어야 합니다.")
    @Max(value = 365, message = "목표 일수는 365일을 초과할 수 없습니다.")
    private Integer goalDays;
}