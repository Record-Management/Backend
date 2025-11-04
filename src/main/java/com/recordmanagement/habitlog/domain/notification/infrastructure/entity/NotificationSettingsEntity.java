package com.recordmanagement.habitlog.domain.notification.infrastructure.entity;

import com.recordmanagement.habitlog.domain.common.BaseEntity;
import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationSettings;
import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationSettingsId;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 설정 JPA 엔터티
 *
 * - 알림 설정 도메인 모델을 데이터베이스에 저장하기 위한 JPA 엔터티
 * - 도메인 모델과 데이터베이스 간의 변환 담당
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Entity
@Table(name = "notification_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSettingsEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "daily_record_notification_enabled", nullable = false)
    private boolean dailyRecordNotificationEnabled;

    @Column(name = "exercise_notification_enabled", nullable = false)
    private boolean exerciseNotificationEnabled;

    @Column(name = "habit_notification_enabled", nullable = false)
    private boolean habitNotificationEnabled;

    @Column(name = "goal_setting_notification_enabled", nullable = false)
    private boolean goalSettingNotificationEnabled;

    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;

    /**
     * 도메인 모델로부터 JPA 엔터티 생성
     *
     * @param notificationSettings 도메인 알림 설정
     */
    public NotificationSettingsEntity(NotificationSettings notificationSettings) {
        this.id = notificationSettings.getId().getValue();
        this.userId = notificationSettings.getUserId().getValue();
        this.dailyRecordNotificationEnabled = notificationSettings.isDailyRecordNotificationEnabled();
        this.exerciseNotificationEnabled = notificationSettings.isExerciseNotificationEnabled();
        this.habitNotificationEnabled = notificationSettings.isHabitNotificationEnabled();
        this.goalSettingNotificationEnabled = notificationSettings.isGoalSettingNotificationEnabled();
        this.lastCheckedAt = notificationSettings.getLastCheckedAt();
        this.setCreatedAt(notificationSettings.getCreatedAt());
        this.setUpdatedAt(notificationSettings.getUpdatedAt());
    }

    /**
     * JPA 엔터티를 도메인 모델로 변환
     *
     * @return 도메인 알림 설정
     */
    public NotificationSettings toDomain() {
        NotificationSettings notificationSettings = new NotificationSettings(
            UserId.from(this.userId),
            this.dailyRecordNotificationEnabled,
            this.exerciseNotificationEnabled,
            this.habitNotificationEnabled,
            this.goalSettingNotificationEnabled
        );

        // 리플렉션을 사용하여 private 필드 설정
        try {
            setFieldValue(notificationSettings, "id", NotificationSettingsId.from(this.id));
            setFieldValue(notificationSettings, "lastCheckedAt", this.lastCheckedAt);
            setFieldValue(notificationSettings, "createdAt", this.getCreatedAt());
            setFieldValue(notificationSettings, "updatedAt", this.getUpdatedAt());
        } catch (Exception e) {
            throw new RuntimeException("도메인 모델 변환 중 오류 발생", e);
        }

        return notificationSettings;
    }

    /**
     * 도메인 모델의 변경사항을 엔터티에 반영
     *
     * @param notificationSettings 업데이트된 도메인 알림 설정
     */
    public void updateFromDomain(NotificationSettings notificationSettings) {
        this.dailyRecordNotificationEnabled = notificationSettings.isDailyRecordNotificationEnabled();
        this.exerciseNotificationEnabled = notificationSettings.isExerciseNotificationEnabled();
        this.habitNotificationEnabled = notificationSettings.isHabitNotificationEnabled();
        this.goalSettingNotificationEnabled = notificationSettings.isGoalSettingNotificationEnabled();
        this.setUpdatedAt(notificationSettings.getUpdatedAt());
    }

    /**
     * 리플렉션을 사용하여 필드 값 설정
     */
    private void setFieldValue(Object target, String fieldName, Object value) throws Exception {
        var field = NotificationSettings.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}