package com.recordmanagement.habitlog.domain.record.repository;

import com.recordmanagement.habitlog.domain.record.model.ScheduleRecord;
import com.recordmanagement.habitlog.domain.user.model.UserId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 일정 기록 저장소 인터페이스
 */
public interface ScheduleRecordRepository {
    
    /**
     * 일정 기록 저장
     */
    ScheduleRecord save(ScheduleRecord scheduleRecord);
    
    /**
     * 사용자와 날짜로 일정 기록 목록 조회
     */
    List<ScheduleRecord> findByUserIdAndDate(UserId userId, LocalDate date);
    
    /**
     * ID와 사용자 ID로 일정 기록 조회
     */
    Optional<ScheduleRecord> findByIdAndUserId(String id, UserId userId);
    
    /**
     * 일정 기록 삭제
     */
    void delete(ScheduleRecord scheduleRecord);
    
    /**
     * ID로 일정 기록 조회
     */
    Optional<ScheduleRecord> findById(String id);
}