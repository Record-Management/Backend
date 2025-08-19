package com.recordmanagement.habitlog.application.calendar.dto;

import com.recordmanagement.habitlog.domain.record.model.DailyRecord;
import com.recordmanagement.habitlog.domain.record.model.ScheduleRecord;
import com.recordmanagement.habitlog.domain.record.model.HabitRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@Schema(description = "특정 날짜의 모든 기록 응답")
public class DailyRecordsResponse {
    
    @Schema(description = "조회 날짜")
    private final LocalDate date;
    
    @Schema(description = "일상 기록")
    private final DailyRecord dailyRecord;
    
    @Schema(description = "일정 기록 목록")
    private final List<ScheduleRecord> scheduleRecords;
    
    @Schema(description = "습관 기록 목록")
    private final List<HabitRecord> habitRecords;
}