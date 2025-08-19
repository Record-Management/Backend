package com.recordmanagement.habitlog.application.record.dto;

import com.recordmanagement.habitlog.domain.record.model.ExerciseType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

/**
 * 운동 기록 생성 커맨드
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "운동 기록 생성 커맨드")
public class ExerciseRecordCreateCommand {
    
    @Schema(description = "사용자 ID")
    private final String userId;
    
    @Schema(description = "기록 날짜")
    private final LocalDate recordDate;
    
    @Schema(description = "운동 타입")
    private final ExerciseType exerciseType;
    
    @Schema(description = "칼로리", example = "300")
    private final Integer calories;
    
    @Schema(description = "운동 시간 (분)", example = "60")
    private final Integer durationMinutes;
    
    @Schema(description = "몸무게 (kg)", example = "70.5")
    private final Double weight;
    
    @Schema(description = "걸음수", example = "8000")
    private final Integer steps;
    
    @Schema(description = "메모")
    private final String memo;
}