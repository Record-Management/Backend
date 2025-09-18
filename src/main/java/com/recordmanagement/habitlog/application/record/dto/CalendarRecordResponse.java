package com.recordmanagement.habitlog.application.record.dto;

import com.recordmanagement.habitlog.domain.record.model.Record;
import com.recordmanagement.habitlog.domain.user.model.RecordType;

import java.time.LocalDate;
import java.util.List;

public record CalendarRecordResponse(
    LocalDate date,
    List<RecordSummary> records
) {
    
    public static CalendarRecordResponse of(LocalDate date, List<Record> records) {
        List<RecordSummary> summaries = records.stream()
            .map(RecordSummary::from)
            .toList();
        return new CalendarRecordResponse(date, summaries);
    }
    
    public record RecordSummary(
        String id,
        RecordType type
    ) {
        public static RecordSummary from(Record record) {
            return new RecordSummary(
                record.getId().value(),
                record.getType()
            );
        }
    }
}