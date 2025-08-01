package com.recordmanagement.habitlog.config.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SocialLoginException 단위 테스트
 * 
 * 소셜 로그인 예외의 생성과 정적 팩토리 메서드들을 테스트합니다.
 */
@DisplayName("소셜 로그인 예외 테스트")
class SocialLoginExceptionTest {

    @Test
    @DisplayName("지원하지 않는 제공자 예외 생성 테스트")
    void unsupportedProvider_shouldCreateExceptionWithCorrectErrorCode() {
        // Given
        String provider = "NAVER";

        // When
        SocialLoginException exception = SocialLoginException.unsupportedProvider(provider);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SOCIAL_PROVIDER_NOT_SUPPORTED);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.SOCIAL_PROVIDER_NOT_SUPPORTED.getMessage());
    }

    @Test
    @DisplayName("유효하지 않은 액세스 토큰 예외 생성 테스트")
    void invalidAccessToken_shouldCreateExceptionWithCorrectErrorCode() {
        // When
        SocialLoginException exception = SocialLoginException.invalidAccessToken();

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SOCIAL_ACCESS_TOKEN_INVALID);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.SOCIAL_ACCESS_TOKEN_INVALID.getMessage());
    }

    @Test
    @DisplayName("사용자 정보 조회 실패 예외 생성 테스트")
    void userInfoFetchFailed_shouldCreateExceptionWithCorrectErrorCode() {
        // When
        SocialLoginException exception = SocialLoginException.userInfoFetchFailed();

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SOCIAL_USER_INFO_FETCH_FAILED);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.SOCIAL_USER_INFO_FETCH_FAILED.getMessage());
    }

    @Test
    @DisplayName("소셜 로그인 실패 예외 생성 테스트")
    void loginFailed_shouldCreateExceptionWithCorrectErrorCode() {
        // When
        SocialLoginException exception = SocialLoginException.loginFailed();

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SOCIAL_LOGIN_FAILED);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.SOCIAL_LOGIN_FAILED.getMessage());
    }

    @Test
    @DisplayName("Apple ID 토큰 무효 예외 생성 테스트")
    void appleIdTokenInvalid_shouldCreateExceptionWithCorrectErrorCode() {
        // When
        SocialLoginException exception = SocialLoginException.appleIdTokenInvalid();

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.APPLE_ID_TOKEN_INVALID);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.APPLE_ID_TOKEN_INVALID.getMessage());
    }

    @Test
    @DisplayName("Apple 토큰 만료 예외 생성 테스트")
    void appleTokenExpired_shouldCreateExceptionWithCorrectErrorCode() {
        // When
        SocialLoginException exception = SocialLoginException.appleTokenExpired();

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.APPLE_TOKEN_EXPIRED);
        assertThat(exception.getMessage()).isEqualTo(ErrorCode.APPLE_TOKEN_EXPIRED.getMessage());
    }

    @Test
    @DisplayName("SocialLoginException은 DomainException을 상속해야 함")
    void socialLoginException_shouldExtendDomainException() {
        // When
        SocialLoginException exception = SocialLoginException.loginFailed();

        // Then
        assertThat(exception).isInstanceOf(DomainException.class);
        assertThat(exception).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("모든 정적 팩토리 메서드는 서로 다른 ErrorCode를 가져야 함")
    void allStaticMethods_shouldHaveDifferentErrorCodes() {
        // When
        SocialLoginException unsupported = SocialLoginException.unsupportedProvider("test");
        SocialLoginException invalidToken = SocialLoginException.invalidAccessToken();
        SocialLoginException fetchFailed = SocialLoginException.userInfoFetchFailed();
        SocialLoginException loginFailed = SocialLoginException.loginFailed();
        SocialLoginException appleInvalid = SocialLoginException.appleIdTokenInvalid();
        SocialLoginException appleExpired = SocialLoginException.appleTokenExpired();

        // Then
        assertThat(unsupported.getErrorCode()).isNotEqualTo(invalidToken.getErrorCode());
        assertThat(invalidToken.getErrorCode()).isNotEqualTo(fetchFailed.getErrorCode());
        assertThat(fetchFailed.getErrorCode()).isNotEqualTo(loginFailed.getErrorCode());
        assertThat(loginFailed.getErrorCode()).isNotEqualTo(appleInvalid.getErrorCode());
        assertThat(appleInvalid.getErrorCode()).isNotEqualTo(appleExpired.getErrorCode());
    }
}
