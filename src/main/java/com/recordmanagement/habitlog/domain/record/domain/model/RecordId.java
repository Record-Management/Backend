package com.recordmanagement.habitlog.domain.record.domain.model;

import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import java.util.UUID;

public record RecordId(String value) {
    
    public RecordId {
        if (value == null || value.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
    
    public static RecordId generate() {
        return new RecordId(UUID.randomUUID().toString());
    }
    
    public static RecordId from(String value) {
        return new RecordId(value);
    }
}