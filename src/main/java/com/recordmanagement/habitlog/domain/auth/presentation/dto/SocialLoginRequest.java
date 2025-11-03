package com.recordmanagement.habitlog.domain.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소셜 로그인 요청 DTO
 * 
 * 목적:
 * - 클라이언트에서 소셜 로그인 API 호출 시 사용하는 요청 데이터
 * - HTTP 요청 Body를 Java 객체로 역직렬화
 * - Bean Validation을 통한 입력값 검증
 * 
 * 검증 규칙:
 * - socialType: 필수 입력, 빈 문자열 불허용, 지원 플랫폼만 허용
 * - accessToken: 필수 입력, 빈 문자열 불허용 (카카오: accessToken, 애플: identityToken)
 * 
 * JSON 요청 예시:
 * ```json
 * {
 *   "socialType": "apple",
 *   "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIwMDAxMjMuYWJjMTIzLjEyMzQifQ.abc123"
 * }
 * ```
 * 
 * @author 전우선
 * @since 2025.07.30
 * @version 2.0.0 - identityToken 방식 지원
 */
@Getter
@NoArgsConstructor  // Jackson 역직렬화를 위한 기본 생성자
@AllArgsConstructor
@Builder
@Schema(
    description = "소셜 로그인 요청 데이터",
    example = """
        {
          "socialType": "apple",
          "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIwMDAxMjMuYWJjMTIzZGVmNDU2Z2hpNzg5LjEyMzQiLCJlbWFpbCI6InVzZXJAcHJpdmF0ZXJlbGF5LmFwcGxlaWQuY29tIn0.abc123def456"
        }
        """
)
public class SocialLoginRequest {
    
    /** 소셜 로그인 플랫폼 타입 (kakao, apple) */
    @Schema(
        description = """
            소셜 로그인 플랫폼 타입
            
            ### 지원 플랫폼
            - **kakao**: 카카오 로그인
            - **apple**: 애플 로그인
            """,
        example = "kakao",
        allowableValues = {"kakao", "apple"},
        required = true
    )
    @NotBlank(message = "소셜 타입은 필수입니다")
    @Pattern(
        regexp = "^(kakao|apple)$", 
        message = "지원하지 않는 소셜 로그인 타입입니다. (지원: kakao, apple)"
    )
    private String socialType;
    
    /** 소셜 플랫폼에서 발급받은 토큰 (카카오: accessToken, 애플: identityToken) */
    @Schema(
        description = """
            소셜 플랫폼에서 발급받은 인증 토큰
            
            ### 토큰 타입별 설명
            1. **카카오**: OAuth2 accessToken 
               - Kakao SDK/REST API로 로그인 후 발급받은 액세스 토큰
               - 카카오 사용자 정보 조회용
            
            2. **애플**: identityToken (JWT)
               - Sign in with Apple로 로그인 후 발급받은 JWT 토큰
               - 애플 사용자 식별용 sub 정보 포함
               - 더 안정적이고 Apple 공식 권장 방식
            
            ### 주의사항
            - 토큰은 발급 직후 즉시 사용 권장
            - 만료된 토큰은 401 에러 발생
            - 토큰 값은 로그 노출 금지
            """,
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIwMDAxMjMuYWJjMTIzZGVmNDU2Z2hpNzg5LjEyMzQiLCJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20ifQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
        required = true,
        minLength = 10,
        maxLength = 4096
    )
    @NotBlank(message = "인증 토큰은 필수입니다")
    private String accessToken;
    
    /**
     * 정적 팩토리 메서드
     * 
     * @param socialType 소셜 타입
     * @param accessToken 액세스 토큰
     * @return SocialLoginRequest 객체
     */
    public static SocialLoginRequest of(String socialType, String accessToken) {
        return SocialLoginRequest.builder()
                .socialType(socialType)
                .accessToken(accessToken)
                .build();
    }
}