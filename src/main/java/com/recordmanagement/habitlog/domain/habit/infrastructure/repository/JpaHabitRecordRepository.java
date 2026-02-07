package com.recordmanagement.habitlog.domain.auth.infrastructure.habit.repository;

import com.recordmanagement.habitlog.domain.habit.infrastructure.entity.HabitRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface JpaHabitRecordRepository extends JpaRepository<HabitRecordEntity, String> {
    
    Optional<HabitRecordEntity> findByHabitRecordIdAndUserId(String habitRecordId, String userId);
    
    List<HabitRecordEntity> findByUserIdAndRecordDate(String userId, LocalDate recordDate);
    
    List<HabitRecordEntity> findByUserIdAndRecordDateBetween(String userId, LocalDate startDate, LocalDate endDate);
    
    void deleteByHabitRecordIdAndUserId(String habitRecordId, String userId);
    
    boolean existsByHabitRecordIdAndUserId(String habitRecordId, String userId);
    
    int countByUserIdAndRecordDate(String userId, LocalDate recordDate);
    
    // 사용자 ID로 모든 습관 기록 삭제 (회원 탈퇴 시)
    void deleteByUserId(String userId);
    
    // 특정 날짜에 메인 습관 기록 존재 여부 확인
    boolean existsByUserIdAndRecordDateAndIsMainRecord(String userId, LocalDate recordDate, boolean isMainRecord);
    
    /**
     * 특정 날짜 이후의 메인 습관 기록들을 삭제
     *
     * @param userId 사용자 ID
     * @param fromDate 삭제할 기준 날짜 (이 날짜 이후의 메인 기록들이 삭제됨)
     * @param isMainRecord 메인 기록 여부 (true: 메인 기록만 삭제)
     * @return 삭제된 기록 수
     */
    int deleteByUserIdAndRecordDateAfterAndIsMainRecord(String userId, LocalDate fromDate, boolean isMainRecord);

    /**
     * 특정 시간에 알림이 필요한 습관 기록들을 조회
     *
     * 조건:
     * - 오늘 날짜의 습관 기록
     * - 알림이 활성화되어 있고 (notificationEnabled = true)
     * - 알림 시간이 현재 시간과 일치하며 (notificationTime = currentTime)
     * - 아직 완료하지 않은 (isCompleted = false)
     * - 오늘 아직 알림을 보내지 않음 (lastNotificationSentDate != today)
     *
     * @param currentTime 현재 시간 (HH:mm)
     * @param today 오늘 날짜
     * @return 알림이 필요한 습관 기록 목록
     */
    @Query("SELECT h FROM HabitRecordEntity h " +
           "WHERE h.recordDate = :today " +
           "AND h.notificationEnabled = true " +
           "AND h.notificationTime = :currentTime " +
           "AND h.isCompleted = false " +
           "AND (h.lastNotificationSentDate IS NULL OR h.lastNotificationSentDate <> :today)")
    List<HabitRecordEntity> findHabitsForNotification(
            @Param("currentTime") LocalTime currentTime,
            @Param("today") LocalDate today
    );
}