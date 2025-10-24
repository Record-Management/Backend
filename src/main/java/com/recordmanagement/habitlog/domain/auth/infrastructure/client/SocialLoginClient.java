package com.recordmanagement.habitlog.domain.auth.infrastructure.client;

import com.recordmanagement.habitlog.domain.auth.domain.model.SocialUserInfo;
import com.recordmanagement.habitlog.domain.auth.exception.SocialLoginException;

/**
 * 소셜 로그인 클라이언트 공통 인터페이스
 *
 * LSP 적용: 모든 구현체가 동일한 예외 계약을 준수
 * - 일관된 예외 처리로 대체 가능성 보장
 * - 구현체별 예외 강화 방지
 *
 * 목적:
 * - 소셜 로그인 플랫폼(카카오, 애플 등)의 사용자 정보 조회 기능을 추상화합니다.
 * - 서비스 로직에서는 플랫폼별 구현체에 의존하지 않고 이 인터페이스에 의존함으로써, 확장성과 테스트 용이성을 높입니다.
 *
 * 구현 대상 예:
 * - KakaoLoginClient
 * - AppleLoginClient
 *
 * @author 전우선
 * @since 2025.10.24
 * @version 2.0.0 (LSP 적용)
 */
public interface SocialLoginClient {

    /**
     * 액세스 토큰을 사용하여 사용자 정보를 조회합니다.
     *
     * @param accessToken 소셜 플랫폼에서 발급한 액세스 토큰
     * @return 조회된 사용자 정보 도메인 객체
     * @throws SocialLoginException 사용자 정보 조회 실패 시
     */
    SocialUserInfo getUserInfo(String accessToken) throws SocialLoginException;
}
