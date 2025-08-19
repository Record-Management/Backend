package com.recordmanagement.habitlog.application.record.dto;

import com.recordmanagement.habitlog.domain.record.model.HabitType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class HabitRecordCreateCommand {
    private final String userId;
    private final LocalDate recordDate;
    private final HabitType habitType;
    private final boolean completed;
    private final String memo;
}