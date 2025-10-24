package com.recordmanagement.habitlog.domain.notification.infrastructure.entity;

import com.recordmanagement.habitlog.domain.common.BaseEntity;
import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationHistory;
import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationHistoryId;
import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationType;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 알림 히스토리 JPA 엔터티
 *
 * - 알림 히스토리 도메인 모델을 데이터베이스에 저장하기 위한 JPA 엔터티
 * - 도메인 모델과 데이터베이스 간의 변환 담당
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Entity
@Table(name = "notification_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationHistoryEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    /**
     * 도메인 모델로부터 JPA 엔터티 생성
     *
     * @param notificationHistory 도메인 알림 히스토리
     */
    public NotificationHistoryEntity(NotificationHistory notificationHistory) {
        this.userId = notificationHistory.getUserId().getValue();
        this.type = notificationHistory.getType();
        this.title = notificationHistory.getTitle();
        this.message = notificationHistory.getMessage();
        this.isRead = notificationHistory.isRead();
        this.sentAt = notificationHistory.getSentAt();
        
        // BaseEntity의 id와 시간 필드 설정
        this.id = notificationHistory.getId().getValue();
        this.setCreatedAt(notificationHistory.getSentAt()); // 알림은 생성시간 = 발송시간
        this.setUpdatedAt(notificationHistory.getSentAt());
    }

    /**
     * JPA 엔터티를 도메인 모델로 변환
     *
     * @return 도메인 알림 히스토리
     */
    public NotificationHistory toDomain() {
        NotificationHistory notificationHistory = new NotificationHistory(
            UserId.from(this.userId),
            this.type,
            this.title,
            this.message
        );

        // 리플렉션을 사용하여 private 필드 설정
        try {
            setFieldValue(notificationHistory, "id", NotificationHistoryId.from(this.id));
            setFieldValue(notificationHistory, "isRead", this.isRead);
            setFieldValue(notificationHistory, "sentAt", this.sentAt);
        } catch (Exception e) {
            throw new RuntimeException("도메인 모델 변환 중 오류 발생", e);
        }

        return notificationHistory;
    }

    /**
     * 알림을 읽음 상태로 변경
     */
    public void markAsRead() {
        this.isRead = true;
        this.updateTimestamp(); // BaseEntity의 updatedAt 갱신
    }

    /**
     * 리플렉션을 사용하여 필드 값 설정
     */
    private void setFieldValue(Object target, String fieldName, Object value) throws Exception {
        var field = NotificationHistory.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}