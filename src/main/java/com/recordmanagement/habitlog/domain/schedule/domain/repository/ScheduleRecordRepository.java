package com.recordmanagement.habitlog.domain.schedule.domain.repository;

import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord;
import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecordId;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleRecordRepository {

    ScheduleRecord save(ScheduleRecord scheduleRecord);

    Optional<ScheduleRecord> findById(ScheduleRecordId id);

    Optional<ScheduleRecord> findByIdAndUserId(ScheduleRecordId id, UserId userId);

    /**
     * 사용자의 특정 날짜 범위 내 일정 기록 조회 (캘린더용)
     * startDate와 endDate가 겹치는 모든 일정을 조회
     */
    List<ScheduleRecord> findByUserIdAndDateRange(UserId userId, LocalDate startDate, LocalDate endDate);

    void deleteById(ScheduleRecordId id);

    void deleteByIdAndUserId(ScheduleRecordId id, UserId userId);

    void deleteByUserId(String userId);

    boolean existsByIdAndUserId(ScheduleRecordId id, UserId userId);

    /**
     * 알림이 필요한 일정 조회
     * - 알림 타입이 NONE이 아니고
     * - 알림 발송 날짜/시간/분이 현재와 일치하는 일정
     */
    List<ScheduleRecord> findSchedulesForNotification(LocalDate notificationDate, int notificationHour, int notificationMinute);

    /**
     * 반복 일정 조회 (전체 사용자)
     * - 반복 타입이 NONE이 아니고
     * - 반복 종료일이 null이거나 아직 종료되지 않은 일정
     */
    List<ScheduleRecord> findRepeatableSchedules();

    /**
     * 특정 사용자의 반복 일정 조회 (성능 최적화)
     * - 반복 타입이 NONE이 아니고
     * - 반복 종료일이 null이거나 아직 종료되지 않은 일정
     * - 특정 사용자로 필터링 (DB 레벨에서 필터링)
     */
    List<ScheduleRecord> findRepeatableSchedulesByUserId(UserId userId);

    /**
     * 오늘 생성된 일정 개수 조회 (createdAt 기준)
     * - 일정 생성 제한 검증용
     */
    int countByUserIdAndCreatedAtToday(UserId userId, LocalDate today);
}
