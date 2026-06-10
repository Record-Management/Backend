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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
            // 1. 일반 일정 조회 (현재 시간에 알림이 필요한 일정)
            List<ScheduleRecord> schedulesForNotification = new ArrayList<>(
                scheduleRecordRepository.findSchedulesForNotification(today, currentHour, currentMinute)
            );

            // 2. 반복 일정 조회 및 필터링 (오늘 알림을 보내야 하는 반복 일정)
            List<ScheduleRecord> repeatSchedules = scheduleRecordRepository.findRepeatableSchedules().stream()
                .filter(schedule -> shouldSendNotificationToday(schedule, today, currentHour, currentMinute))
                .toList();

            // 중복 제거하며 반복 일정 추가
            Set<String> existingIds = schedulesForNotification.stream()
                .map(s -> s.getId().value())
                .collect(java.util.stream.Collectors.toSet());

            for (ScheduleRecord repeatSchedule : repeatSchedules) {
                if (!existingIds.contains(repeatSchedule.getId().value())) {
                    schedulesForNotification.add(repeatSchedule);
                }
            }

            if (schedulesForNotification.isEmpty()) {
                log.debug("현재 시간({})에 알림이 필요한 일정이 없습니다", currentTime);
                return;
            }

            log.info("현재 시간({})에 알림이 필요한 일정 {}개 발견 (반복 일정 포함)", currentTime, schedulesForNotification.size());

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

    /**
     * 반복 일정이 오늘 알림을 보내야 하는지 판단
     *
     * @param schedule 반복 일정
     * @param today 오늘 날짜
     * @param currentHour 현재 시간
     * @param currentMinute 현재 분
     * @return 알림을 보내야 하면 true
     */
    private boolean shouldSendNotificationToday(ScheduleRecord schedule, LocalDate today, int currentHour, int currentMinute) {
        // 알림이 NONE이면 발송 안 함
        if (schedule.getNotificationType() == com.recordmanagement.habitlog.domain.schedule.domain.model.NotificationType.NONE) {
            return false;
        }

        // 반복 종료일이 지났으면 발송 안 함
        LocalDate repeatEndsOn = schedule.getRepeatEndsOn();
        if (repeatEndsOn != null && repeatEndsOn.isBefore(today)) {
            return false;
        }

        // 알림 타입별로 목표 날짜 계산
        LocalDate targetScheduleDate = null;

        switch (schedule.getNotificationType()) {
            case ONE_DAY_BEFORE:
                // 내일이 일정 날짜인지 확인 & 시간이 09:00인지
                if (currentHour != 9 || currentMinute != 0) {
                    return false;
                }
                targetScheduleDate = today.plusDays(1);
                break;

            case TWO_DAYS_BEFORE:
                // 모레가 일정 날짜인지 확인 & 시간이 09:00인지
                if (currentHour != 9 || currentMinute != 0) {
                    return false;
                }
                targetScheduleDate = today.plusDays(2);
                break;

            case CUSTOM:
                // 오늘이 일정 날짜인지 확인 & 시간이 맞는지
                if (schedule.getNotificationCustomHours() == null || schedule.getNotificationCustomMinutes() == null) {
                    return false;
                }
                if (currentHour != schedule.getNotificationCustomHours() || currentMinute != schedule.getNotificationCustomMinutes()) {
                    return false;
                }
                targetScheduleDate = today;
                break;

            default:
                return false;
        }

        // 목표 날짜가 반복 일정에 해당하는지 확인
        return isScheduleDateInRepeat(schedule, targetScheduleDate);
    }

    /**
     * 특정 날짜가 반복 일정에 해당하는지 확인
     *
     * @param schedule 반복 일정
     * @param targetDate 확인할 날짜
     * @return 해당하면 true
     */
    private boolean isScheduleDateInRepeat(ScheduleRecord schedule, LocalDate targetDate) {
        LocalDate scheduleStart = schedule.getStartDate();
        LocalDate scheduleEnd = schedule.getEndDate();
        LocalDate repeatEndsOn = schedule.getRepeatEndsOn();
        com.recordmanagement.habitlog.domain.schedule.domain.model.RepeatType repeatType = schedule.getRepeatType();

        // 반복 종료일 체크
        if (repeatEndsOn != null && targetDate.isAfter(repeatEndsOn)) {
            return false;
        }

        // 일정 시작일 이전이면 해당 안 됨
        if (targetDate.isBefore(scheduleStart)) {
            return false;
        }

        switch (repeatType) {
            case NONE:
                // 반복 없음: startDate ~ endDate 범위만
                return !targetDate.isBefore(scheduleStart) && !targetDate.isAfter(scheduleEnd);

            case DAY:
                // 매일 반복: startDate 이후 매일
                return !targetDate.isBefore(scheduleStart);

            case WEEK:
                // 매주 반복: 같은 요일인지 확인
                if (targetDate.isBefore(scheduleStart)) {
                    return false;
                }
                // 시작일과 같은 요일인지 확인
                long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(scheduleStart, targetDate);
                return daysBetween % 7 == 0;

            case MONTH:
                // 매월 반복: 같은 날짜인지 확인
                if (targetDate.isBefore(scheduleStart)) {
                    return false;
                }
                // 시작일과 같은 일(day of month)인지 확인
                return targetDate.getDayOfMonth() == scheduleStart.getDayOfMonth();

            case YEAR:
                // 매년 반복: 같은 월-일인지 확인
                if (targetDate.isBefore(scheduleStart)) {
                    return false;
                }
                return targetDate.getMonthValue() == scheduleStart.getMonthValue() &&
                       targetDate.getDayOfMonth() == scheduleStart.getDayOfMonth();

            default:
                return false;
        }
    }
}
