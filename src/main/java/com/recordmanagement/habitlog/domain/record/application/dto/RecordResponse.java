package com.recordmanagement.habitlog.domain.record.application.dto;

import com.recordmanagement.habitlog.domain.record.domain.model.Record;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record RecordResponse(
    String id,
    RecordType type,
    String emotion,
    String content,
    List<String> imageUrls,
    LocalDate recordDate,
    LocalTime recordTime,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    
    public static RecordResponse from(Record record) {
        return new RecordResponse(
            record.getId().value(),
            record.getType(),
            record.getEmotion(),
            record.getContent(),
            record.getImageUrls(),
            record.getRecordDate(),
            record.getRecordTime(),
            record.getCreatedAt(),
            record.getUpdatedAt()
        );
    }
}