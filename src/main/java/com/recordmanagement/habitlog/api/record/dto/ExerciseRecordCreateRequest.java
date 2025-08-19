package com.recordmanagement.habitlog.api.record.dto;

import com.recordmanagement.habitlog.domain.record.model.ExerciseType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 운동 기록 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@Schema(description = "운동 기록 생성 요청")
public class ExerciseRecordCreateRequest {
    
    @NotNull(message = "기록 날짜는 필수입니다")
    @Schema(description = "기록 날짜", example = "2025-10-05")
    private LocalDate recordDate;
    
    @NotNull(message = "운동 타입은 필수입니다")
    @Schema(description = "운동 타입")
    private ExerciseType exerciseType;
    
    @Schema(description = "칼로리", example = "300")
    private Integer calories;
    
    @Schema(description = "운동 시간 (분)", example = "60")
    private Integer durationMinutes;
    
    @Schema(description = "몸무게 (kg)", example = "70.5")
    private Double weight;
    
    @Schema(description = "걸음수", example = "8000")
    private Integer steps;
    
    @Schema(description = "메모", example = "오늘 운동 완료!")
    private String memo;
}