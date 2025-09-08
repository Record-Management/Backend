package com.recordmanagement.habitlog.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * FCM 토큰 업데이트 요청 DTO
 * 
 * @since 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "FCM 토큰 업데이트 요청")
public class FcmTokenUpdateRequest {

    @NotBlank(message = "FCM 토큰은 필수입니다.")
    @Schema(description = "FCM 토큰", example = "dGhpcyBpcyBhIGZha2UgZmNtIHRva2VuLi4u", required = true)
    private String fcmToken;
}