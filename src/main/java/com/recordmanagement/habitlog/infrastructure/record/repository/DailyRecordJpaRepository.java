package com.recordmanagement.habitlog.infrastructure.record.repository;

import com.recordmanagement.habitlog.infrastructure.record.entity.DailyRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 일상 기록 JPA Repository
 */
public interface DailyRecordJpaRepository extends JpaRepository<DailyRecordEntity, String> {
    
    /**
     * 사용자의 특정 날짜 일상 기록 조회
     */
    Optional<DailyRecordEntity> findByUserIdAndRecordDate(String userId, LocalDate recordDate);
    
    /**
     * 사용자의 기간별 일상 기록 조회
     */
    List<DailyRecordEntity> findByUserIdAndRecordDateBetween(String userId, LocalDate startDate, LocalDate endDate);
}