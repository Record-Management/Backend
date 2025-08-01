package com.recordmanagement.habitlog.application.auth.dto;

import com.recordmanagement.habitlog.application.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 소셜 로그인 처리 결과를 담는 DTO
 * 사용자 정보, 액세스 토큰, 리프레시 토큰, 신규 사용자 여부를 포함
 * 클라이언트는 토큰 저장 및 온보딩 분기 처리에 활용
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
@Builder
@Schema(description = "소셜 로그인 결과 DTO")
public class SocialLoginResult {

    @Schema(description = "로그인한 사용자 정보")
    private final UserResponse user;

    @Schema(description = "JWT 액세스 토큰 (API 인증용)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String accessToken;

    @Schema(description = "JWT 리프레시 토큰 (토큰 갱신용)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String refreshToken;

    @Schema(description = "신규 사용자 여부 (온보딩 플로우 분기용)", example = "false")
    private final boolean isNewUser;

    /**
     * 소셜 로그인 결과 객체 생성 팩토리 메서드
     *
     * @param user 로그인한 사용자 정보
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @param isNewUser 신규 사용자 여부
     * @return SocialLoginResult 객체
     */
    public static SocialLoginResult of(UserResponse user, String accessToken,
                                       String refreshToken, boolean isNewUser) {
        return SocialLoginResult.builder()
                .user(user)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isNewUser(isNewUser)
                .build();
    }
}