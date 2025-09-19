package com.recordmanagement.habitlog.application.exercise.dto;

import com.recordmanagement.habitlog.domain.exercise.model.ExerciseType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ExerciseRecordResponse(
    String id,
    ExerciseType exerciseType,
    Integer caloriesBurned,
    Integer exerciseTimeMinutes,
    Integer stepCount,
    Double weight,
    String dailyNote,
    List<String> imageUrls,
    LocalDate recordDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    
    /**
     * 새로운 이미지 URL로 업데이트된 ExerciseRecordResponse 반환
     */
    public ExerciseRecordResponse withUpdatedImageUrls(List<String> newImageUrls) {
        return new ExerciseRecordResponse(
            this.id,
            this.exerciseType,
            this.caloriesBurned,
            this.exerciseTimeMinutes,
            this.stepCount,
            this.weight,
            this.dailyNote,
            newImageUrls,
            this.recordDate,
            this.createdAt,
            this.updatedAt
        );
    }
}