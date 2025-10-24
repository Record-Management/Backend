package com.recordmanagement.habitlog.domain.record.application.dto;

import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import com.recordmanagement.habitlog.domain.record.domain.model.RecordId;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;

import java.util.List;

public record UpdateRecordCommand(
    RecordId recordId,
    UserId userId,
    RecordType type,
    String emotion,
    String content,
    List<String> imageUrls
) {
    
    public UpdateRecordCommand {
        if (recordId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
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
        if (imageUrls == null) {
            imageUrls = List.of();
        }
        if (imageUrls.size() > 3) {
            throw new CustomException(ErrorCode.FILE_COUNT_EXCEEDED);
        }
    }
}