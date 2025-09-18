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
) {}