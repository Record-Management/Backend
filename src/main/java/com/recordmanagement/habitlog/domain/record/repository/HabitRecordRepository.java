package com.recordmanagement.habitlog.domain.record.repository;

import com.recordmanagement.habitlog.domain.record.model.HabitType;
import com.recordmanagement.habitlog.domain.record.model.HabitRecord;
import com.recordmanagement.habitlog.domain.user.model.UserId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 습관 기록 저장소 인터페이스
 */
public interface HabitRecordRepository {
    
    /**
     * 습관 기록 저장
     */
    HabitRecord save(HabitRecord habitRecord);
    
    /**
     * 사용자와 기록 날짜로 습관 기록 목록 조회
     */
    List<HabitRecord> findByUserIdAndRecordDate(UserId userId, LocalDate recordDate);
    
    /**
     * 사용자, 기록 날짜, 습관 타입으로 습관 기록 조회
     */
    Optional<HabitRecord> findByUserIdAndRecordDateAndHabitType(UserId userId, LocalDate recordDate, HabitType habitType);
    
    /**
     * 습관 기록 삭제
     */
    void delete(HabitRecord habitRecord);
    
    /**
     * ID로 습관 기록 조회
     */
    Optional<HabitRecord> findById(String id);
}