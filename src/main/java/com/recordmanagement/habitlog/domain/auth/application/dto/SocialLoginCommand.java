package com.recordmanagement.habitlog.domain.auth.application.dto;

import com.recordmanagement.habitlog.domain.user.domain.model.SocialType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 소셜 로그인 요청을 캡슐화하는 커맨드 DTO
 * 컨트롤러에서 애플리케이션 서비스로 소셜 로그인 요청 전달 시 사용
 * 소셜 플랫폼 타입과 액세스 토큰 정보를 불변 객체로 보장
 * Command 패턴을 적용해 요청을 객체화
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
@Builder
@Schema(description = "소셜 로그인 명령 DTO")
public class SocialLoginCommand {

    @Schema(description = "소셜 로그인 플랫폼 타입 (KAKAO, APPLE 등)", example = "KAKAO")
    private final SocialType socialType;

    @Schema(description = "소셜 플랫폼에서 발급받은 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String accessToken;

    /**
     * 소셜 로그인 커맨드 생성 팩토리 메서드
     *
     * @param socialType 소셜 플랫폼 타입
     * @param accessToken 액세스 토큰
     * @return SocialLoginCommand 객체
     */
    public static SocialLoginCommand of(SocialType socialType, String accessToken) {
        return SocialLoginCommand.builder()
                .socialType(socialType)
                .accessToken(accessToken)
                .build();
    }
}

