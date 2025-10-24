package com.recordmanagement.habitlog.global.config.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 전용 커스텀 예외 클래스
 *
 * ErrorCode를 활용해 HTTP 상태, 에러 코드, 메시지를 일관되게 관리
 * 서비스 및 도메인 계층에서 발생하는 비즈니스 예외 명확히 표현
 * 
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
public class CustomException extends RuntimeException {

    /** 에러 코드 객체 */
    private final ErrorCode errorCode;

    /**
     * 생성자
     *
     * @param errorCode 발생한 에러 코드
     */
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

