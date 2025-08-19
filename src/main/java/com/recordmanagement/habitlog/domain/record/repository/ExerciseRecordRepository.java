package com.recordmanagement.habitlog.domain.record.repository;

import com.recordmanagement.habitlog.domain.record.model.ExerciseRecord;
import com.recordmanagement.habitlog.domain.user.model.UserId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 운동 기록 Repository 인터페이스
 */
public interface ExerciseRecordRepository {
    
    /**
     * 운동 기록 저장
     */
    ExerciseRecord save(ExerciseRecord exerciseRecord);
    
    /**
     * 운동 기록 ID로 조회
     */
    Optional<ExerciseRecord> findById(String id);
    
    /**
     * 사용자의 특정 날짜 운동 기록 조회
     */
    Optional<ExerciseRecord> findByUserIdAndRecordDate(UserId userId, LocalDate recordDate);
    
    /**
     * 사용자의 기간별 운동 기록 조회
     */
    List<ExerciseRecord> findByUserIdAndRecordDateBetween(UserId userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 운동 기록 삭제
     */
    void delete(ExerciseRecord exerciseRecord);
}