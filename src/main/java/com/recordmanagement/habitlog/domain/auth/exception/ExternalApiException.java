package com.recordmanagement.habitlog.domain.auth.exception;

import com.recordmanagement.habitlog.global.config.exception.DomainException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;

/**
 * 외부 API 호출 관련 예외 클래스
 * 
 * DIP 적용: 도메인 레이어에서 정의된 예외
 * - ExternalApiClient 사용 시 발생하는 예외
 * - 구체적인 HTTP 클라이언트 예외를 추상화
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0 (DIP 적용)
 */
public class ExternalApiException extends DomainException {

    private ExternalApiException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 외부 API 호출 실패 예외 생성
     * 
     * @return ExternalApiException 인스턴스
     */
    public static ExternalApiException callFailed() {
        return new ExternalApiException(ErrorCode.EXTERNAL_API_CALL_FAILED);
    }

    /**
     * 외부 API 응답 파싱 실패 예외 생성
     * 
     * @return ExternalApiException 인스턴스
     */
    public static ExternalApiException responseParseFailed() {
        return new ExternalApiException(ErrorCode.EXTERNAL_API_RESPONSE_PARSE_FAILED);
    }

    /**
     * 외부 API 연결 시간 초과 예외 생성
     * 
     * @return ExternalApiException 인스턴스
     */
    public static ExternalApiException timeout() {
        return new ExternalApiException(ErrorCode.EXTERNAL_API_TIMEOUT);
    }
}