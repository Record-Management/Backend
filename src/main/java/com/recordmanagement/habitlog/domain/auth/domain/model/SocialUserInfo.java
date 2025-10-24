package com.recordmanagement.habitlog.domain.auth.domain.model;

import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 소셜 사용자 정보 값 객체 (Value Object)
 *
 * 소셜 플랫폼(카카오, 애플 등)에서 받아온 사용자 정보를 캡슐화하는 불변 객체입니다.
 * 외부 API 응답을 내부 도메인 모델로 변환하는 역할을 하며, 소셜 로그인 시 필요한 최소한의 정보를 보관합니다.
 *
 * 비즈니스 룰
 * - socialId, name 필수, email은 선택적
 * - 유효하지 않은 상태로 객체 생성 불가
 *
 * 보안 및 개인정보 처리
 * - 이메일은 민감정보로, 로그 시 마스킹 필요
 * - 외부 API 응답에 의존하여 null 체크 필수
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "소셜 사용자 정보 값 객체 (SocialUserInfo)")
public class SocialUserInfo {

    @Schema(description = "소셜 플랫폼 고유 사용자 ID", example = "12345678", required = true)
    private String socialId;

    @Schema(description = "사용자 이름/닉네임", example = "전우선", required = true)
    private String name;

    @Schema(description = "사용자 이메일 (선택적)", example = "wooseon@example.com", nullable = true)
    private String email;

    @Schema(description = "프로필 이미지 URL (선택적)", example = "http://k.kakaocdn.net/dn/...", nullable = true)
    private String profileImageUrl;

    @Schema(description = "생년월일 (선택적)", example = "1990", nullable = true)
    private String birthYear;


    /**
     * 정적 팩토리 메서드 - 검증 후 객체 생성
     *
     * @param socialId 소셜 플랫폼 고유 사용자 ID (필수)
     * @param name 사용자 이름/닉네임 (필수)
     * @param email 사용자 이메일 (선택적)
     * @param profileImageUrl 프로필 이미지 URL (선택적)
     * @param birthYear 생년월일 (선택적)
     * @return 검증된 SocialUserInfo 객체
     * @throws CustomException 필수 필드가 null이거나 빈 문자열인 경우
     */
    public static SocialUserInfo of(String socialId, String name, String email, String profileImageUrl, String birthYear) {
        if (socialId == null || socialId.trim().isEmpty()) {
            throw new CustomException(ErrorCode.SOCIAL_ID_NULL_OR_EMPTY);
        }
        if (name == null || name.trim().isEmpty()) {
            throw new CustomException(ErrorCode.SOCIAL_NAME_NULL_OR_EMPTY);
        }
        return new SocialUserInfo(socialId, name, email, profileImageUrl, birthYear);
    }
}