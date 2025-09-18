package com.recordmanagement.habitlog.application.record.dto;

import java.time.YearMonth;
import java.util.List;

public record CalendarResponse(
    int year,
    int month,
    List<CalendarRecordResponse> monthlyRecords
) {
    
    public static CalendarResponse of(YearMonth yearMonth, List<CalendarRecordResponse> monthlyRecords) {
        return new CalendarResponse(
            yearMonth.getYear(),
            yearMonth.getMonthValue(),
            monthlyRecords
        );
    }
}