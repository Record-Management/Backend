package com.recordmanagement.habitlog.domain.notification.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * 알림 설정 식별자 값 객체
 *
 * - 알림 설정의 고유 식별자를 담당하는 값 객체
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
@Schema(description = "알림 설정 식별자 값 객체")
public class NotificationSettingsId {

    @Schema(description = "알림 설정 고유 식별자 값", example = "550e8400-e29b-41d4-a716-446655440000")
    private final String value;

    /**
     * 새로운 알림 설정 ID 생성
     *
     * @param value UUID 문자열
     */
    private NotificationSettingsId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("알림 설정 ID는 null이거나 빈 값일 수 없습니다.");
        }
        this.value = value;
    }

    /**
     * 새로운 알림 설정 ID 생성 (UUID 자동 생성)
     *
     * @return 새로 생성된 NotificationSettingsId
     */
    public static NotificationSettingsId generate() {
        return new NotificationSettingsId(UUID.randomUUID().toString());
    }

    /**
     * 기존 알림 설정 ID로부터 객체 생성
     *
     * @param value 알림 설정 ID 문자열
     * @return NotificationSettingsId 객체
     */
    public static NotificationSettingsId from(String value) {
        return new NotificationSettingsId(value);
    }
}