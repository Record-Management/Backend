package com.recordmanagement.habitlog.application.notification;

import com.recordmanagement.habitlog.domain.user.event.UserWithdrawnEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 알림 관련 도메인 이벤트 핸들러
 *
 * - 사용자 관련 이벤트 발생 시 알림 데이터 정리
 * - 비동기 처리를 통한 성능 최적화
 * - 느슨한 결합을 통한 사이드 이펙트 처리
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationApplicationService notificationApplicationService;
    private final NotificationHistoryApplicationService notificationHistoryApplicationService;

    /**
     * 사용자 탈퇴 이벤트 처리
     * 알림 설정과 알림 히스토리를 삭제합니다.
     *
     * @param event 사용자 탈퇴 이벤트
     */
    @EventListener
    @Async
    public void handleUserWithdrawn(UserWithdrawnEvent event) {
        log.info("사용자 탈퇴 이벤트 처리 시작: userId={}", event.getUserId().getValue());

        try {
            // 알림 설정 삭제
            notificationApplicationService.deleteNotificationSettings(event.getUserId());
            log.info("회원탈퇴 이벤트 - 알림 설정 삭제 완료: userId={}", event.getUserId().getValue());
        } catch (Exception e) {
            log.warn("회원탈퇴 이벤트 - 알림 설정 삭제 실패: userId={}, error={}", 
                    event.getUserId().getValue(), e.getMessage());
        }

        try {
            // 알림 히스토리 삭제
            notificationHistoryApplicationService.deleteNotificationHistory(event.getUserId());
            log.info("회원탈퇴 이벤트 - 알림 히스토리 삭제 완료: userId={}", event.getUserId().getValue());
        } catch (Exception e) {
            log.warn("회원탈퇴 이벤트 - 알림 히스토리 삭제 실패: userId={}, error={}", 
                    event.getUserId().getValue(), e.getMessage());
        }

        log.info("사용자 탈퇴 이벤트 처리 완료: userId={}", event.getUserId().getValue());
    }
}