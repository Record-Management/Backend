package com.recordmanagement.habitlog.infrastructure.habit.repository;

import com.recordmanagement.habitlog.infrastructure.habit.entity.HabitRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JpaHabitRecordRepository extends JpaRepository<HabitRecordEntity, String> {
    
    Optional<HabitRecordEntity> findByHabitRecordIdAndUserId(String habitRecordId, String userId);
    
    List<HabitRecordEntity> findByUserIdAndRecordDate(String userId, LocalDate recordDate);
    
    List<HabitRecordEntity> findByUserIdAndRecordDateBetween(String userId, LocalDate startDate, LocalDate endDate);
    
    void deleteByHabitRecordIdAndUserId(String habitRecordId, String userId);
    
    boolean existsByHabitRecordIdAndUserId(String habitRecordId, String userId);
}