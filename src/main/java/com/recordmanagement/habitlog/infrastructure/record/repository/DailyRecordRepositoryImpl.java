package com.recordmanagement.habitlog.infrastructure.record.repository;

import com.recordmanagement.habitlog.domain.record.model.DailyRecord;
import com.recordmanagement.habitlog.domain.record.repository.DailyRecordRepository;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import com.recordmanagement.habitlog.infrastructure.record.entity.DailyRecordEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 일상 기록 Repository 구현체
 */
@Repository
public class DailyRecordRepositoryImpl implements DailyRecordRepository {
    
    private final DailyRecordJpaRepository dailyRecordJpaRepository;
    
    public DailyRecordRepositoryImpl(DailyRecordJpaRepository dailyRecordJpaRepository) {
        this.dailyRecordJpaRepository = dailyRecordJpaRepository;
    }
    
    @Override
    public DailyRecord save(DailyRecord dailyRecord) {
        DailyRecordEntity entity = DailyRecordEntity.from(dailyRecord);
        DailyRecordEntity savedEntity = dailyRecordJpaRepository.save(entity);
        return savedEntity.toDomain();
    }
    
    @Override
    public Optional<DailyRecord> findById(String id) {
        return dailyRecordJpaRepository.findById(id)
                .map(DailyRecordEntity::toDomain);
    }
    
    @Override
    public Optional<DailyRecord> findByUserIdAndRecordDate(UserId userId, LocalDate recordDate) {
        return dailyRecordJpaRepository.findByUserIdAndRecordDate(userId.getValue(), recordDate)
                .map(DailyRecordEntity::toDomain);
    }
    
    @Override
    public List<DailyRecord> findByUserIdAndRecordDateBetween(UserId userId, LocalDate startDate, LocalDate endDate) {
        return dailyRecordJpaRepository.findByUserIdAndRecordDateBetween(
                        userId.getValue(), startDate, endDate)
                .stream()
                .map(DailyRecordEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public void delete(DailyRecord dailyRecord) {
        dailyRecordJpaRepository.deleteById(dailyRecord.getId());
    }
}