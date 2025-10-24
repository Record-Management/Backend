package com.recordmanagement.habitlog.domain.auth.infrastructure.record.repository;

import com.recordmanagement.habitlog.domain.record.domain.model.Record;
import com.recordmanagement.habitlog.domain.record.domain.model.RecordId;
import com.recordmanagement.habitlog.domain.record.domain.repository.RecordRepository;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.record.infrastructure.entity.RecordEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class RecordRepositoryImpl implements RecordRepository {
    
    private final JpaRecordRepository jpaRecordRepository;
    
    public RecordRepositoryImpl(JpaRecordRepository jpaRecordRepository) {
        this.jpaRecordRepository = jpaRecordRepository;
    }
    
    @Override
    public Record save(Record record) {
        RecordEntity entity = RecordEntity.from(record);
        RecordEntity savedEntity = jpaRecordRepository.save(entity);
        return savedEntity.toDomain();
    }
    
    @Override
    public Optional<Record> findById(RecordId recordId) {
        return jpaRecordRepository.findById(recordId.value())
                .map(RecordEntity::toDomain);
    }
    
    @Override
    public List<Record> findByUserIdAndRecordDate(UserId userId, LocalDate recordDate) {
        return jpaRecordRepository.findByUserIdAndRecordDate(userId.getValue(), recordDate)
                .stream()
                .map(RecordEntity::toDomain)
                .toList();
    }
    
    @Override
    public List<Record> findByUserIdAndRecordDateBetween(UserId userId, LocalDate startDate, LocalDate endDate) {
        return jpaRecordRepository.findByUserIdAndRecordDateBetween(userId.getValue(), startDate, endDate)
                .stream()
                .map(RecordEntity::toDomain)
                .toList();
    }
    
    @Override
    public void deleteById(RecordId recordId) {
        jpaRecordRepository.deleteById(recordId.value());
    }
    
    @Override
    public List<Record> findByUserIdAndRecordDateBetweenAndTypeIn(UserId userId, LocalDate startDate, LocalDate endDate, List<RecordType> types) {
        return jpaRecordRepository.findByUserIdAndRecordDateBetweenAndTypeIn(userId.getValue(), startDate, endDate, types)
                .stream()
                .map(RecordEntity::toDomain)
                .toList();
    }
    
    @Override
    public int countByUserIdAndRecordDateAndType(UserId userId, LocalDate recordDate, RecordType type) {
        return jpaRecordRepository.countByUserIdAndRecordDateAndType(userId.getValue(), recordDate, type);
    }
    
    @Override
    public boolean existsById(RecordId recordId) {
        return jpaRecordRepository.existsById(recordId.value());
    }
    
    @Override
    public void deleteByUserId(String userId) {
        jpaRecordRepository.deleteByUserId(userId);
    }
}