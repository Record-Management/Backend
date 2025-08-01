package com.recordmanagement.habitlog.config.exception;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * JWT 인증 관련 예외 클래스
 *
 * JWT 토큰 처리 중 발생하는 다양한 예외 상황을 처리합니다.
 * DomainException을 상속받아 일관된 예외 처리 체계를 제공합니다.
 *
 * 주요 용도:
 * - 토큰 만료, 유효하지 않음, 형식 오류, 서명 오류 등의 JWT 예외를 명확하게 구분하여 처리
 * - ErrorCode 기반 일관된 예외 관리 및 응답 제공
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Schema(description = "JWT 인증 예외 클래스")
public class JwtAuthenticationException extends DomainException {

    /**
     * JwtAuthenticationException 생성자
     * 
     * 외부에서 직접 호출하지 않고 정적 팩토리 메서드를 통해서만 생성합니다.
     *
     * @param errorCode JWT 관련 에러 코드
     */
    private JwtAuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 만료된 토큰 예외 생성
     *
     * @return JwtAuthenticationException 인스턴스
     */
    public static JwtAuthenticationException expiredToken() {
        return new JwtAuthenticationException(ErrorCode.EXPIRED_TOKEN);
    }

    /**
     * 유효하지 않은 토큰 예외 생성
     *
     * @return JwtAuthenticationException 인스턴스
     */
    public static JwtAuthenticationException invalidToken() {
        return new JwtAuthenticationException(ErrorCode.INVALID_TOKEN);
    }

    /**
     * 잘못된 형식의 토큰 예외 생성
     *
     * @return JwtAuthenticationException 인스턴스
     */
    public static JwtAuthenticationException malformedToken() {
        return new JwtAuthenticationException(ErrorCode.TOKEN_MALFORMED);
    }

    /**
     * 토큰 서명 오류 예외 생성
     *
     * @return JwtAuthenticationException 인스턴스
     */
    public static JwtAuthenticationException signatureInvalid() {
        return new JwtAuthenticationException(ErrorCode.TOKEN_SIGNATURE_INVALID);
    }

    /**
     * 리프레시 토큰 무효 예외 생성
     *
     * @return JwtAuthenticationException 인스턴스
     */
    public static JwtAuthenticationException refreshTokenInvalid() {
        return new JwtAuthenticationException(ErrorCode.REFRESH_TOKEN_INVALID);
    }

    /**
     * 리프레시 토큰 만료 예외 생성
     *
     * @return JwtAuthenticationException 인스턴스
     */
    public static JwtAuthenticationException refreshTokenExpired() {
        return new JwtAuthenticationException(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }
}