package com.recordmanagement.habitlog.domain.notification.domain.model;

import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 알림 히스토리 도메인 엔터티
 *
 * - 발송된 알림의 기록을 저장하여 앱 내에서 알림 목록 조회 가능
 * - FCM 푸시 알림과 별도로 앱 내 알림 센터 기능 제공
 * - 사용자별 알림 읽음 상태 관리
 *
 * 주요 필드:
 * - id: 알림 히스토리 고유 식별자
 * - userId: 사용자 ID
 * - type: 알림 타입 (DAILY_RECORD_REMINDER, EXERCISE_REMINDER 등)
 * - title: 알림 제목
 * - message: 알림 내용
 * - isRead: 읽음 여부
 * - sentAt: 발송 시간
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "알림 히스토리 도메인 엔터티")
public class NotificationHistory {

    @Schema(description = "알림 히스토리 고유 식별자", example = "notification-history-1234-uuid")
    private NotificationHistoryId id;

    @Schema(description = "사용자 ID", example = "user-1234-uuid")
    private UserId userId;

    @Schema(description = "알림 타입", example = "DAILY_RECORD_REMINDER")
    private NotificationType type;

    @Schema(description = "알림 제목", example = "오늘 운동 기록을 등록하지 않았어요")
    private String title;

    @Schema(description = "알림 내용", example = "꾸준한 운동 기록으로 건강한 습관을 만들어보세요!")
    private String message;

    @Schema(description = "읽음 여부", example = "false")
    private boolean isRead;

    @Schema(description = "발송 시간", example = "2025-10-23T20:00:00")
    private LocalDateTime sentAt;

    @Schema(description = "읽은 시간", example = "2025-10-23T20:05:00")
    private LocalDateTime readAt;

    /**
     * 새로운 알림 히스토리 생성자
     *
     * @param userId 사용자 ID
     * @param type 알림 타입
     * @param title 알림 제목
     * @param message 알림 내용
     */
    public NotificationHistory(UserId userId, NotificationType type, String title, String message) {
        this.id = NotificationHistoryId.generate();
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.isRead = false;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * 알림을 읽음으로 처리
     */
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }

    /**
     * 알림을 미읽음으로 처리
     */
    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }

    /**
     * 특정 사용자의 알림인지 확인
     *
     * @param userId 확인할 사용자 ID
     * @return true 동일 사용자, false 다른 사용자
     */
    public boolean belongsToUser(UserId userId) {
        return this.userId.equals(userId);
    }
}