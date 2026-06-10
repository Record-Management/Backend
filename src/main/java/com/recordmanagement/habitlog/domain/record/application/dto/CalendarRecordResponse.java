package com.recordmanagement.habitlog.domain.record.application.dto;

import com.recordmanagement.habitlog.domain.record.domain.model.Record;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;

import java.time.LocalDate;
import java.util.List;

public record CalendarRecordResponse(
    LocalDate date,
    RecordType mainRecordTypeForDate,
    List<RecordSummary> records,
    ScheduleSummary schedules
) {

    public static CalendarRecordResponse of(LocalDate date, RecordType mainRecordTypeForDate, List<Record> records, ScheduleSummary schedules) {
        List<RecordSummary> summaries = records.stream()
            .map(RecordSummary::from)
            .toList();
        return new CalendarRecordResponse(date, mainRecordTypeForDate, summaries, schedules);
    }
    
    public record RecordSummary(
        String id,
        RecordType type,
        Boolean isCompleted
    ) {
        public static RecordSummary from(Record record) {
            return new RecordSummary(
                record.getId().value(),
                record.getType(),
                true // 일상 기록 존재 자체가 완료
            );
        }
        
        public static RecordSummary from(UnifiedRecordResponse unified) {
            return new RecordSummary(
                unified.id(),
                unified.type(),
                unified.isCompleted()
            );
        }
    }
}