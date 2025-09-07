package com.recordmanagement.habitlog.infrastructure.record.repository;

import com.recordmanagement.habitlog.domain.record.model.Record;
import com.recordmanagement.habitlog.domain.record.model.RecordId;
import com.recordmanagement.habitlog.domain.record.repository.RecordRepository;
import com.recordmanagement.habitlog.domain.user.model.RecordType;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import com.recordmanagement.habitlog.infrastructure.record.entity.RecordEntity;
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
    public List<Record> findByUserIdAndType(UserId userId, RecordType type) {
        return jpaRecordRepository.findByUserIdAndType(userId.getValue(), type)
                .stream()
                .map(RecordEntity::toDomain)
                .toList();
    }
    
    @Override
    public void deleteById(RecordId recordId) {
        jpaRecordRepository.deleteById(recordId.value());
    }
    
    @Override
    public boolean existsById(RecordId recordId) {
        return jpaRecordRepository.existsById(recordId.value());
    }
}