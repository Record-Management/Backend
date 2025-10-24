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
    
    void deleteById(RecordId recordId);
    
    void deleteByUserId(String userId);
    
    boolean existsById(RecordId recordId);
}