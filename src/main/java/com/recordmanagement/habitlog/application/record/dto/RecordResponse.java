package com.recordmanagement.habitlog.application.record.dto;

import com.recordmanagement.habitlog.domain.record.model.Record;
import com.recordmanagement.habitlog.domain.user.model.RecordType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record RecordResponse(
    String id,
    RecordType type,
    String emotion,
    String content,
    List<String> imageUrls,
    LocalDate recordDate,
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
            record.getCreatedAt(),
            record.getUpdatedAt()
        );
    }
}