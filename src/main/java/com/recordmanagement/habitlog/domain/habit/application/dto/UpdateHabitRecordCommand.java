package com.recordmanagement.habitlog.domain.habit.application.dto;

import com.recordmanagement.habitlog.domain.habit.domain.model.HabitType;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;

import java.time.LocalTime;

public record UpdateHabitRecordCommand(
    UserId userId,
    HabitType habitType,
    boolean notificationEnabled,
    LocalTime notificationTime,
    String memo,
    Boolean isMainRecord
) {}