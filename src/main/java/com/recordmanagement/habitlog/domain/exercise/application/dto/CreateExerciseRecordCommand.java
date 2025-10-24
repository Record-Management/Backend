package com.recordmanagement.habitlog.domain.exercise.application.dto;

import com.recordmanagement.habitlog.domain.exercise.domain.model.ExerciseType;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;

import java.time.LocalDate;
import java.time.LocalTime;
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
    LocalDate recordDate,
    LocalTime recordTime
) {}