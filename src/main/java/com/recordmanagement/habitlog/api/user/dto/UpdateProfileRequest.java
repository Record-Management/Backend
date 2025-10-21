package com.recordmanagement.habitlog.api.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Schema(description = "프로필 업데이트 요청")
public record UpdateProfileRequest(
    @Schema(description = "닉네임 (선택적, 1-6글자, 한글/영문/숫자만)", example = "홍길동")
    @Size(min = 1, max = 6, message = "닉네임은 1-6글자만 가능합니다")
    @Pattern(
        regexp = "^[가-힣a-zA-Z0-9]+$", 
        message = "닉네임은 한글, 영문, 숫자만 가능하며 공백이나 특수기호는 사용할 수 없습니다"
    )
    String nickname,

    @Schema(description = "생년월일 (선택적, YYYY-MM-DD 형식)", example = "1990-01-01")
    LocalDate birthDate
) {
}