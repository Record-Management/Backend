package com.recordmanagement.habitlog.infrastructure.auth.client;

import com.recordmanagement.habitlog.domain.auth.model.SocialUserInfo;
import com.recordmanagement.habitlog.config.exception.CustomException;
import com.recordmanagement.habitlog.config.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Kakao 로그인 클라이언트
 *
 * 카카오 액세스 토큰을 통해 사용자 정보를 조회합니다.
 * WebClient를 사용하여 Kakao REST API 호출 및 응답 파싱을 수행합니다.
 */
@Component
public class KakaoLoginClient implements SocialLoginClient {

    private final WebClient webClient;

    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    public KakaoLoginClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * 카카오 액세스 토큰으로 사용자 정보 조회
     *
     * @param accessToken 카카오로부터 발급받은 OAuth2 액세스 토큰
     * @return SocialUserInfo 객체 (socialId, name, email 포함)
     * @throws CustomException 사용자 정보 조회 실패 시
     */
    @Operation(
            summary = "카카오 사용자 정보 조회",
            description = "카카오 액세스 토큰을 사용하여 사용자 ID, 닉네임, 이메일 정보를 조회합니다."
    )
    @Override
    public SocialUserInfo getUserInfo(String accessToken) {
        try {
            KakaoUserResponse response = webClient.get()
                    .uri(KAKAO_USER_INFO_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(KakaoUserResponse.class)
                    .block();

            if (response == null || response.getKakaoAccount() == null) {
                throw new CustomException(ErrorCode.SOCIAL_USER_INFO_FETCH_FAILED);
            }

            return SocialUserInfo.of(
                    String.valueOf(response.getId()),
                    response.getKakaoAccount().getProfile().getNickname(),
                    response.getKakaoAccount().getEmail()
            );
        } catch (Exception e) {
            throw new CustomException(ErrorCode.SOCIAL_USER_INFO_FETCH_FAILED);
        }
    }

    // ============ 카카오 API 응답 DTO ============

    /**
     * 카카오 사용자 정보 응답 객체
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KakaoUserResponse {
        private Long id;
        private KakaoAccount kakaoAccount;
    }

    /**
     * 카카오 계정 정보 객체
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KakaoAccount {
        private String email;
        private Profile profile;
    }

    /**
     * 카카오 프로필 정보 객체
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        private String nickname;
    }
}