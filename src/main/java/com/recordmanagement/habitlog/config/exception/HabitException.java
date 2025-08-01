package com.recordmanagement.habitlog.config.exception;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 습관 관리 관련 예외 클래스
 *
 * 습관 도메인에서 발생하는 다양한 예외 상황을 표준화하여 처리합니다.
 * DomainException을 상속받아 일관된 예외 처리 체계를 제공합니다.
 * 
 * 주요 예외 유형:
 * - 습관 조회 실패 (존재하지 않는 습관)
 * - 습관 기록 조회 실패
 * - 습관 이름 검증 실패 (빈 값, 길이 초과)
 * - 습관 빈도 설정 오류
 * - 중복 완료 처리 시도
 * - 잘못된 날짜 범위 설정
 *
 * 사용 패턴:
 * - 정적 팩토리 메서드를 통한 명확한 예외 생성
 * - 메서드명으로 예외 상황을 직관적으로 표현
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Schema(description = "습관 관련 예외 클래스")
public class HabitException extends DomainException {

    /**
     * HabitException 생성자
     * 
     * 외부에서 직접 호출하지 않고 정적 팩토리 메서드를 통해서만 생성합니다.
     *
     * @param errorCode 습관 관련 에러 코드
     */
    private HabitException(ErrorCode errorCode) {
        super(errorCode);
    }

    // ============ 정적 팩토리 메서드들 ============

    /**
     * 습관을 찾을 수 없는 예외 생성
     *
     * @return HabitException 인스턴스
     */
    public static HabitException notFound() {
        return new HabitException(ErrorCode.HABIT_NOT_FOUND);
    }

    /**
     * 습관을 찾을 수 없는 예외 생성 (ID 정보 포함)
     *
     * @param habitId 조회 실패한 습관 ID
     * @return HabitException 인스턴스
     */
    public static HabitException notFound(String habitId) {
        return new HabitException(ErrorCode.HABIT_NOT_FOUND);
    }

    /**
     * 습관 기록을 찾을 수 없는 예외 생성
     *
     * @return HabitException 인스턴스
     */
    public static HabitException recordNotFound() {
        return new HabitException(ErrorCode.HABIT_RECORD_NOT_FOUND);
    }

    /**
     * 빈 습관 이름 예외 생성
     *
     * @return HabitException 인스턴스
     */
    public static HabitException emptyName() {
        return new HabitException(ErrorCode.HABIT_NAME_EMPTY);
    }

    /**
     * 습관 이름이 너무 긴 예외 생성
     *
     * @return HabitException 인스턴스
     */
    public static HabitException nameTooLong() {
        return new HabitException(ErrorCode.HABIT_NAME_TOO_LONG);
    }

    /**
     * 잘못된 습관 빈도 예외 생성
     *
     * @return HabitException 인스턴스
     */
    public static HabitException invalidFrequency() {
        return new HabitException(ErrorCode.INVALID_HABIT_FREQUENCY);
    }

    /**
     * 오늘 이미 완료한 습관 예외 생성
     *
     * @return HabitException 인스턴스
     */
    public static HabitException alreadyCompletedToday() {
        return new HabitException(ErrorCode.HABIT_ALREADY_COMPLETED_TODAY);
    }

    /**
     * 잘못된 날짜 범위 예외 생성
     *
     * @return HabitException 인스턴스
     */
    public static HabitException invalidDateRange() {
        return new HabitException(ErrorCode.INVALID_DATE_RANGE);
    }
}
