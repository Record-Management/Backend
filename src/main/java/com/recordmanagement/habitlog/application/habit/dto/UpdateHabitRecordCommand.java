package com.recordmanagement.habitlog.application.habit.dto;

import com.recordmanagement.habitlog.domain.habit.model.HabitType;
import com.recordmanagement.habitlog.domain.user.model.UserId;

import java.time.LocalTime;

public record UpdateHabitRecordCommand(
    UserId userId,
    HabitType habitType,
    boolean notificationEnabled,
    LocalTime notificationTime,
    String memo,
    Boolean isMainRecord
) {}