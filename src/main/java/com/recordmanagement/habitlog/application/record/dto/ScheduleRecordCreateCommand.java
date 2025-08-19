package com.recordmanagement.habitlog.application.record.dto;

import com.recordmanagement.habitlog.domain.record.model.ScheduleType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@RequiredArgsConstructor
public class ScheduleRecordCreateCommand {
    private final String userId;
    private final String title;
    private final ScheduleType scheduleType;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String memo;
}