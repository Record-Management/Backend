package com.recordmanagement.habitlog.domain.schedule.domain.model;

import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import java.util.UUID;

public record ScheduleRecordId(String value) {

    public ScheduleRecordId {
        if (value == null || value.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    public static ScheduleRecordId generate() {
        return new ScheduleRecordId(UUID.randomUUID().toString());
    }

    public static ScheduleRecordId from(String value) {
        return new ScheduleRecordId(value);
    }
}
