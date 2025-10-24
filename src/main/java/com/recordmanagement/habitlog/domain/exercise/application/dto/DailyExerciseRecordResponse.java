package com.recordmanagement.habitlog.domain.exercise.application.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyExerciseRecordResponse(
    LocalDate date,
    List<ExerciseRecordResponse> exerciseRecords
) {}