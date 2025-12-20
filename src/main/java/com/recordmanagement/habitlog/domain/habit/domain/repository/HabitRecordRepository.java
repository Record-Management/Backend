package com.recordmanagement.habitlog.domain.habit.domain.repository;

import com.recordmanagement.habitlog.domain.habit.domain.model.HabitRecord;
import com.recordmanagement.habitlog.domain.habit.domain.model.HabitRecordId;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;

import java.time.LocalDate;
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
}