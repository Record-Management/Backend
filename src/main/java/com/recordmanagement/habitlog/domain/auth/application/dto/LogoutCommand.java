package com.recordmanagement.habitlog.domain.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 로그아웃 명령 DTO
 * 
 * 로그아웃 요청 시 필요한 정보를 캡슐화합니다.
 * 단일 기기 로그아웃과 전체 기기 로그아웃을 지원합니다.
 * 
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Schema(description = "로그아웃 명령 DTO")
public record LogoutCommand(
        @Schema(description = "무효화할 리프레시 토큰", 
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken,
        
        @Schema(description = "전체 디바이스 로그아웃 여부", 
                example = "false")
        boolean allDevices,
        
        @Schema(description = "블랙리스트에 추가할 액세스 토큰 (optional)", 
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken
) {
    /**
     * 로그아웃 명령 생성 팩토리 메서드
     *
     * @param refreshToken 리프레시 토큰
     * @param allDevices 전체 기기 로그아웃 여부
     * @param accessToken 액세스 토큰 (블랙리스트용)
     * @return LogoutCommand 객체
     */
    public static LogoutCommand of(String refreshToken, boolean allDevices, String accessToken) {
        return new LogoutCommand(refreshToken, allDevices, accessToken);
    }
    
    /**
     * 하위 호환성을 위한 팩토리 메서드
     */
    public static LogoutCommand of(String refreshToken, boolean allDevices) {
        return new LogoutCommand(refreshToken, allDevices, null);
    }
}