package com.recordmanagement.habitlog.api.auth.dto;

import com.recordmanagement.habitlog.application.auth.dto.SocialLoginResult;
import com.recordmanagement.habitlog.application.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 소셜 로그인 응답 DTO
 * 
 * 목적:
 * - 소셜 로그인 API 응답 데이터를 클라이언트에게 전달
 * - 사용자 정보, JWT 토큰들, 신규 사용자 여부 포함
 * 
 * JSON 응답 예시:
 * ```json
 * {
 *   "user": {
 *     "id": "user123",
 *     "name": "홍길동",
 *     "email": "hong@example.com"
 *   },
 *   "accessToken": "jwt_access_token",
 *   "refreshToken": "jwt_refresh_token",
 *   "isNewUser": false
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
    description = "소셜 로그인 성공 응답 데이터",
    example = """
        {
          "user": {
            "id": "user_123456",
            "name": "홍길동", 
            "email": "hong@example.com",
            "socialType": "KAKAO",
            "createdAt": "2025-09-02T02:46:41.454753",
            "onboardingCompleted": false
          },
          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
          "isNewUser": false
        }
        """
)
public class SocialLoginResponse {
    
    /** 로그인한 사용자 정보 */
    @Schema(
        description = "로그인한 사용자의 기본 정보",
        implementation = UserResponse.class,
        required = true
    )
    private final UserResponse user;
    
    /** JWT 액세스 토큰 */
    @Schema(
        description = """
            JWT 액세스 토큰
            
            ### 사용법
            - API 요청 시 Authorization 헤더에 'Bearer {accessToken}' 형태로 포함
            - 유효기간: 1시간
            - 만료 시 refreshToken으로 갱신 필요
            
            ### 보안 주의사항
            - 안전한 메모리에만 저장 (sessionStorage 권장)
            - 로그나 URL에 노출 금지
            - HTTPS 통신에서만 사용
            """,
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyXzEyMzQ1NiIsImlhdCI6MTYzMjM2MDAwMCwiZXhwIjoxNjMyMzYzNjAwfQ.abc123...",
        required = true
    )
    private final String accessToken;
    
    /** JWT 리프레시 토큰 */
    @Schema(
        description = """
            JWT 리프레시 토큰
            
            ### 사용법
            - 액세스 토큰 갱신 시에만 사용
            - 유효기간: 30일
            - 일회성 사용 (사용 후 새 리프레시 토큰 발급)
            
            ### 보안 주의사항
            - 안전한 저장소에 보관 (httpOnly 쿠키 또는 secure storage)
            - 네트워크 통신 최소화
            - 탈취 시 즉시 로그아웃 처리
            """,
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyXzEyMzQ1NiIsInR5cCI6InJlZnJlc2giLCJpYXQiOjE2MzIzNjAwMDAsImV4cCI6MTYzNDk1MjAwMH0.def456...",
        required = true
    )
    private final String refreshToken;
    
    /** 신규 사용자 여부 */
    @Schema(
        description = """
            신규 사용자 여부
            
            ### 값 의미
            - **true**: 이번 로그인으로 새로 가입된 사용자
            - **false**: 기존에 가입된 사용자
            
            ### 활용 방안
            - 신규 사용자인 경우 온보딩 화면 표시
            - 기존 사용자인 경우 메인 화면으로 이동
            - 가입 축하 이벤트 또는 튜토리얼 제공
            """,
        example = "false",
        required = true
    )
    private final boolean isNewUser;
    
    /**
     * SocialLoginResult에서 Response DTO로 변환
     * 
     * @param result 소셜 로그인 처리 결과
     * @return SocialLoginResponse 객체
     */
    public static SocialLoginResponse from(SocialLoginResult result) {
        return SocialLoginResponse.builder()
                .user(result.getUser())
                .accessToken(result.getAccessToken())
                .refreshToken(result.getRefreshToken())
                .isNewUser(result.isNewUser())
                .build();
    }
}