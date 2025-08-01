package com.recordmanagement.habitlog.application.user.dto;

import com.recordmanagement.habitlog.domain.user.model.SocialType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 신규 사용자 등록 요청을 위한 커맨드 DTO
 * 소셜 로그인으로부터 받은 사용자 정보를 캡슐화
 * 도메인 모델(User) 생성을 위한 입력 데이터를 검증 및 전달
 *
 * @author 전우선
 * @since 1.0.0
 * @version 1.1.0
 */
@Getter
@AllArgsConstructor
@Builder
@Schema(description = "사용자 등록 명령 DTO")
public class UserRegistrationCommand {

    @Schema(description = "사용자 이름 또는 닉네임", example = "홍길동")
    private final String name;

    @Schema(description = "사용자 이메일 주소 (소셜 플랫폼에서 제공하지 않을 수 있음)", example = "user@example.com", nullable = true)
    private final String email;

    @Schema(description = "소셜 로그인 플랫폼 타입 (KAKAO, APPLE 등)", example = "KAKAO")
    private final SocialType socialType;

    @Schema(description = "소셜 플랫폼의 고유 사용자 식별자", example = "1234567890")
    private final String socialId;

    /**
     * 소셜 사용자 정보로부터 UserRegistrationCommand 객체 생성
     *
     * @param name 사용자 이름
     * @param email 이메일 주소 (nullable)
     * @param socialType 소셜 로그인 플랫폼 타입
     * @param socialId 소셜 플랫폼 고유 사용자 ID
     * @return UserRegistrationCommand 객체
     */
    public static UserRegistrationCommand of(String name, String email,
                                             SocialType socialType, String socialId) {
        return UserRegistrationCommand.builder()
                .name(name)
                .email(email)
                .socialType(socialType)
                .socialId(socialId)
                .build();
    }
}

