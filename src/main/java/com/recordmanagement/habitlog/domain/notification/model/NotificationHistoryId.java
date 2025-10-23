package com.recordmanagement.habitlog.domain.notification.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * 알림 히스토리 식별자 값 객체
 *
 * - 알림 히스토리의 고유 식별자를 담당하는 값 객체
 * - UUID 기반으로 고유성 보장
 * - 불변 객체로 설계하여 안전한 참조 가능
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Getter
@ToString
@EqualsAndHashCode
@Schema(description = "알림 히스토리 식별자 값 객체")
public class NotificationHistoryId {

    @Schema(description = "알림 히스토리 고유 식별자 값", example = "550e8400-e29b-41d4-a716-446655440000")
    private final String value;

    /**
     * 새로운 알림 히스토리 ID 생성
     *
     * @param value UUID 문자열
     */
    private NotificationHistoryId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("알림 히스토리 ID는 null이거나 빈 값일 수 없습니다.");
        }
        this.value = value;
    }

    /**
     * 새로운 알림 히스토리 ID 생성 (UUID 자동 생성)
     *
     * @return 새로 생성된 NotificationHistoryId
     */
    public static NotificationHistoryId generate() {
        return new NotificationHistoryId(UUID.randomUUID().toString());
    }

    /**
     * 기존 알림 히스토리 ID로부터 객체 생성
     *
     * @param value 알림 히스토리 ID 문자열
     * @return NotificationHistoryId 객체
     */
    public static NotificationHistoryId from(String value) {
        return new NotificationHistoryId(value);
    }
}