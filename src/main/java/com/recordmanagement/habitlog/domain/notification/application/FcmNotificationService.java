package com.recordmanagement.habitlog.domain.notification.application;

import com.recordmanagement.habitlog.domain.notification.application.dto.NotificationMessage;
import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationHistory;
import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationType;
import com.recordmanagement.habitlog.domain.notification.domain.repository.NotificationSettingsRepository;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
import com.recordmanagement.habitlog.domain.notification.infrastructure.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FCM 알림 서비스
 *
 * - 비즈니스 로직에 따른 알림 발송 처리
 * - 사용자별 알림 설정 확인 및 메시지 생성
 * - PushNotificationService를 사용하여 실제 FCM 발송
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmNotificationService {

    private final PushNotificationService pushNotificationService;
    private final UserRepository userRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final com.recordmanagement.habitlog.domain.notification.application.service.NotificationHistoryApplicationService notificationHistoryApplicationService;

    /**
     * 메인 기록 미등록 알림 발송
     * 사용자의 메인 기록 타입에 따라 적절한 메시지를 생성하여 발송
     *
     * @param userId 사용자 ID
     */
    public void sendDailyRecordReminderNotification(UserId userId) {
        log.info("메인 기록 미등록 알림 발송 시작: userId={}", userId.getValue());

        // 사용자 정보 조회
        User user = userRepository.findById(userId)
                .filter(u -> !u.isWithdrawn()) // 탈퇴한 사용자 제외
                .orElse(null);

        if (user == null) {
            log.warn("사용자를 찾을 수 없거나 탈퇴한 사용자입니다: userId={}", userId.getValue());
            return;
        }

        // FCM 토큰 확인
        if (user.getFcmToken() == null || user.getFcmToken().trim().isEmpty()) {
            log.warn("FCM 토큰이 없어 알림을 발송할 수 없습니다: userId={}", userId.getValue());
            return;
        }

        // 알림 설정 확인
        boolean isNotificationEnabled = notificationSettingsRepository.findByUserId(userId)
                .map(settings -> settings.isDailyRecordNotificationEnabled())
                .orElse(false);

        if (!isNotificationEnabled) {
            log.info("메인 기록 알림이 비활성화되어 있습니다: userId={}", userId.getValue());
            return;
        }

        // 메시지 생성
        NotificationMessage message = createDailyRecordReminderMessage(user.getMainRecordType());

        // 추가 데이터 설정
        Map<String, String> data = new HashMap<>();
        data.put("mainType", user.getMainRecordType().name());
        data.put("notificationType", "DAILY_RECORD_REMINDER");

        // 알림 히스토리 저장 (FCM 발송 전에 저장)
        NotificationHistory history = new NotificationHistory(
                userId, 
                NotificationType.DAILY_RECORD_REMINDER, 
                message.getTitle(), 
                message.getBody()
        );
        notificationHistoryApplicationService.saveNotificationHistory(history);

        // FCM 발송
        boolean success = pushNotificationService.sendNotification(
                user.getFcmToken(),
                message.getTitle(),
                message.getBody(),
                data
        );

        if (success) {
            log.info("메인 기록 미등록 알림 발송 성공: userId={}, mainType={}", 
                    userId.getValue(), user.getMainRecordType());
        } else {
            log.error("메인 기록 미등록 알림 발송 실패: userId={}", userId.getValue());
        }
    }

    /**
     * 여러 사용자에게 메인 기록 미등록 알림 일괄 발송
     *
     * @param userIds 사용자 ID 목록
     */
    public void sendDailyRecordReminderNotifications(List<UserId> userIds) {
        log.info("메인 기록 미등록 알림 일괄 발송 시작: 대상 사용자 {}명", userIds.size());

        int successCount = 0;
        int failureCount = 0;

        for (UserId userId : userIds) {
            try {
                sendDailyRecordReminderNotification(userId);
                successCount++;
            } catch (Exception e) {
                log.error("메인 기록 미등록 알림 발송 중 오류 발생: userId={}, error={}", 
                        userId.getValue(), e.getMessage());
                failureCount++;
            }
        }

        log.info("메인 기록 미등록 알림 일괄 발송 완료: 성공 {}건, 실패 {}건", successCount, failureCount);
    }

    /**
     * 메인 기록 타입에 따른 알림 메시지 생성
     *
     * @param mainRecordType 메인 기록 타입
     * @return 알림 메시지
     */
    private NotificationMessage createDailyRecordReminderMessage(RecordType mainRecordType) {
        String title;
        String body;

        switch (mainRecordType) {
            case EXERCISE:
                title = "오늘 운동 기록을 등록하지 않았어요";
                body = "꾸준한 운동 기록으로 건강한 습관을 만들어보세요!";
                break;
            case HABIT:
                title = "오늘 습관 기록을 확인해보세요";
                body = "목표 달성을 위해 오늘의 습관을 체크해주세요!";
                break;
            case DAILY:
            default:
                title = "오늘 하루는 어떠셨나요?";
                body = "오늘의 소중한 순간을 기록으로 남겨보세요!";
                break;
        }

        return new NotificationMessage(title, body);
    }

    /**
     * 테스트용 알림 발송
     * 개발/테스트 환경에서 FCM 연동 테스트용
     *
     * @param userId 사용자 ID
     * @param title 알림 제목
     * @param body 알림 내용
     * @return 발송 성공 여부
     */
    public boolean sendTestNotification(UserId userId, String title, String body) {
        log.info("테스트 알림 발송: userId={}", userId.getValue());

        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getFcmToken() == null) {
            log.warn("테스트 알림 발송 실패: 사용자 없음 또는 FCM 토큰 없음");
            return false;
        }

        // 테스트 알림 히스토리 저장
        NotificationHistory history = new NotificationHistory(
                userId, 
                NotificationType.TEST, 
                title, 
                body
        );
        notificationHistoryApplicationService.saveNotificationHistory(history);

        Map<String, String> data = new HashMap<>();
        data.put("notificationType", "TEST");

        return pushNotificationService.sendNotification(
                user.getFcmToken(),
                title,
                body,
                data
        );
    }
}