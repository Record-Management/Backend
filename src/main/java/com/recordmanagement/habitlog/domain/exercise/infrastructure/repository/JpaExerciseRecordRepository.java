package com.recordmanagement.habitlog.domain.exercise.infrastructure.repository;

import com.recordmanagement.habitlog.domain.exercise.infrastructure.entity.ExerciseRecordEntity;
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
    
    int countByUserIdAndRecordDate(String userId, LocalDate recordDate);
    
    // 사용자 ID로 모든 운동 기록 삭제 (회원 탈퇴 시)
    void deleteByUserId(String userId);
}