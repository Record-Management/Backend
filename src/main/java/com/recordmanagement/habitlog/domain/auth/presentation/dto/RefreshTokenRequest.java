package com.recordmanagement.habitlog.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 리프레시 토큰 갱신 요청 DTO
 * 
 * 목적:
 * - 액세스 토큰 갱신 API 호출 시 사용하는 요청 데이터
 * - 만료된 액세스 토큰을 새로 발급받기 위한 리프레시 토큰 전달
 * 
 * JSON 요청 예시:
 * ```json
 * {
 *   "refreshToken": "jwt_refresh_token_string"
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
    description = "액세스 토큰 갱신 요청 데이터",
    example = """
        {
          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        }
        """
)
public class RefreshTokenRequest {
    
    /** 갱신할 리프레시 토큰 */
    @Schema(
        description = """
            유효한 리프레시 토큰
            
            ### 사용 조건
            - 소셜 로그인 성공 시 발급받은 리프레시 토큰
            - 만료되지 않은 토큰 (유효기간: 30일)
            - 이전에 사용되지 않은 토큰
            
            ### 갱신 시나리오
            1. **액세스 토큰 만료**: API 호출 시 401 Unauthorized 응답 시
            2. **예방적 갱신**: 만료 전 미리 토큰 갱신
            3. **앱 재시작**: 저장된 액세스 토큰의 유효성 확인 불가 시
            
            ### 보안 고려사항
            - 리프레시 토큰 탈취 시 계정 보안 위험
            - 안전한 저장소에 보관 필수
            - 의심스러운 활동 감지 시 즉시 무효화
            """,
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyXzEyMzQ1NiIsInR5cCI6InJlZnJlc2giLCJpYXQiOjE2MzIzNjAwMDAsImV4cCI6MTYzNDk1MjAwMH0.def456...",
        required = true,
        minLength = 10,
        maxLength = 2048
    )
    @NotBlank(message = "리프레시 토큰은 필수입니다")
    private String refreshToken;
    
    /**
     * 정적 팩토리 메서드
     * 
     * @param refreshToken 리프레시 토큰
     * @return RefreshTokenRequest 객체
     */
    public static RefreshTokenRequest of(String refreshToken) {
        return RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();
    }
}