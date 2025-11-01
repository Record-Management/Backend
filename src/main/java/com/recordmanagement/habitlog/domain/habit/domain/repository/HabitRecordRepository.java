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
}