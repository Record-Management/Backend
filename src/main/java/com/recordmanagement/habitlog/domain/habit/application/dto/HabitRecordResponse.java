package com.recordmanagement.habitlog.domain.habit.application.dto;

import com.recordmanagement.habitlog.domain.habit.domain.model.HabitType;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record HabitRecordResponse(
    // 공통 필드
    String id,
    RecordType type,
    LocalDate recordDate,
    LocalTime recordTime,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    
    // 습관기록 전용 필드
    HabitType habitType,
    boolean notificationEnabled,
    LocalTime notificationTime,
    String memo,
    boolean isCompleted,
    boolean isMainRecord
) {}