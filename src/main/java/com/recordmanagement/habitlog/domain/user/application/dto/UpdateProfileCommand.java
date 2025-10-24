package com.recordmanagement.habitlog.domain.user.application.dto;

import java.time.LocalDate;

public record UpdateProfileCommand(
    String userId,
    String nickname,
    LocalDate birthDate
) {
}