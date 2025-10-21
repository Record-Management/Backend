package com.recordmanagement.habitlog.application.user.dto;

import java.time.LocalDate;

public record UpdateProfileCommand(
    String userId,
    String nickname,
    LocalDate birthDate
) {
}