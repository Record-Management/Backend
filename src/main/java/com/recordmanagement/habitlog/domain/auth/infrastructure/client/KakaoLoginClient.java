package com.recordmanagement.habitlog.domain.auth.infrastructure.client;

import com.recordmanagement.habitlog.domain.auth.domain.model.SocialUserInfo;
import com.recordmanagement.habitlog.domain.auth.exception.SocialLoginException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.*;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Kakao 로그인 클라이언트
 *
 * LSP 적용: SocialLoginClient 계약을 정확히 준수
 * - 일관된 예외 처리로 대체 가능성 보장
 * - 카카오 액세스 토큰을 통해 사용자 정보를 조회합니다.
 * - WebClient를 사용하여 Kakao REST API 호출 및 응답 파싱을 수행합니다.
 *
 * @author 전우선
 * @since 2025.10.24
 * @version 2.0.0 (LSP 적용)
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
     * LSP 준수: 인터페이스 계약을 정확히 이행
     * - 동일한 예외 타입 (SocialLoginException) 사용
     * - 사전조건과 사후조건 준수
     *
     * @param accessToken 카카오로부터 발급받은 OAuth2 액세스 토큰
     * @return SocialUserInfo 객체 (socialId, name, email 포함)
     * @throws SocialLoginException 사용자 정보 조회 실패 시
     */
    @Operation(
            summary = "카카오 사용자 정보 조회",
            description = "카카오 액세스 토큰을 사용하여 사용자 ID, 닉네임, 이메일 정보를 조회합니다."
    )
    @Override
    public SocialUserInfo getUserInfo(String accessToken) throws SocialLoginException {
        try {
            KakaoUserResponse response = webClient.get()
                    .uri(KAKAO_USER_INFO_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(KakaoUserResponse.class)
                    .block();

            if (response == null || response.getId() == null) {
                throw SocialLoginException.userInfoFetchFailed();
            }

            String nickname = null;
            if (response.getKakaoAccount() != null && response.getKakaoAccount().getProfile() != null) {
                nickname = response.getKakaoAccount().getProfile().getNickname();
            }
            if (nickname == null && response.getProperties() != null) {
                nickname = response.getProperties().getNickname();
            }
            if (nickname == null) {
                nickname = "카카오 사용자 " + response.getId();
            }

            return SocialUserInfo.of(
                    String.valueOf(response.getId()),
                    nickname,
                    null,
                    null,
                    null
            );
        } catch (SocialLoginException e) {
            throw e; // SocialLoginException은 그대로 재던지기
        } catch (Exception e) {
            throw SocialLoginException.userInfoFetchFailed();
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
        private Properties properties;
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

    /**
     * 카카오 Properties 정보 객체
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Properties {
        private String nickname;
    }
}