package com.recordmanagement.habitlog.infrastructure.exercise.repository;

import com.recordmanagement.habitlog.infrastructure.exercise.entity.ExerciseRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JpaExerciseRecordRepository extends JpaRepository<ExerciseRecordEntity, String> {
    
    Optional<ExerciseRecordEntity> findByExerciseRecordIdAndUserId(String exerciseRecordId, String userId);
    
    List<ExerciseRecordEntity> findByUserIdAndRecordDate(String userId, LocalDate recordDate);
    
    List<ExerciseRecordEntity> findByUserIdAndRecordDateBetween(String userId, LocalDate startDate, LocalDate endDate);
    
    void deleteByExerciseRecordIdAndUserId(String exerciseRecordId, String userId);
    
    boolean existsByExerciseRecordIdAndUserId(String exerciseRecordId, String userId);
}