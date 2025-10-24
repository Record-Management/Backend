package com.recordmanagement.habitlog.domain.auth.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Apple Token 검증 응답 DTO
 * 
 * Apple의 token validation 엔드포인트로부터 받는 응답 데이터
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"accessToken", "refreshToken", "idToken"}) // 토큰 정보 로그 제외
public class AppleTokenResponse {

    /** 액세스 토큰 */
    @JsonProperty("access_token")
    private String accessToken;

    /** 토큰 타입 (일반적으로 "Bearer") */
    @JsonProperty("token_type")
    private String tokenType;

    /** 토큰 만료 시간 (초) */
    @JsonProperty("expires_in")
    private Long expiresIn;

    /** 리프레시 토큰 */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /** ID 토큰 (JWT 형태, 사용자 정보 포함) */
    @JsonProperty("id_token")
    private String idToken;
}
