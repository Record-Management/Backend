package com.recordmanagement.habitlog.application.exercise.dto;

import com.recordmanagement.habitlog.domain.exercise.model.ExerciseRecordId;
import com.recordmanagement.habitlog.domain.exercise.model.ExerciseType;
import com.recordmanagement.habitlog.domain.user.model.UserId;

import java.time.LocalTime;
import java.util.List;

public record UpdateExerciseRecordCommand(
    ExerciseRecordId exerciseRecordId,
    UserId userId,
    ExerciseType exerciseType,
    Integer caloriesBurned,
    Integer exerciseTimeMinutes,
    Integer stepCount,
    Double weight,
    String dailyNote,
    List<String> imageUrls,
    LocalTime recordTime
) {}