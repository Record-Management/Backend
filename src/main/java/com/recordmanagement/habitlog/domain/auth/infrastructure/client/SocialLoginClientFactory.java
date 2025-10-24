package com.recordmanagement.habitlog.domain.auth.infrastructure.client;

import com.recordmanagement.habitlog.domain.user.domain.model.SocialType;
import com.recordmanagement.habitlog.domain.auth.exception.SocialLoginException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 소셜 로그인 클라이언트 팩토리
 *
 * 설명:
 * - 소셜 타입(SocialType)에 따라 적절한 소셜 로그인 클라이언트 구현체를 반환합니다.
 * - OCP(Open-Closed Principle)를 만족시키며, 새로운 소셜 타입이 추가되면 구현체만 등록하면 됩니다.
 *
 * 사용 예:
 * SocialLoginClient client = socialLoginClientFactory.getClient(SocialType.KAKAO);
 * SocialUserInfo info = client.getUserInfo("access-token");
 */
@Component
public class SocialLoginClientFactory {

    private final Map<SocialType, SocialLoginClient> clients = new HashMap<>();

    /**
     * 생성자
     * 각 소셜 타입별 로그인 클라이언트를 매핑합니다.
     *
     * @param kakaoLoginClient 카카오 로그인 클라이언트
     * @param appleLoginClient 애플 로그인 클라이언트
     */
    public SocialLoginClientFactory(KakaoLoginClient kakaoLoginClient,
                                    AppleLoginClient appleLoginClient) {
        clients.put(SocialType.KAKAO, kakaoLoginClient);
        clients.put(SocialType.APPLE, appleLoginClient);
    }

    /**
     * 소셜 타입에 해당하는 클라이언트를 반환합니다.
     *
     * @param socialType 사용할 소셜 타입 (KAKAO, APPLE 등)
     * @return 소셜 로그인 클라이언트 구현체
     * @throws SocialLoginException 지원하지 않는 소셜 타입일 경우
     */
    public SocialLoginClient getClient(SocialType socialType) {
        SocialLoginClient client = clients.get(socialType);
        if (client == null) {
            throw SocialLoginException.unsupportedProvider(socialType != null ? socialType.toString() : "null");
        }
        return client;
    }
}

