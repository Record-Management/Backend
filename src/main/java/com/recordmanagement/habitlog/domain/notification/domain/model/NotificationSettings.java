package com.recordmanagement.habitlog.domain.notification.domain.model;

import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 알림 설정 도메인 엔터티
 *
 * - 사용자별 알림 설정 관리
 * - User 엔티티와 별도로 분리하여 알림 관련 설정을 독립적으로 관리
 * - 각 알림 타입별로 개별 활성화/비활성화 가능
 *
 * 주요 필드:
 * - id: 알림 설정 고유 식별자
 * - userId: 사용자 ID (User 엔티티와 연관)
 * - dailyRecordNotificationEnabled: 메인 기록 미등록 알림 활성화 여부
 * - exerciseNotificationEnabled: 운동 기록 알림 활성화 여부
 * - habitNotificationEnabled: 습관 기록 알림 활성화 여부
 * - createdAt: 설정 생성 시간
 * - updatedAt: 마지막 수정 시간
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "알림 설정 도메인 엔터티")
public class NotificationSettings {

    @Schema(description = "알림 설정 고유 식별자", example = "notification-1234-uuid")
    private NotificationSettingsId id;

    @Schema(description = "사용자 ID", example = "user-1234-uuid")
    private UserId userId;

    @Schema(description = "메인 기록 미등록 알림 활성화 여부", example = "true")
    private boolean dailyRecordNotificationEnabled;

    @Schema(description = "운동 기록 알림 활성화 여부", example = "true")
    private boolean exerciseNotificationEnabled;

    @Schema(description = "습관 기록 알림 활성화 여부", example = "true")
    private boolean habitNotificationEnabled;

    @Schema(description = "설정 생성 시간", example = "2025-10-23T12:34:56")
    private LocalDateTime createdAt;

    @Schema(description = "마지막 수정 시간", example = "2025-10-23T15:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "알림 센터 마지막 확인 시간", example = "2025-10-23T16:45:30")
    private LocalDateTime lastCheckedAt;

    /**
     * 새로운 알림 설정 생성자
     *
     * - 기본적으로 모든 알림이 활성화된 상태로 생성
     * - 사용자가 온보딩에서 알림 허용 시 호출
     *
     * @param userId 사용자 ID
     */
    public NotificationSettings(UserId userId) {
        this.id = NotificationSettingsId.generate();
        this.userId = userId;
        this.dailyRecordNotificationEnabled = true;
        this.exerciseNotificationEnabled = true;
        this.habitNotificationEnabled = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 특정 알림 설정으로 생성자
     *
     * @param userId 사용자 ID
     * @param dailyRecordEnabled 메인 기록 알림 활성화 여부
     * @param exerciseEnabled 운동 기록 알림 활성화 여부
     * @param habitEnabled 습관 기록 알림 활성화 여부
     */
    public NotificationSettings(UserId userId, boolean dailyRecordEnabled, boolean exerciseEnabled, boolean habitEnabled) {
        this.id = NotificationSettingsId.generate();
        this.userId = userId;
        this.dailyRecordNotificationEnabled = dailyRecordEnabled;
        this.exerciseNotificationEnabled = exerciseEnabled;
        this.habitNotificationEnabled = habitEnabled;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 메인 기록 알림 설정 업데이트
     *
     * @param enabled 활성화 여부
     */
    public void updateDailyRecordNotification(boolean enabled) {
        this.dailyRecordNotificationEnabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 운동 기록 알림 설정 업데이트
     *
     * @param enabled 활성화 여부
     */
    public void updateExerciseNotification(boolean enabled) {
        this.exerciseNotificationEnabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 습관 기록 알림 설정 업데이트
     *
     * @param enabled 활성화 여부
     */
    public void updateHabitNotification(boolean enabled) {
        this.habitNotificationEnabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 모든 알림 설정 업데이트
     *
     * @param dailyRecordEnabled 메인 기록 알림 활성화 여부
     * @param exerciseEnabled 운동 기록 알림 활성화 여부
     * @param habitEnabled 습관 기록 알림 활성화 여부
     */
    public void updateAllNotifications(boolean dailyRecordEnabled, boolean exerciseEnabled, boolean habitEnabled) {
        this.dailyRecordNotificationEnabled = dailyRecordEnabled;
        this.exerciseNotificationEnabled = exerciseEnabled;
        this.habitNotificationEnabled = habitEnabled;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 특정 사용자의 설정인지 확인
     *
     * @param userId 확인할 사용자 ID
     * @return true 동일 사용자, false 다른 사용자
     */
    public boolean belongsToUser(UserId userId) {
        return this.userId.equals(userId);
    }

    /**
     * 알림 센터 마지막 확인 시간 업데이트
     * 
     * 알림 센터 화면 진입 시 자동으로 호출되어 읽음 처리 시점을 기록
     */
    public void updateLastCheckedAt() {
        this.lastCheckedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 모든 알림이 비활성화되어 있는지 확인
     *
     * @return true 모든 알림 비활성화, false 하나 이상 활성화
     */
    public boolean isAllNotificationsDisabled() {
        return !dailyRecordNotificationEnabled && 
               !exerciseNotificationEnabled && 
               !habitNotificationEnabled;
    }
}