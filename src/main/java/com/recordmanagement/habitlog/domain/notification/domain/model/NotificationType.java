package com.recordmanagement.habitlog.domain.notification.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 알림 타입 열거형
 *
 * - 시스템에서 발송하는 알림의 종류를 정의
 * - 각 타입별로 다른 처리 로직 적용 가능
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum NotificationType {

    /**
     * 메인 기록 미등록 알림
     */
    DAILY_RECORD_REMINDER("메인 기록 미등록 알림"),

    /**
     * 운동 기록 알림
     */
    EXERCISE_REMINDER("운동 기록 알림"),

    /**
     * 습관 기록 알림
     */
    HABIT_REMINDER("습관 기록 알림"),

    /**
     * 시스템 공지사항
     */
    SYSTEM_ANNOUNCEMENT("시스템 공지사항"),

    /**
     * 테스트 알림
     */
    TEST("테스트 알림");

    private final String displayName;

    /**
     * 문자열로부터 알림 타입 찾기
     *
     * @param value 알림 타입 문자열
     * @return NotificationType
     * @throws IllegalArgumentException 잘못된 타입인 경우
     */
    public static NotificationType fromValue(String value) {
        for (NotificationType type : values()) {
            if (type.name().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("잘못된 알림 타입입니다: " + value);
    }
}