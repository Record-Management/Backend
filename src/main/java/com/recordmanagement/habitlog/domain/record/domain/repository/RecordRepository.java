package com.recordmanagement.habitlog.domain.record.domain.repository;

import com.recordmanagement.habitlog.domain.record.domain.model.Record;
import com.recordmanagement.habitlog.domain.record.domain.model.RecordId;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecordRepository {
    
    Record save(Record record);
    
    Optional<Record> findById(RecordId recordId);
    
    List<Record> findByUserIdAndRecordDate(UserId userId, LocalDate recordDate);
    
    List<Record> findByUserIdAndRecordDateBetween(UserId userId, LocalDate startDate, LocalDate endDate);
    
    List<Record> findByUserIdAndRecordDateBetweenAndTypeIn(UserId userId, LocalDate startDate, LocalDate endDate, List<RecordType> types);
    
    int countByUserIdAndRecordDateAndType(UserId userId, LocalDate recordDate, RecordType type);

    /**
     * 특정 기간 내 기록이 있는 날짜 수를 조회 (성능 최적화)
     * - 목표 달성률 계산 시 사용
     * - N번 쿼리 대신 1번 쿼리로 처리
     */
    int countDistinctRecordDatesByUserIdAndDateRangeAndType(
        UserId userId, LocalDate startDate, LocalDate endDate, RecordType type);

    void deleteById(RecordId recordId);

    void deleteByUserId(String userId);

    boolean existsById(RecordId recordId);

    boolean existsByUserIdAndRecordDateAndType(UserId userId, LocalDate recordDate, RecordType type);
}