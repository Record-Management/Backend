package com.recordmanagement.habitlog.domain.habit.repository;

import com.recordmanagement.habitlog.domain.habit.model.HabitRecord;
import com.recordmanagement.habitlog.domain.habit.model.HabitRecordId;
import com.recordmanagement.habitlog.domain.user.model.UserId;

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
    
    boolean existsByIdAndUserId(HabitRecordId id, UserId userId);
}