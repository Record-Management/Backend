package com.recordmanagement.habitlog.domain.schedule.infrastructure.repository;

import com.recordmanagement.habitlog.domain.schedule.domain.model.NotificationType;
import com.recordmanagement.habitlog.domain.schedule.domain.model.RepeatType;
import com.recordmanagement.habitlog.domain.schedule.infrastructure.entity.ScheduleRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JpaScheduleRecordRepository extends JpaRepository<ScheduleRecordEntity, String> {

    Optional<ScheduleRecordEntity> findByScheduleRecordIdAndUserId(String scheduleRecordId, String userId);

    /**
     * 특정 날짜 범위와 겹치는 일정 조회
     * (일정의 startDate <= 조회 endDate AND 일정의 endDate >= 조회 startDate)
     */
    @Query("SELECT s FROM ScheduleRecordEntity s " +
           "WHERE s.userId = :userId " +
           "AND s.startDate <= :endDate " +
           "AND s.endDate >= :startDate " +
           "ORDER BY s.startDate ASC")
    List<ScheduleRecordEntity> findByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    void deleteByScheduleRecordIdAndUserId(String scheduleRecordId, String userId);

    void deleteByUserId(String userId);

    boolean existsByScheduleRecordIdAndUserId(String scheduleRecordId, String userId);

    /**
     * 알림이 필요한 일정 조회
     * - ONE_DAY_BEFORE: startDate - 1일, 09:00
     * - TWO_DAYS_BEFORE: startDate - 2일, 09:00
     * - CUSTOM: startDate - customDays일, customHours시
     */
    @Query("SELECT s FROM ScheduleRecordEntity s " +
           "WHERE s.notificationType <> 'NONE' " +
           "AND (" +
           "  (s.notificationType = 'ONE_DAY_BEFORE' AND s.startDate = :oneDayAfter AND :hour = 9) " +
           "  OR (s.notificationType = 'TWO_DAYS_BEFORE' AND s.startDate = :twoDaysAfter AND :hour = 9) " +
           "  OR (s.notificationType = 'CUSTOM' AND " +
           "      FUNCTION('DATE_ADD', s.startDate, FUNCTION('INTERVAL', -s.notificationCustomDays, 'DAY')) = :notificationDate " +
           "      AND s.notificationCustomHours = :hour) " +
           ")")
    List<ScheduleRecordEntity> findSchedulesForNotification(
            @Param("notificationDate") LocalDate notificationDate,
            @Param("oneDayAfter") LocalDate oneDayAfter,
            @Param("twoDaysAfter") LocalDate twoDaysAfter,
            @Param("hour") int hour
    );

    /**
     * 반복 일정 조회
     * - 반복 타입이 NONE이 아니고
     * - 반복 종료일이 null이거나 아직 종료되지 않은 일정
     */
    @Query("SELECT s FROM ScheduleRecordEntity s " +
           "WHERE s.repeatType <> 'NONE' " +
           "AND (s.repeatEndsOn IS NULL OR s.repeatEndsOn >= :today)")
    List<ScheduleRecordEntity> findRepeatableSchedules(@Param("today") LocalDate today);
}
