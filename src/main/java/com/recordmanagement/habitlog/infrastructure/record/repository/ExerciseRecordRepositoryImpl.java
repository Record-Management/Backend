package com.recordmanagement.habitlog.infrastructure.record.repository;

import com.recordmanagement.habitlog.domain.record.model.ExerciseRecord;
import com.recordmanagement.habitlog.domain.record.repository.ExerciseRecordRepository;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import com.recordmanagement.habitlog.infrastructure.record.entity.ExerciseRecordEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 운동 기록 Repository 구현체
 */
@Repository
public class ExerciseRecordRepositoryImpl implements ExerciseRecordRepository {
    
    private final ExerciseRecordJpaRepository exerciseRecordJpaRepository;
    
    public ExerciseRecordRepositoryImpl(ExerciseRecordJpaRepository exerciseRecordJpaRepository) {
        this.exerciseRecordJpaRepository = exerciseRecordJpaRepository;
    }
    
    @Override
    public ExerciseRecord save(ExerciseRecord exerciseRecord) {
        ExerciseRecordEntity entity = ExerciseRecordEntity.from(exerciseRecord);
        ExerciseRecordEntity savedEntity = exerciseRecordJpaRepository.save(entity);
        return savedEntity.toDomain();
    }
    
    @Override
    public Optional<ExerciseRecord> findById(String id) {
        return exerciseRecordJpaRepository.findById(id)
                .map(ExerciseRecordEntity::toDomain);
    }
    
    @Override
    public Optional<ExerciseRecord> findByUserIdAndRecordDate(UserId userId, LocalDate recordDate) {
        return exerciseRecordJpaRepository.findByUserIdAndRecordDate(userId.getValue(), recordDate)
                .map(ExerciseRecordEntity::toDomain);
    }
    
    @Override
    public List<ExerciseRecord> findByUserIdAndRecordDateBetween(UserId userId, LocalDate startDate, LocalDate endDate) {
        return exerciseRecordJpaRepository.findByUserIdAndRecordDateBetween(
                        userId.getValue(), startDate, endDate)
                .stream()
                .map(ExerciseRecordEntity::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public void delete(ExerciseRecord exerciseRecord) {
        exerciseRecordJpaRepository.deleteById(exerciseRecord.getId());
    }
}