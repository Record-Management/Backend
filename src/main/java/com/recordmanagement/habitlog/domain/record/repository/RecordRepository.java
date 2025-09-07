package com.recordmanagement.habitlog.domain.record.repository;

import com.recordmanagement.habitlog.domain.record.model.Record;
import com.recordmanagement.habitlog.domain.record.model.RecordId;
import com.recordmanagement.habitlog.domain.user.model.RecordType;
import com.recordmanagement.habitlog.domain.user.model.UserId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecordRepository {
    
    Record save(Record record);
    
    Optional<Record> findById(RecordId recordId);
    
    List<Record> findByUserIdAndRecordDate(UserId userId, LocalDate recordDate);
    
    List<Record> findByUserIdAndRecordDateBetween(UserId userId, LocalDate startDate, LocalDate endDate);
    
    List<Record> findByUserIdAndType(UserId userId, RecordType type);
    
    void deleteById(RecordId recordId);
    
    boolean existsById(RecordId recordId);
}