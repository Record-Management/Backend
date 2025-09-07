package com.recordmanagement.habitlog.application.record.dto;

import com.recordmanagement.habitlog.config.exception.CustomException;
import com.recordmanagement.habitlog.config.exception.ErrorCode;
import com.recordmanagement.habitlog.domain.user.model.RecordType;
import com.recordmanagement.habitlog.domain.user.model.UserId;

import java.time.LocalDate;
import java.util.List;

public record CreateRecordCommand(
    UserId userId,
    RecordType type,
    String emotion,
    String content,
    List<String> imageUrls,
    LocalDate recordDate
) {
    
    public CreateRecordCommand {
        if (userId == null) {
            throw new CustomException(ErrorCode.USER_ID_NULL_OR_EMPTY);
        }
        if (type == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (content == null || content.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (content.length() > 1000) {
            throw new CustomException(ErrorCode.VALIDATION_FAIL);
        }
        if (recordDate == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (imageUrls == null) {
            imageUrls = List.of();
        }
        if (imageUrls.size() > 3) {
            throw new CustomException(ErrorCode.FILE_COUNT_EXCEEDED);
        }
    }
}