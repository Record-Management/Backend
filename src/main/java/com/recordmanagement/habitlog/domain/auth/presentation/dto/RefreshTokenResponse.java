package com.recordmanagement.habitlog.domain.auth.presentation.dto;

import com.recordmanagement.habitlog.domain.auth.application.dto.RefreshTokenResult;
import com.recordmanagement.habitlog.domain.user.application.dto.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 리프레시 토큰 갱신 응답 DTO
 * 
 * 목적:
 * - 토큰 갱신 API 응답 데이터를 클라이언트에게 전달
 * - 새로 발급된 액세스 토큰과 만료 시간 정보 포함
 * 
 * JSON 응답 예시:
 * ```json
 * {
 *   "accessToken": "new_jwt_access_token",
 *   "tokenType": "Bearer",
 *   "expiresIn": 3600
 * }
 * ```
 * 
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
@Builder
@Schema(
    description = "액세스 토큰 갱신 성공 응답 데이터",
    example = """
        {
          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new_access_token...",
          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new_refresh_token...",
          "expiresIn": 3600,
          "user": {
            "id": "user_id",
            "name": "이름",
            "email": null,
            "onboardingCompleted": false
          }
        }
        """
)
public class RefreshTokenResponse {
    
    /** 새로 발급된 JWT 액세스 토큰 */
    @Schema(
        description = """
            새로 발급된 JWT 액세스 토큰
            
            ### 즉시 적용 필요
            - 기존 액세스 토큰을 즉시 새 토큰으로 교체
            - Authorization 헤더 업데이트 필요
            - 메모리나 임시 저장소에 보관
            
            ### 토큰 갱신 주기
            - 만료 5-10분 전 미리 갱신 권장
            - 네트워크 지연을 고려한 여유 시간 확보
            - 백그라운드에서 자동 갱신 처리 권장
            """,
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyXzEyMzQ1NiIsImlhdCI6MTYzMjM2MzYwMCwiZXhwIjoxNjMyMzY3MjAwfQ.new123...",
        required = true
    )
    private final String accessToken;

    /** 새로 발급된 리프레시 토큰 (토큰 로테이션 적용) */
    @Schema(
        description = """
            새로 발급된 리프레시 토큰 (토큰 로테이션 적용)
            
            ### 보안 강화를 위한 토큰 로테이션
            - 기존 리프레시 토큰은 자동 무효화됨
            - 새 리프레시 토큰으로 즉시 교체 필요
            - 안전한 저장소에 보관 (Keychain, EncryptedSharedPreferences 등)
            
            ### 주의사항
            - 기존 리프레시 토큰은 더 이상 사용 불가
            - 토큰 갱신 시마다 새로운 리프레시 토큰 발급
            - 탈취 위험 최소화를 위한 보안 정책
            """,
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyXzEyMzQ1NiIsImlhdCI6MTYzMjM2MzYwMCwiZXhwIjoxNjMzNTczMjAwfQ.refresh123..."
    )
    private final String refreshToken;
    
    
    /** 토큰 만료까지 남은 시간 (초 단위) */
    @Schema(
        description = """
            액세스 토큰 만료까지 남은 시간 (초 단위)
            
            ### 활용 방법
            - 토큰 갱신 타이밍 계산에 사용
            - 클라이언트 타이머 설정 기준
            - 만료 예정 알림 표시 기준
            
            ### 일반적인 값
            - **3600**: 1시간 (기본값)
            - **1800**: 30분 (단축된 경우)
            - **7200**: 2시간 (연장된 경우)
            """,
        example = "3600",
        minimum = "300",
        maximum = "86400",
        required = true
    )
    private final Long expiresIn;
    
    @Schema(description = "사용자 정보")
    private final UserResponse user;
    
    /**
     * RefreshTokenResult에서 Response DTO로 변환
     * 
     * @param result 토큰 갱신 처리 결과
     * @return RefreshTokenResponse 객체
     */
    public static RefreshTokenResponse from(RefreshTokenResult result) {
        return RefreshTokenResponse.builder()
                .accessToken(result.getAccessToken())
                .refreshToken(result.getRefreshToken())
                .expiresIn(result.getExpiresIn())
                .user(result.getUser())
                .build();
    }
}