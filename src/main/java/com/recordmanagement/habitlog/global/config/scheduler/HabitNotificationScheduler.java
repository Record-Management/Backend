package com.recordmanagement.habitlog.global.config.scheduler;

import com.recordmanagement.habitlog.domain.habit.domain.model.HabitRecord;
import com.recordmanagement.habitlog.domain.habit.domain.repository.HabitRecordRepository;
import com.recordmanagement.habitlog.domain.notification.application.FcmNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 습관별 시간 지정 알림 스케줄러
 *
 * 매분마다 실행되어 사용자가 설정한 시간에 맞춰 습관 알림을 발송합니다.
 *
 * 처리 과정:
 * 1. 현재 시간(HH:mm)을 확인
 * 2. 오늘 날짜의 습관 기록 중 현재 시간에 알림이 설정된 습관 조회
 * 3. 조회된 각 습관에 대해 개별 알림 발송
 *
 * 알림 발송 조건:
 * - notificationEnabled = true (알림 활성화)
 * - notificationTime = 현재 시간 (분 단위 일치)
 * - isCompleted = false (아직 완료하지 않음)
 *
 * 중복 발송 방지:
 * - notificationTime이 현재 시간과 정확히 일치해야 조회되므로 자연스럽게 방지됨
 * - 예: 08:00 설정 → 08:00에만 조회, 08:01~23:59에는 조회 안 됨
 * - 같은 날 알림 시간을 변경하면 변경된 시간에 다시 알림 받을 수 있음
 *
 * 중요 사항:
 * - FCM 푸시 알림만 발송됨 (NotificationHistory에 저장 안 함)
 * - 프론트엔드 앱 호환성을 위해 히스토리 제외 처리
 *
 * 실행 주기: 매분 정각 (예: 08:00, 08:01, 08:02 ...)
 * 실행 시간대: 한국시간(KST) 기준
 *
 * @author 전우선
 * @since 2025.02.07
 * @version 1.0.1
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HabitNotificationScheduler {

    private final HabitRecordRepository habitRecordRepository;
    private final FcmNotificationService fcmNotificationService;

    /**
     * 매분마다 습관별 시간 지정 알림 발송
     * 한국시간(KST) 기준으로 실행
     */
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void sendHabitTimeBasedNotifications() {
        LocalTime currentTime = LocalTime.now().withSecond(0).withNano(0); // 초, 나노초 제거 (HH:mm만 사용)
        LocalDate today = LocalDate.now();

        log.debug("습관별 시간 지정 알림 스케줄러 시작: currentTime={}, today={}", currentTime, today);

        try {
            // 현재 시간에 알림이 필요한 습관 기록 조회
            List<HabitRecord> habitsForNotification = habitRecordRepository.findHabitsForNotification(currentTime, today);

            if (habitsForNotification.isEmpty()) {
                log.debug("현재 시간({})에 알림이 필요한 습관 기록이 없습니다", currentTime);
                return;
            }

            log.info("현재 시간({})에 알림이 필요한 습관 기록 {}개 발견", currentTime, habitsForNotification.size());

            int successCount = 0;
            int failureCount = 0;

            for (HabitRecord habitRecord : habitsForNotification) {
                try {
                    fcmNotificationService.sendHabitTimeBasedNotification(habitRecord);
                    successCount++;
                    log.debug("습관 알림 발송 성공: habitRecordId={}, habitType={}, userId={}",
                            habitRecord.getId().getValue(),
                            habitRecord.getHabitType().getDescription(),
                            habitRecord.getUserId().getValue());
                } catch (Exception e) {
                    failureCount++;
                    log.error("습관 알림 발송 실패: habitRecordId={}, error={}",
                            habitRecord.getId().getValue(), e.getMessage(), e);
                }
            }

            log.info("습관별 시간 지정 알림 스케줄러 완료: 성공 {}건, 실패 {}건", successCount, failureCount);

        } catch (Exception e) {
            log.error("습관별 시간 지정 알림 스케줄러 실행 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
