package com.recordmanagement.habitlog.application.record.dto;

import com.recordmanagement.habitlog.domain.record.model.ExerciseType;
import com.recordmanagement.habitlog.domain.record.model.ExerciseRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 운동 기록 응답 DTO
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "운동 기록 응답")
public class ExerciseRecordResponse {
    
    @Schema(description = "운동 기록 ID")
    private final String id;
    
    @Schema(description = "기록 날짜")
    private final LocalDate recordDate;
    
    @Schema(description = "운동 타입")
    private final ExerciseType exerciseType;
    
    @Schema(description = "칼로리")
    private final Integer calories;
    
    @Schema(description = "운동 시간 (분)")
    private final Integer durationMinutes;
    
    @Schema(description = "몸무게 (kg)")
    private final Double weight;
    
    @Schema(description = "걸음수")
    private final Integer steps;
    
    @Schema(description = "메모")
    private final String memo;
    
    @Schema(description = "생성 시간")
    private final LocalDateTime createdAt;
    
    @Schema(description = "수정 시간")
    private final LocalDateTime updatedAt;
    
    public static ExerciseRecordResponse from(ExerciseRecord exerciseRecord) {
        return new ExerciseRecordResponse(
                exerciseRecord.getId(),
                exerciseRecord.getRecordDate(),
                exerciseRecord.getExerciseType(),
                exerciseRecord.getCalories(),
                exerciseRecord.getDurationMinutes(),
                exerciseRecord.getWeight(),
                exerciseRecord.getSteps(),
                exerciseRecord.getMemo(),
                exerciseRecord.getCreatedAt(),
                exerciseRecord.getUpdatedAt()
        );
    }
}