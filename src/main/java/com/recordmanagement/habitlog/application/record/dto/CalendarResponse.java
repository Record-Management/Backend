package com.recordmanagement.habitlog.application.record.dto;

import java.time.YearMonth;
import java.util.List;

public record CalendarResponse(
    int year,
    int month,
    List<CalendarRecordResponse> dailyRecords
) {
    
    public static CalendarResponse of(YearMonth yearMonth, List<CalendarRecordResponse> dailyRecords) {
        return new CalendarResponse(
            yearMonth.getYear(),
            yearMonth.getMonthValue(),
            dailyRecords
        );
    }
}