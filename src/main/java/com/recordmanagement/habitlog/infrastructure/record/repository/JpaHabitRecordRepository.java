package com.recordmanagement.habitlog.infrastructure.record.repository;

import com.recordmanagement.habitlog.domain.record.model.HabitType;
import com.recordmanagement.habitlog.infrastructure.record.entity.HabitRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 습관 기록 JPA Repository
 */
public interface JpaHabitRecordRepository extends JpaRepository<HabitRecordEntity, String> {
    
    /**
     * 사용자와 기록 날짜로 습관 기록 목록 조회
     */
    List<HabitRecordEntity> findByUserIdAndRecordDate(String userId, LocalDate recordDate);
    
    /**
     * 사용자, 기록 날짜, 습관 타입으로 습관 기록 조회
     */
    Optional<HabitRecordEntity> findByUserIdAndRecordDateAndHabitType(String userId, LocalDate recordDate, HabitType habitType);
}