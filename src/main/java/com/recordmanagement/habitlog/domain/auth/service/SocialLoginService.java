package com.recordmanagement.habitlog.domain.auth.service;

import com.recordmanagement.habitlog.domain.auth.model.SocialUserInfo;
import com.recordmanagement.habitlog.domain.user.model.SocialType;

/**
 * 소셜 로그인 도메인 서비스 인터페이스
 *
 * 소셜 로그인 관련 비즈니스 로직을 처리하기 위한 계약(인터페이스)입니다.
 *
 * 주요 역할
 * - 소셜 플랫폼별 액세스 토큰을 검증하고 사용자 정보를 조회합니다.
 *
 * 사용 예
 * - 카카오, 애플 등 다양한 소셜 로그인 제공자에 대해 구현체가 존재할 수 있습니다.
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
public interface SocialLoginService {

    /**
     * 소셜 플랫폼에서 사용자 정보를 조회합니다.
     *
     * @param socialType 소셜 플랫폼 종류 (예: KAKAO, APPLE)
     * @param accessToken 소셜 플랫폼에서 발급받은 액세스 토큰
     * @return SocialUserInfo 검증된 소셜 사용자 정보 값 객체
     */
    SocialUserInfo getUserInfo(SocialType socialType, String accessToken);
}
