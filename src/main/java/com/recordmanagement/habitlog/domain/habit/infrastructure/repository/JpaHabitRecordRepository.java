package com.recordmanagement.habitlog.domain.auth.infrastructure.habit.repository;

import com.recordmanagement.habitlog.domain.habit.infrastructure.entity.HabitRecordEntity;
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
    
    int countByUserIdAndRecordDate(String userId, LocalDate recordDate);
    
    // 사용자 ID로 모든 습관 기록 삭제 (회원 탈퇴 시)
    void deleteByUserId(String userId);
    
    // 특정 날짜에 메인 습관 기록 존재 여부 확인
    boolean existsByUserIdAndRecordDateAndIsMainRecord(String userId, LocalDate recordDate, boolean isMainRecord);
}