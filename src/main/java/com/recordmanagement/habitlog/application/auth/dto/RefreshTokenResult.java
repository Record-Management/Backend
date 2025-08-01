package com.recordmanagement.habitlog.application.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 리프레시 토큰 갱신 처리 결과를 나타내는 DTO
 * 새로 발급된 액세스 토큰과 토큰 만료까지 남은 시간을 포함
 * 클라이언트는 새 액세스 토큰 저장 및 갱신 타이밍 계산에 활용
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
@Builder
@Schema(description = "리프레시 토큰 갱신 결과 DTO")
public class RefreshTokenResult {

    @Schema(description = "새로 발급된 JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String accessToken;

    @Schema(description = "토큰 만료까지 남은 시간(초)", example = "3600")
    private final Long expiresIn;

    /**
     * 갱신 결과 객체 생성 팩토리 메서드
     *
     * @param accessToken 새 액세스 토큰 문자열
     * @param expiresIn 토큰 만료까지 남은 시간(초)
     * @return RefreshTokenResult 객체
     */
    public static RefreshTokenResult of(String accessToken, Long expiresIn) {
        return RefreshTokenResult.builder()
                .accessToken(accessToken)
                .expiresIn(expiresIn)
                .build();
    }
}
