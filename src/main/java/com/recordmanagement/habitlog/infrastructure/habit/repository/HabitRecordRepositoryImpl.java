package com.recordmanagement.habitlog.infrastructure.habit.repository;

import com.recordmanagement.habitlog.domain.habit.model.HabitRecord;
import com.recordmanagement.habitlog.domain.habit.model.HabitRecordId;
import com.recordmanagement.habitlog.domain.habit.repository.HabitRecordRepository;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import com.recordmanagement.habitlog.infrastructure.habit.entity.HabitRecordEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Transactional
public class HabitRecordRepositoryImpl implements HabitRecordRepository {
    
    private final JpaHabitRecordRepository jpaHabitRecordRepository;
    
    public HabitRecordRepositoryImpl(JpaHabitRecordRepository jpaHabitRecordRepository) {
        this.jpaHabitRecordRepository = jpaHabitRecordRepository;
    }
    
    @Override
    public HabitRecord save(HabitRecord habitRecord) {
        HabitRecordEntity entity = toEntity(habitRecord);
        HabitRecordEntity savedEntity = jpaHabitRecordRepository.save(entity);
        return toDomain(savedEntity);
    }
    
    @Override
    public Optional<HabitRecord> findById(HabitRecordId id) {
        return jpaHabitRecordRepository.findById(id.getValue())
                .map(this::toDomain);
    }
    
    @Override
    public Optional<HabitRecord> findByIdAndUserId(HabitRecordId id, UserId userId) {
        return jpaHabitRecordRepository.findByHabitRecordIdAndUserId(id.getValue(), userId.getValue())
                .map(this::toDomain);
    }
    
    @Override
    public List<HabitRecord> findByUserIdAndRecordDate(UserId userId, LocalDate recordDate) {
        return jpaHabitRecordRepository.findByUserIdAndRecordDate(userId.getValue(), recordDate)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<HabitRecord> findByUserIdAndRecordDateBetween(UserId userId, LocalDate startDate, LocalDate endDate) {
        return jpaHabitRecordRepository.findByUserIdAndRecordDateBetween(userId.getValue(), startDate, endDate)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(HabitRecordId id) {
        jpaHabitRecordRepository.deleteById(id.getValue());
    }
    
    @Override
    public void deleteByIdAndUserId(HabitRecordId id, UserId userId) {
        jpaHabitRecordRepository.deleteByHabitRecordIdAndUserId(id.getValue(), userId.getValue());
    }
    
    @Override
    public boolean existsByIdAndUserId(HabitRecordId id, UserId userId) {
        return jpaHabitRecordRepository.existsByHabitRecordIdAndUserId(id.getValue(), userId.getValue());
    }
    
    private HabitRecordEntity toEntity(HabitRecord habitRecord) {
        HabitRecordEntity entity = new HabitRecordEntity(
            habitRecord.getId().getValue(),
            habitRecord.getUserId().getValue(),
            habitRecord.getHabitType(),
            habitRecord.isNotificationEnabled(),
            habitRecord.getNotificationTime(),
            habitRecord.getMemo(),
            habitRecord.getRecordDate(),
            habitRecord.isCompleted(),
            habitRecord.isMainRecord()
        );
        
        return entity;
    }
    
    private HabitRecord toDomain(HabitRecordEntity entity) {
        return new HabitRecord(
            HabitRecordId.from(entity.getHabitRecordId()),
            UserId.of(entity.getUserId()),
            entity.getHabitType(),
            entity.isNotificationEnabled(),
            entity.getNotificationTime(),
            entity.getMemo(),
            entity.getRecordDate(),
            entity.isCompleted(),
            entity.isMainRecord(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
    
    @Override
    public int countByUserIdAndRecordDate(UserId userId, LocalDate recordDate) {
        return jpaHabitRecordRepository.countByUserIdAndRecordDate(userId.getValue(), recordDate);
    }
    
    @Override
    public void deleteByUserId(String userId) {
        jpaHabitRecordRepository.deleteByUserId(userId);
    }
}