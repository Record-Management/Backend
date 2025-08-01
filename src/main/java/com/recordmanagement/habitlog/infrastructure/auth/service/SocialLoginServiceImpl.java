package com.recordmanagement.habitlog.infrastructure.auth.service;

import com.recordmanagement.habitlog.domain.auth.model.SocialUserInfo;
import com.recordmanagement.habitlog.domain.auth.service.SocialLoginService;
import com.recordmanagement.habitlog.domain.user.model.SocialType;
import com.recordmanagement.habitlog.infrastructure.auth.client.SocialLoginClient;
import com.recordmanagement.habitlog.infrastructure.auth.client.SocialLoginClientFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 소셜 로그인 서비스 구현체 (도메인 서비스 구현)
 *
 * - 소셜 타입에 따른 클라이언트를 팩토리에서 선택
 * - 액세스 토큰으로 사용자 정보를 조회
 *
 * 위치: Infrastructure Layer
 */
@Service
@RequiredArgsConstructor
public class SocialLoginServiceImpl implements SocialLoginService {

    private final SocialLoginClientFactory clientFactory;

    /**
     * 소셜 타입과 액세스 토큰을 이용하여 사용자 정보 조회
     *
     * @param socialType   KAKAO, APPLE 등 소셜 플랫폼 종류
     * @param accessToken  소셜 플랫폼에서 발급한 액세스 토큰
     * @return             소셜 사용자 정보
     */
    @Override
    public SocialUserInfo getUserInfo(SocialType socialType, String accessToken) {
        SocialLoginClient client = clientFactory.getClient(socialType);
        return client.getUserInfo(accessToken);
    }
}

