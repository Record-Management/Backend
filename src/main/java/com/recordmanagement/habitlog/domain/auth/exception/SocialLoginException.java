package com.recordmanagement.habitlog.domain.auth.exception;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 소셜 로그인 관련 예외 클래스
 *
 * 소셜 로그인 과정에서 발생하는 다양한 예외 상황을 처리합니다.
 * DomainException을 상속받아 일관된 예외 처리 체계를 제공합니다.
 *
 * 주요 예외 유형:
 * - 지원하지 않는 소셜 제공자
 * - 소셜 액세스 토큰 무효
 * - 사용자 정보 조회 실패
 * - 소셜 로그인 실패 일반 오류
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Schema(description = "소셜 로그인 예외 클래스")
public class SocialLoginException extends RuntimeException {

    /**
     * SocialLoginException 생성자
     * 
     * 외부에서 직접 호출하지 않고 정적 팩토리 메서드를 통해서만 생성합니다.
     *
     * @param errorCode 소셜 로그인 관련 에러 코드
     */
    private final ErrorCode errorCode;

    private SocialLoginException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 지원하지 않는 소셜 제공자 예외 생성
     *
     * @param provider 소셜 제공자 명 (ex: Google, Facebook)
     * @return SocialLoginException 인스턴스
     */
    public static SocialLoginException unsupportedProvider(String provider) {
        return new SocialLoginException(ErrorCode.SOCIAL_PROVIDER_NOT_SUPPORTED);
    }

    /**
     * 소셜 액세스 토큰 무효 예외 생성
     *
     * @return SocialLoginException 인스턴스
     */
    public static SocialLoginException invalidAccessToken() {
        return new SocialLoginException(ErrorCode.SOCIAL_ACCESS_TOKEN_INVALID);
    }

    /**
     * 소셜 사용자 정보 조회 실패 예외 생성
     *
     * @return SocialLoginException 인스턴스
     */
    public static SocialLoginException userInfoFetchFailed() {
        return new SocialLoginException(ErrorCode.SOCIAL_USER_INFO_FETCH_FAILED);
    }

    /**
     * 소셜 로그인 일반 실패 예외 생성
     *
     * @return SocialLoginException 인스턴스
     */
    public static SocialLoginException loginFailed() {
        return new SocialLoginException(ErrorCode.SOCIAL_LOGIN_FAILED);
    }

    /**
     * Apple ID 토큰 무효 예외 생성
     *
     * @return SocialLoginException 인스턴스
     */
    public static SocialLoginException appleIdTokenInvalid() {
        return new SocialLoginException(ErrorCode.APPLE_ID_TOKEN_INVALID);
    }

    /**
     * Apple 토큰 만료 예외 생성
     *
     * @return SocialLoginException 인스턴스
     */
    public static SocialLoginException appleTokenExpired() {
        return new SocialLoginException(ErrorCode.APPLE_TOKEN_EXPIRED);
    }
}