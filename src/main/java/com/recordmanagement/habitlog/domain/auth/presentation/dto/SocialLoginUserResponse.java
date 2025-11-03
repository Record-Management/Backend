package com.recordmanagement.habitlog.domain.auth.presentation.dto;

import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.model.SocialType;
import com.recordmanagement.habitlog.domain.user.application.dto.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 소셜 로그인용 사용자 정보 응답 DTO
 * 
 * 소셜 로그인 시 필요한 최소한의 사용자 정보만 포함
 * 온보딩 관련 상세 정보는 제외하여 응답 크기 최적화
 * 
 * @author 전우선
 * @since 2025.09.06
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
@Builder
@Schema(description = "소셜 로그인용 사용자 정보")
public class SocialLoginUserResponse {

    @Schema(description = "사용자 고유 식별자", example = "user_123456", required = true)
    private final String id;

    @Schema(description = "사용자 이름", example = "홍길동", required = true)
    private final String name;

    @Schema(description = "이메일 주소", example = "hong@example.com", nullable = true)
    private final String email;

    @Schema(description = "소셜 로그인 플랫폼", example = "KAKAO", required = true)
    private final SocialType socialType;

    @Schema(description = "계정 생성 시간", example = "2025-09-02T02:46:41.454753", required = true)
    private final LocalDateTime createdAt;

    @Schema(description = "온보딩 완료 여부", example = "false", required = true)
    private final boolean onboardingCompleted;

    /**
     * User 도메인 객체를 SocialLoginUserResponse로 변환
     *
     * @param user 도메인 사용자 객체
     * @return SocialLoginUserResponse 객체
     */
    public static SocialLoginUserResponse from(User user) {
        return SocialLoginUserResponse.builder()
                .id(user.getId().getValue())
                .name(user.getName())
                .email(user.getEmail() != null ? user.getEmail().getValue() : null)
                .socialType(user.getSocialType())
                .createdAt(user.getCreatedAt())
                .onboardingCompleted(user.isOnboardingCompleted())
                .build();
    }

    /**
     * UserResponse DTO를 SocialLoginUserResponse로 변환
     *
     * @param userResponse 사용자 응답 DTO
     * @return SocialLoginUserResponse 객체
     */
    public static SocialLoginUserResponse from(UserResponse userResponse) {
        return SocialLoginUserResponse.builder()
                .id(userResponse.getId())
                .name(userResponse.getName())
                .email(userResponse.getEmail())
                .socialType(userResponse.getSocialType())
                .createdAt(userResponse.getCreatedAt())
                .onboardingCompleted(userResponse.isOnboardingCompleted())
                .build();
    }
}