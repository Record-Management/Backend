package com.recordmanagement.habitlog.domain.notification.application;

import com.recordmanagement.habitlog.domain.notification.application.dto.NotificationMessage;
import com.recordmanagement.habitlog.domain.notification.application.strategy.NotificationMessageStrategyFactory;
import com.recordmanagement.habitlog.domain.notification.application.util.NotificationImageUtil;
import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationHistory;
import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationType;
import com.recordmanagement.habitlog.domain.notification.application.service.NotificationApplicationService;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
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
 * OCP 적용: Strategy 패턴을 통한 확장 가능한 설계
 * DIP 적용: NotificationSender 추상화에 의존
 * - 새로운 기록 타입 추가 시 기존 코드 수정 없이 확장 가능
 * - Factory 패턴을 통한 전략 관리로 switch 문 제거
 * - Infrastructure 구체 클래스 의존성 제거
 * - 비즈니스 로직에 따른 알림 발송 처리
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 3.0.0 (OCP + DIP 적용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmNotificationService {

    private final NotificationSender notificationSender;
    private final UserRepository userRepository;
    private final NotificationApplicationService notificationApplicationService;
    private final NotificationMessageStrategyFactory messageStrategyFactory;
    private final com.recordmanagement.habitlog.domain.notification.application.service.NotificationHistoryApplicationService notificationHistoryApplicationService;

    /**
     * 메인 기록 미등록 알림 발송
     * 사용자의 메인 기록 타입에 따라 적절한 메시지를 생성하여 발송
     *
     * @param userId 사용자 ID
     */
    @Transactional
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

        // 알림 설정 확인 (일관성 있는 서비스 메서드 사용)
        boolean isNotificationEnabled = notificationApplicationService.isDailyRecordNotificationEnabled(userId);

        if (!isNotificationEnabled) {
            log.info("메인 기록 알림이 비활성화되어 있습니다: userId={}", userId.getValue());
            return;
        }

        // 메시지 생성 (OCP 적용: Strategy 패턴 사용)
        NotificationMessage message = messageStrategyFactory
                .getStrategy(user.getMainRecordType())
                .createDailyRecordReminderMessage();

        // 추가 데이터 설정
        Map<String, String> data = new HashMap<>();
        data.put("mainType", user.getMainRecordType().name());
        data.put("notificationType", "DAILY_RECORD_REMINDER");
        data.put("imageUrl", NotificationImageUtil.getImageUrl(user.getMainRecordType()));

        // 알림 발송 (DIP 적용: 추상화된 NotificationSender 사용)
        boolean success = notificationSender.sendNotification(
                user.getFcmToken(),
                message.getTitle(),
                message.getBody(),
                data
        );

        if (success) {
            // 알림 발송 성공 시에만 히스토리 저장
            NotificationHistory history = new NotificationHistory(
                    userId, 
                    NotificationType.DAILY_RECORD_REMINDER, 
                    message.getTitle(), 
                    message.getBody()
            );
            notificationHistoryApplicationService.saveNotificationHistory(history);
            
            log.info("메인 기록 미등록 알림 발송 성공: userId={}, mainType={}", 
                    userId.getValue(), user.getMainRecordType());
        } else {
            log.error("메인 기록 미등록 알림 발송 실패: userId={}", userId.getValue());
        }
    }

    /**
     * 목표 설정 미완료 알림 발송
     * 목표를 설정하지 않은 사용자에게 목표 설정을 유도하는 알림 발송
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void sendGoalSettingReminderNotification(UserId userId) {
        log.info("목표 설정 미완료 알림 발송 시작: userId={}", userId.getValue());

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

        // 목표 설정 알림 설정 확인
        boolean isNotificationEnabled = notificationApplicationService.isGoalSettingNotificationEnabled(userId);

        if (!isNotificationEnabled) {
            log.info("목표 설정 알림이 비활성화되어 있습니다: userId={}", userId.getValue());
            return;
        }

        // 목표 설정 알림 메시지
        String title = "HabitLog";
        String body = "목표를 설정해서 습관을 시작해보세요!";

        // 추가 데이터 설정
        Map<String, String> data = new HashMap<>();
        data.put("notificationType", "GOAL_SETTING_REMINDER");
        data.put("imageUrl", NotificationImageUtil.getGoalSettingImageUrl());

        // 알림 발송 (DIP 적용: 추상화된 NotificationSender 사용)
        boolean success = notificationSender.sendNotification(
                user.getFcmToken(),
                title,
                body,
                data
        );

        if (success) {
            // 알림 발송 성공 시에만 히스토리 저장
            NotificationHistory history = new NotificationHistory(
                    userId, 
                    NotificationType.GOAL_SETTING_REMINDER, 
                    title, 
                    body
            );
            notificationHistoryApplicationService.saveNotificationHistory(history);
            
            log.info("목표 설정 미완료 알림 발송 성공: userId={}", userId.getValue());
        } else {
            log.error("목표 설정 미완료 알림 발송 실패: userId={}", userId.getValue());
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
     * 테스트용 알림 발송
     * 개발/테스트 환경에서 FCM 연동 테스트용
     *
     * @param userId 사용자 ID
     * @param title 알림 제목
     * @param body 알림 내용
     * @return 발송 성공 여부
     */
    @Transactional
    public boolean sendTestNotification(UserId userId, String title, String body) {
        log.info("테스트 알림 발송: userId={}", userId.getValue());

        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getFcmToken() == null) {
            log.warn("테스트 알림 발송 실패: 사용자 없음 또는 FCM 토큰 없음");
            return false;
        }

        Map<String, String> data = new HashMap<>();
        data.put("notificationType", "TEST");

        boolean success = notificationSender.sendNotification(
                user.getFcmToken(),
                title,
                body,
                data
        );
        
        if (success) {
            // 테스트 알림 발송 성공 시에만 히스토리 저장
            NotificationHistory history = new NotificationHistory(
                    userId, 
                    NotificationType.TEST, 
                    title, 
                    body
            );
            notificationHistoryApplicationService.saveNotificationHistory(history);
        }
        
        return success;
    }
}