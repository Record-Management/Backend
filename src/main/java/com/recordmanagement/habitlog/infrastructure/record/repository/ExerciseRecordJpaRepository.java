package com.recordmanagement.habitlog.infrastructure.record.repository;

import com.recordmanagement.habitlog.infrastructure.record.entity.ExerciseRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 운동 기록 JPA Repository
 */
public interface ExerciseRecordJpaRepository extends JpaRepository<ExerciseRecordEntity, String> {
    
    /**
     * 사용자의 특정 날짜 운동 기록 조회
     */
    Optional<ExerciseRecordEntity> findByUserIdAndRecordDate(String userId, LocalDate recordDate);
    
    /**
     * 사용자의 기간별 운동 기록 조회
     */
    List<ExerciseRecordEntity> findByUserIdAndRecordDateBetween(String userId, LocalDate startDate, LocalDate endDate);
}