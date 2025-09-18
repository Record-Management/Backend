package com.recordmanagement.habitlog.application.exercise.dto;

import com.recordmanagement.habitlog.domain.exercise.model.ExerciseType;
import com.recordmanagement.habitlog.domain.user.model.UserId;

import java.time.LocalDate;
import java.util.List;

public record CreateExerciseRecordCommand(
    UserId userId,
    ExerciseType exerciseType,
    Integer caloriesBurned,
    Integer exerciseTimeMinutes,
    Integer stepCount,
    Double weight,
    String dailyNote,
    List<String> imageUrls,
    LocalDate recordDate
) {}