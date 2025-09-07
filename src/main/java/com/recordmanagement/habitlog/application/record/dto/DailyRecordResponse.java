package com.recordmanagement.habitlog.application.record.dto;

import com.recordmanagement.habitlog.domain.record.model.Record;

import java.time.LocalDate;
import java.util.List;

public record DailyRecordResponse(
    LocalDate date,
    List<RecordResponse> records
) {
    
    public static DailyRecordResponse of(LocalDate date, List<Record> records) {
        List<RecordResponse> recordResponses = records.stream()
            .map(RecordResponse::from)
            .sorted((a, b) -> b.createdAt().compareTo(a.createdAt())) // 최신순
            .toList();
        return new DailyRecordResponse(date, recordResponses);
    }
}