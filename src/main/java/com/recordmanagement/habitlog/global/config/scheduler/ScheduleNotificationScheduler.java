package com.recordmanagement.habitlog.global.config.scheduler;

import com.recordmanagement.habitlog.domain.notification.application.FcmNotificationService;
import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord;
import com.recordmanagement.habitlog.domain.schedule.domain.repository.ScheduleRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 일정 알림 스케줄러
 *
 * 매분마다 실행되어 일정의 알림 설정에 맞춰 알림을 발송합니다.
 *
 * 처리 과정:
 * 1. 현재 시간(HH:mm)과 날짜 확인
 * 2. 알림이 필요한 일정 조회:
 *    - ONE_DAY_BEFORE: 일정 시작일 1일 전 오전 9시
 *    - TWO_DAYS_BEFORE: 일정 시작일 2일 전 오전 9시
 *    - CUSTOM: 일정 시작일 당일 사용자 지정 시간
 *    - NONE: 알림 없음 (조회 제외)
 * 3. 조회된 각 일정에 대해 개별 알림 발송
 *
 * 알림 발송 조건:
 * - notificationType != NONE
 * - 알림 시간이 현재 시간과 일치
 *
 * 중복 발송 방지:
 * - 매분 실행되지만 알림 시간이 정확히 일치해야 조회되므로 자연스럽게 방지됨
 * - 예: ONE_DAY_BEFORE → 일정 시작일 1일 전 09:00에만 조회
 *
 * 중요 사항:
 * - FCM 푸시 알림 + NotificationHistory에 저장
 * - 알림 message 필드에는 일정명이 저장됨 (다른 알림과 차이점)
 * - 사용자의 scheduleNotificationEnabled 설정 확인
 *
 * 실행 주기: 매분 정각 (예: 08:00, 08:01, 08:02 ...)
 * 실행 시간대: 한국시간(KST) 기준
 *
 * @author 전우선
 * @since 2026.05.31
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleNotificationScheduler {

    private final ScheduleRecordRepository scheduleRecordRepository;
    private final FcmNotificationService fcmNotificationService;

    /**
     * 매분마다 일정 알림 발송
     * 한국시간(KST) 기준으로 실행
     */
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void sendScheduleNotifications() {
        LocalTime currentTime = LocalTime.now().withSecond(0).withNano(0); // 초, 나노초 제거 (HH:mm만 사용)
        LocalDate today = LocalDate.now();
        int currentHour = currentTime.getHour();
        int currentMinute = currentTime.getMinute();

        log.debug("일정 알림 스케줄러 시작: currentTime={}, today={}", currentTime, today);

        try {
            // 현재 시간에 알림이 필요한 일정 조회
            List<ScheduleRecord> schedulesForNotification = scheduleRecordRepository.findSchedulesForNotification(
                    today, currentHour, currentMinute
            );

            if (schedulesForNotification.isEmpty()) {
                log.debug("현재 시간({})에 알림이 필요한 일정이 없습니다", currentTime);
                return;
            }

            log.info("현재 시간({})에 알림이 필요한 일정 {}개 발견", currentTime, schedulesForNotification.size());

            int successCount = 0;
            int failureCount = 0;

            for (ScheduleRecord schedule : schedulesForNotification) {
                try {
                    fcmNotificationService.sendScheduleNotification(schedule);
                    successCount++;
                    log.debug("일정 알림 발송 성공: scheduleRecordId={}, title={}, userId={}",
                            schedule.getId().value(),
                            schedule.getTitle(),
                            schedule.getUserId().getValue());
                } catch (Exception e) {
                    failureCount++;
                    log.error("일정 알림 발송 실패: scheduleRecordId={}, error={}",
                            schedule.getId().value(), e.getMessage(), e);
                }
            }

            log.info("일정 알림 스케줄러 완료: 성공 {}건, 실패 {}건", successCount, failureCount);

        } catch (Exception e) {
            log.error("일정 알림 스케줄러 실행 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
