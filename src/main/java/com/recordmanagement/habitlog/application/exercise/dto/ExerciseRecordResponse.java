package com.recordmanagement.habitlog.application.exercise.dto;

import com.recordmanagement.habitlog.domain.user.model.RecordType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public record ExerciseRecordResponse(
    String id,
    RecordType type,
    LocalDate recordDate,
    LocalTime recordTime,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    
}