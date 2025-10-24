package com.recordmanagement.habitlog.domain.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 리프레시 토큰 갱신 요청을 전달하는 커맨드 DTO
 * 
 * 액세스 토큰 만료 시 새로운 액세스 토큰 발급 요청에 사용됩니다.
 * 리프레시 토큰 정보를 캡슐화하고 불변성을 보장합니다.
 * 클라이언트의 끊김 없는 인증 상태 유지를 지원합니다.
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Schema(description = "리프레시 토큰 갱신 명령 DTO")
public record RefreshTokenCommand(
        @Schema(description = "갱신할 리프레시 토큰 문자열", 
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken
) {
    /**
     * 리프레시 토큰 갱신 커맨드 생성 팩토리 메서드
     *
     * @param refreshToken 리프레시 토큰 문자열
     * @return RefreshTokenCommand 객체
     */
    public static RefreshTokenCommand of(String refreshToken) {
        return new RefreshTokenCommand(refreshToken);
    }
}