package com.recordmanagement.habitlog.global.config.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 도메인 예외를 위한 추상 베이스 클래스
 * 
 * 모든 도메인별 예외 클래스들의 공통 기능을 제공합니다.
 * ErrorCode 기반의 일관된 예외 처리 체계를 강제합니다.
 * 
 * 주요 기능:
 * - ErrorCode 기반 예외 생성
 * - 일관된 예외 구조 제공
 * - 정적 팩토리 메서드 패턴 지원
 * 
 * 사용 예시:
 * ```java
 * public class UserException extends DomainException {
 *     private UserException(ErrorCode errorCode) {
 *         super(errorCode);
 *     }
 *     
 *     public static UserException notFound() {
 *         return new UserException(ErrorCode.USER_NOT_FOUND);
 *     }
 * }
 * ```
 * 
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
@Schema(description = "도메인 예외 추상 베이스 클래스")
public abstract class DomainException extends CustomException {

    /**
     * 도메인 예외 생성자
     * 
     * 하위 클래스에서만 호출 가능하도록 protected로 제한합니다.
     * 외부에서는 정적 팩토리 메서드를 통해서만 인스턴스를 생성해야 합니다.
     * 
     * @param errorCode 도메인별 에러 코드
     */
    protected DomainException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * 도메인 예외 생성자 (커스텀 메시지 포함)
     * 
     * 기본 에러 메시지 대신 커스텀 메시지를 사용하고 싶을 때 활용합니다.
     * 
     * @param errorCode 도메인별 에러 코드
     * @param customMessage 커스텀 에러 메시지
     */
    protected DomainException(ErrorCode errorCode, String customMessage) {
        super(errorCode);
        // 커스텀 메시지는 CustomException에서 처리되도록 구조 개선 필요 시 추가
    }
}
