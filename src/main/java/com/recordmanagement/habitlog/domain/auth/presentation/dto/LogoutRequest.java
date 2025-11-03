package com.recordmanagement.habitlog.domain.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그아웃 요청 DTO
 * 
 * 목적:
 * - 로그아웃 API 호출 시 사용하는 요청 데이터
 * - 리프레시 토큰과 로그아웃 범위 정보 전달
 * 
 * JSON 요청 예시:
 * ```json
 * {
 *   "refreshToken": "jwt_refresh_token_string",
 *   "allDevices": false
 * }
 * ```
 * 
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
    description = "로그아웃 요청 데이터",
    example = """
        {
          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
          "allDevices": false
        }
        """
)
public class LogoutRequest {
    
    /** 무효화할 리프레시 토큰 */
    @Schema(
        description = """
            무효화할 리프레시 토큰
            
            ### 토큰 검증
            - 현재 사용자 소유의 유효한 리프레시 토큰만 허용
            - 만료된 토큰도 로그아웃 처리 가능 (보안상 삭제)
            - 이미 사용된 토큰도 처리 가능
            
            ### 보안 고려사항
            - 토큰 탈취 의심 시 allDevices=true로 전체 로그아웃 권장
            - 로그아웃 후 클라이언트에서 토큰 완전 삭제 필요
            - 네트워크 요청 실패 시에도 로컬 토큰 삭제 권장
            """,
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyXzEyMzQ1NiIsInR5cCI6InJlZnJlc2giLCJpYXQiOjE2MzIzNjAwMDAsImV4cCI6MTYzNDk1MjAwMH0.def456...",
        required = true,
        minLength = 10,
        maxLength = 2048
    )
    @NotBlank(message = "리프레시 토큰은 필수입니다")
    private String refreshToken;
    
    /** 전체 디바이스 로그아웃 여부 (기본값: false) */
    @Schema(
        description = """
            전체 디바이스 로그아웃 여부
            
            ### 옵션 설명
            - **false (기본값)**: 현재 디바이스만 로그아웃
            - **true**: 모든 디바이스에서 강제 로그아웃
            
            ### 사용 시나리오
            
            #### 단일 디바이스 로그아웃 (false)
            - 일반적인 로그아웃
            - 현재 디바이스의 리프레시 토큰만 무효화
            - 다른 디바이스는 로그인 상태 유지
            
            #### 전체 디바이스 로그아웃 (true)
            - 보안 위험 상황 (토큰 탈취 의심)
            - 계정 설정에서 "모든 디바이스에서 로그아웃"
            - 비밀번호 변경 후 보안 강화
            - 해당 사용자의 모든 리프레시 토큰 무효화
            """,
        example = "false",
        defaultValue = "false"
    )
    @Builder.Default
    private boolean allDevices = false;
    
    /**
     * 단일 디바이스 로그아웃 요청 생성
     * 
     * @param refreshToken 리프레시 토큰
     * @return LogoutRequest 객체
     */
    public static LogoutRequest singleDevice(String refreshToken) {
        return LogoutRequest.builder()
                .refreshToken(refreshToken)
                .allDevices(false)
                .build();
    }
    
    /**
     * 전체 디바이스 로그아웃 요청 생성
     * 
     * @param refreshToken 리프레시 토큰
     * @return LogoutRequest 객체
     */
    public static LogoutRequest allDevices(String refreshToken) {
        return LogoutRequest.builder()
                .refreshToken(refreshToken)
                .allDevices(true)
                .build();
    }
}