package com.recordmanagement.habitlog.application.exercise.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyExerciseRecordResponse(
    LocalDate date,
    List<ExerciseRecordResponse> exerciseRecords
) {}