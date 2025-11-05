package com.recordmanagement.habitlog.domain.record.application.dto;

import com.recordmanagement.habitlog.domain.record.domain.model.Record;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;

import java.time.LocalDate;
import java.util.List;

public record CalendarRecordResponse(
    LocalDate date,
    RecordType mainRecordTypeForDate,
    List<RecordSummary> records
) {
    
    public static CalendarRecordResponse of(LocalDate date, RecordType mainRecordTypeForDate, List<Record> records) {
        List<RecordSummary> summaries = records.stream()
            .map(RecordSummary::from)
            .toList();
        return new CalendarRecordResponse(date, mainRecordTypeForDate, summaries);
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
        
        public static RecordSummary from(UnifiedRecordResponse unified) {
            return new RecordSummary(
                unified.id(),
                unified.type()
            );
        }
    }
}