package com.recordmanagement.habitlog.domain.exercise.domain.repository;

import com.recordmanagement.habitlog.domain.exercise.domain.model.ExerciseRecord;
import com.recordmanagement.habitlog.domain.exercise.domain.model.ExerciseRecordId;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExerciseRecordRepository {
    
    ExerciseRecord save(ExerciseRecord exerciseRecord);
    
    Optional<ExerciseRecord> findById(ExerciseRecordId id);
    
    Optional<ExerciseRecord> findByIdAndUserId(ExerciseRecordId id, UserId userId);
    
    List<ExerciseRecord> findByUserIdAndRecordDate(UserId userId, LocalDate recordDate);
    
    List<ExerciseRecord> findByUserIdAndRecordDateBetween(UserId userId, LocalDate startDate, LocalDate endDate);
    
    void deleteById(ExerciseRecordId id);
    
    void deleteByIdAndUserId(ExerciseRecordId id, UserId userId);
    
    void deleteByUserId(String userId);
    
    boolean existsByIdAndUserId(ExerciseRecordId id, UserId userId);
    
    int countByUserIdAndRecordDate(UserId userId, LocalDate recordDate);
}