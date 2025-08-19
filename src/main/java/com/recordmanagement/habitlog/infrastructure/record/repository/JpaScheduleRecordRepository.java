package com.recordmanagement.habitlog.infrastructure.record.repository;

import com.recordmanagement.habitlog.infrastructure.record.entity.ScheduleRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 일정 기록 JPA Repository
 */
public interface JpaScheduleRecordRepository extends JpaRepository<ScheduleRecordEntity, String> {
    
    /**
     * 사용자와 날짜로 일정 기록 목록 조회 (시작일이 해당 날짜이거나 해당 날짜가 시작일~종료일 사이에 있는 경우)
     */
    @Query("SELECT s FROM ScheduleRecordEntity s WHERE s.userId = :userId " +
           "AND (s.startDate = :date OR (s.startDate <= :date AND (s.endDate IS NULL OR s.endDate >= :date)))")
    List<ScheduleRecordEntity> findByUserIdAndDate(@Param("userId") String userId, @Param("date") LocalDate date);
    
    /**
     * ID와 사용자 ID로 일정 기록 조회
     */
    Optional<ScheduleRecordEntity> findByIdAndUserId(String id, String userId);
}