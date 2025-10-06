package com.recordmanagement.habitlog.infrastructure.exercise.repository;

import com.recordmanagement.habitlog.domain.exercise.model.ExerciseRecord;
import com.recordmanagement.habitlog.domain.exercise.model.ExerciseRecordId;
import com.recordmanagement.habitlog.domain.exercise.repository.ExerciseRecordRepository;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import com.recordmanagement.habitlog.infrastructure.exercise.entity.ExerciseRecordEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Transactional
public class ExerciseRecordRepositoryImpl implements ExerciseRecordRepository {
    
    private final JpaExerciseRecordRepository jpaExerciseRecordRepository;
    
    public ExerciseRecordRepositoryImpl(JpaExerciseRecordRepository jpaExerciseRecordRepository) {
        this.jpaExerciseRecordRepository = jpaExerciseRecordRepository;
    }
    
    @Override
    public ExerciseRecord save(ExerciseRecord exerciseRecord) {
        ExerciseRecordEntity entity = toEntity(exerciseRecord);
        ExerciseRecordEntity savedEntity = jpaExerciseRecordRepository.save(entity);
        return toDomain(savedEntity);
    }
    
    @Override
    public Optional<ExerciseRecord> findById(ExerciseRecordId id) {
        return jpaExerciseRecordRepository.findById(id.getValue())
                .map(this::toDomain);
    }
    
    @Override
    public Optional<ExerciseRecord> findByIdAndUserId(ExerciseRecordId id, UserId userId) {
        return jpaExerciseRecordRepository.findByExerciseRecordIdAndUserId(id.getValue(), userId.getValue())
                .map(this::toDomain);
    }
    
    @Override
    public List<ExerciseRecord> findByUserIdAndRecordDate(UserId userId, LocalDate recordDate) {
        return jpaExerciseRecordRepository.findByUserIdAndRecordDate(userId.getValue(), recordDate)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ExerciseRecord> findByUserIdAndRecordDateBetween(UserId userId, LocalDate startDate, LocalDate endDate) {
        return jpaExerciseRecordRepository.findByUserIdAndRecordDateBetween(userId.getValue(), startDate, endDate)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(ExerciseRecordId id) {
        jpaExerciseRecordRepository.deleteById(id.getValue());
    }
    
    @Override
    public void deleteByIdAndUserId(ExerciseRecordId id, UserId userId) {
        jpaExerciseRecordRepository.deleteByExerciseRecordIdAndUserId(id.getValue(), userId.getValue());
    }
    
    @Override
    public boolean existsByIdAndUserId(ExerciseRecordId id, UserId userId) {
        return jpaExerciseRecordRepository.existsByExerciseRecordIdAndUserId(id.getValue(), userId.getValue());
    }
    
    private ExerciseRecordEntity toEntity(ExerciseRecord exerciseRecord) {
        ExerciseRecordEntity entity = new ExerciseRecordEntity(
            exerciseRecord.getId().getValue(),
            exerciseRecord.getUserId().getValue(),
            exerciseRecord.getExerciseType(),
            exerciseRecord.getCaloriesBurned(),
            exerciseRecord.getExerciseTimeMinutes(),
            exerciseRecord.getStepCount(),
            exerciseRecord.getWeight(),
            exerciseRecord.getDailyNote(),
            exerciseRecord.getImageUrls(),
            exerciseRecord.getRecordDate(),
            exerciseRecord.getRecordTime()
        );
        
        return entity;
    }
    
    private ExerciseRecord toDomain(ExerciseRecordEntity entity) {
        return new ExerciseRecord(
            ExerciseRecordId.from(entity.getExerciseRecordId()),
            UserId.of(entity.getUserId()),
            entity.getExerciseType(),
            entity.getCaloriesBurned(),
            entity.getExerciseTimeMinutes(),
            entity.getStepCount(),
            entity.getWeight(),
            entity.getDailyNote(),
            entity.getImageUrls(),
            entity.getRecordDate(),
            entity.getRecordTime(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
    
    @Override
    public int countByUserIdAndRecordDate(UserId userId, LocalDate recordDate) {
        return jpaExerciseRecordRepository.countByUserIdAndRecordDate(userId.getValue(), recordDate);
    }
}