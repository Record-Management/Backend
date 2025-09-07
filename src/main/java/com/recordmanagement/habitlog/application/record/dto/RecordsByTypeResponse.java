package com.recordmanagement.habitlog.application.record.dto;

import com.recordmanagement.habitlog.domain.record.model.Record;
import com.recordmanagement.habitlog.domain.user.model.RecordType;

import java.util.List;

public record RecordsByTypeResponse(
    RecordType type,
    String typeDescription,
    int totalCount,
    List<RecordResponse> records
) {
    
    public static RecordsByTypeResponse of(RecordType type, List<Record> records) {
        List<RecordResponse> recordResponses = records.stream()
            .map(RecordResponse::from)
            .sorted((a, b) -> b.createdAt().compareTo(a.createdAt())) // 최신순
            .toList();
            
        return new RecordsByTypeResponse(
            type,
            type.getDescription(),
            records.size(),
            recordResponses
        );
    }
}