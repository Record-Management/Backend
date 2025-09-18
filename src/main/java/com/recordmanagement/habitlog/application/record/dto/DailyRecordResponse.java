package com.recordmanagement.habitlog.application.record.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyRecordResponse(
    LocalDate date,
    List<UnifiedRecordResponse> records
) {
    
    public static DailyRecordResponse of(LocalDate date, List<UnifiedRecordResponse> records) {
        List<UnifiedRecordResponse> sortedRecords = records.stream()
            .sorted((a, b) -> b.createdAt().compareTo(a.createdAt())) // 최신순
            .toList();
        return new DailyRecordResponse(date, sortedRecords);
    }
}