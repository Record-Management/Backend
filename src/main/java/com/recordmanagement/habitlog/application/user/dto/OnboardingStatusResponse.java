package com.recordmanagement.habitlog.application.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 온보딩 상태 응답 DTO
 * 
 * - 사용자의 온보딩 완료 여부를 클라이언트에 전달
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "온보딩 상태 응답")
public class OnboardingStatusResponse {
    
    @Schema(description = "온보딩 완료 여부", example = "true")
    private final boolean onboardingCompleted;
}