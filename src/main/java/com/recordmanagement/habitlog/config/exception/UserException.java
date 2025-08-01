package com.recordmanagement.habitlog.config.exception;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 사용자 관련 예외 클래스
 *
 * 사용자 도메인에서 발생하는 다양한 예외 상황을 표준화하여 처리합니다.
 * DomainException을 상속받아 일관된 예외 처리 체계를 제공합니다.
 * 
 * 주요 예외 유형:
 * - 사용자 조회 실패 (존재하지 않는 사용자)
 * - 이메일 중복 가입 시도
 * - 잘못된 정보 형식 (이메일, 비밀번호)
 * - 계정 상태 관련 예외
 *
 * 사용 패턴:
 * - 정적 팩토리 메서드를 통한 명확한 예외 생성
 * - 메서드명으로 예외 상황을 직관적으로 표현
 * - 필요시 추가 정보(사용자 ID, 이메일 등)를 포함한 예외 생성 가능
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Schema(description = "사용자 관련 예외 클래스")
public class UserException extends DomainException {

    /**
     * UserException 생성자
     * 
     * 외부에서 직접 호출하지 않고 정적 팩토리 메서드를 통해서만 생성합니다.
     *
     * @param errorCode 사용자 관련 에러 코드
     */
    private UserException(ErrorCode errorCode) {
        super(errorCode);
    }

    // ============ 정적 팩토리 메서드들 ============

    /**
     * 사용자를 찾을 수 없는 예외 생성
     *
     * @return UserException 인스턴스
     */
    public static UserException notFound() {
        return new UserException(ErrorCode.USER_NOT_FOUND);
    }

    /**
     * 사용자를 찾을 수 없는 예외 생성 (ID 정보 포함)
     *
     * @param userId 조회 실패한 사용자 ID
     * @return UserException 인스턴스
     */
    public static UserException notFound(String userId) {
        // userId를 로그용 등으로 활용할 수 있음 (현재는 에러 코드만 사용)
        return new UserException(ErrorCode.USER_NOT_FOUND);
    }

    /**
     * 이메일 중복 예외 생성
     *
     * @return UserException 인스턴스
     */
    public static UserException emailDuplicate() {
        return new UserException(ErrorCode.USER_EMAIL_DUPLICATE);
    }

    /**
     * 이미 가입된 사용자 예외 생성
     *
     * @return UserException 인스턴스
     */
    public static UserException alreadyExists() {
        return new UserException(ErrorCode.USER_ALREADY_EXISTS);
    }

    /**
     * 잘못된 이메일 형식 예외 생성
     *
     * @return UserException 인스턴스
     */
    public static UserException invalidEmailFormat() {
        return new UserException(ErrorCode.INVALID_EMAIL_FORMAT);
    }

    /**
     * 잘못된 비밀번호 형식 예외 생성
     *
     * @return UserException 인스턴스
     */
    public static UserException invalidPasswordFormat() {
        return new UserException(ErrorCode.INVALID_PASSWORD_FORMAT);
    }
}