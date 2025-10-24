package com.recordmanagement.habitlog.domain.auth.infrastructure.client;

import com.recordmanagement.habitlog.domain.auth.domain.model.SocialUserInfo;

/**
 * 소셜 로그인 클라이언트 공통 인터페이스
 *
 * 목적:
 * - 소셜 로그인 플랫폼(카카오, 애플 등)의 사용자 정보 조회 기능을 추상화합니다.
 * - 서비스 로직에서는 플랫폼별 구현체에 의존하지 않고 이 인터페이스에 의존함으로써, 확장성과 테스트 용이성을 높입니다.
 *
 * 구현 대상 예:
 * - KakaoLoginClient
 * - AppleLoginClient
 *
 * 사용 예:
 * SocialLoginClient client = new KakaoLoginClient(...);
 * SocialUserInfo userInfo = client.getUserInfo(accessToken);
 */
public interface SocialLoginClient {

    /**
     * 액세스 토큰을 사용하여 사용자 정보를 조회합니다.
     *
     * @param accessToken 소셜 플랫폼에서 발급한 액세스 토큰
     * @return 조회된 사용자 정보 도메인 객체
     */
    SocialUserInfo getUserInfo(String accessToken);
}
