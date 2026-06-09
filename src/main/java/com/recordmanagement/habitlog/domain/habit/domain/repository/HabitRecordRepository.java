package com.recordmanagement.habitlog.domain.habit.domain.repository;

import com.recordmanagement.habitlog.domain.habit.domain.model.HabitRecord;
import com.recordmanagement.habitlog.domain.habit.domain.model.HabitRecordId;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface HabitRecordRepository {
    
    HabitRecord save(HabitRecord habitRecord);
    
    Optional<HabitRecord> findById(HabitRecordId id);
    
    Optional<HabitRecord> findByIdAndUserId(HabitRecordId id, UserId userId);
    
    List<HabitRecord> findByUserIdAndRecordDate(UserId userId, LocalDate recordDate);
    
    List<HabitRecord> findByUserIdAndRecordDateBetween(UserId userId, LocalDate startDate, LocalDate endDate);
    
    void deleteById(HabitRecordId id);
    
    void deleteByIdAndUserId(HabitRecordId id, UserId userId);
    
    void deleteByUserId(String userId);
    
    boolean existsByIdAndUserId(HabitRecordId id, UserId userId);
    
    int countByUserIdAndRecordDate(UserId userId, LocalDate recordDate);

    /**
     * 특정 기간 내 완료된 습관 기록이 있는 날짜 수를 조회 (성능 최적화)
     * - 목표 달성률 계산 시 사용
     * - N번 쿼리 대신 1번 쿼리로 처리
     */
    int countCompletedHabitsByUserIdAndDateRange(UserId userId, LocalDate startDate, LocalDate endDate);

    boolean existsMainRecordByUserIdAndRecordDate(UserId userId, LocalDate recordDate);
    
    /**
     * 특정 날짜 이후의 메인 습관 기록들을 삭제
     * 목표 완료/삭제 시 미래의 메인 습관 기록들을 정리하기 위해 사용
     *
     * @param userId 사용자 ID
     * @param fromDate 삭제할 기준 날짜 (이 날짜 이후의 메인 기록들이 삭제됨)
     * @return 삭제된 메인 기록 수
     */
    int deleteMainRecordsAfterDate(UserId userId, LocalDate fromDate);

    /**
     * 특정 시간에 알림이 필요한 습관 기록들을 조회
     * - 오늘 날짜의 습관 기록 중
     * - 알림이 활성화되어 있고 (notificationEnabled = true)
     * - 알림 시간이 현재 시간과 일치하며 (notificationTime = 현재시간)
     * - 아직 완료하지 않은 (isCompleted = false) 습관 기록들
     *
     * 중복 발송 방지는 시간 일치 조건으로 자연스럽게 처리됨
     * (같은 시간에는 1번만 조회되므로)
     *
     * @param currentTime 현재 시간 (HH:mm)
     * @param today 오늘 날짜
     * @return 알림이 필요한 습관 기록 목록
     */
    List<HabitRecord> findHabitsForNotification(LocalTime currentTime, LocalDate today);
}