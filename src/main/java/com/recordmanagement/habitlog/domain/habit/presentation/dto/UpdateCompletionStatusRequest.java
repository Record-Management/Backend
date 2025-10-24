package com.recordmanagement.habitlog.api.habit.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateCompletionStatusRequest(
    @NotNull(message = "완료 상태는 필수입니다")
    Boolean isCompleted
) {}