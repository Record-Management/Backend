package com.recordmanagement.habitlog.infrastructure.auth.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Apple Sign In 사용자 정보 응답 DTO
 * 
 * Apple로부터 받은 ID Token을 파싱한 사용자 정보를 담습니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"sub", "email"}) // 개인정보 로그 제외
public class AppleUserInfo {

    /** Apple 고유 사용자 ID */
    @JsonProperty("sub")
    private String sub;

    /** 사용자 이메일 */
    @JsonProperty("email")
    private String email;

    /** 이메일 인증 여부 */
    @JsonProperty("email_verified")
    private String emailVerified;

    /** 실제 사용자 여부 (is_private_email이 true면 비공개 이메일) */
    @JsonProperty("is_private_email")
    private String isPrivateEmail;

    /** 토큰 발급자 */
    @JsonProperty("iss")
    private String iss;

    /** 토큰 대상 (audience) */
    @JsonProperty("aud")
    private String aud;

    /** 토큰 만료 시간 */
    @JsonProperty("exp")
    private Long exp;

    /** 토큰 발급 시간 */
    @JsonProperty("iat")
    private Long iat;

    /**
     * 이메일이 인증되었는지 확인
     */
    public boolean isEmailVerified() {
        return "true".equals(emailVerified);
    }

    /**
     * 비공개 이메일인지 확인
     */
    public boolean isPrivateEmail() {
        return "true".equals(isPrivateEmail);
    }
}
