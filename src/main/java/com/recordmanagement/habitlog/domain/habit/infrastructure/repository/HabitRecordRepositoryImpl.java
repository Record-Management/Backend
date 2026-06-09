package com.recordmanagement.habitlog.domain.auth.infrastructure.habit.repository;

import com.recordmanagement.habitlog.domain.habit.domain.model.HabitRecord;
import com.recordmanagement.habitlog.domain.habit.domain.model.HabitRecordId;
import com.recordmanagement.habitlog.domain.habit.domain.repository.HabitRecordRepository;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.habit.infrastructure.entity.HabitRecordEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
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
        return HabitRecordEntity.builder()
            .habitRecordId(habitRecord.getId().getValue())
            .userId(habitRecord.getUserId().getValue())
            .habitType(habitRecord.getHabitType())
            .notificationEnabled(habitRecord.isNotificationEnabled())
            .notificationTime(habitRecord.getNotificationTime())
            .lastNotificationSentDate(habitRecord.getLastNotificationSentDate())
            .memo(habitRecord.getMemo())
            .recordDate(habitRecord.getRecordDate())
            .isCompleted(habitRecord.isCompleted())
            .isMainRecord(habitRecord.isMainRecord())
            .createdAt(habitRecord.getCreatedAt())
            .updatedAt(habitRecord.getUpdatedAt())
            .build();
    }
    
    private HabitRecord toDomain(HabitRecordEntity entity) {
        return HabitRecord.restore(
            HabitRecordId.from(entity.getHabitRecordId()),
            UserId.of(entity.getUserId()),
            entity.getHabitType(),
            entity.isNotificationEnabled(),
            entity.getNotificationTime(),
            entity.getLastNotificationSentDate(),
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
    
    @Override
    public boolean existsMainRecordByUserIdAndRecordDate(UserId userId, LocalDate recordDate) {
        return jpaHabitRecordRepository.existsByUserIdAndRecordDateAndIsMainRecord(
            userId.getValue(), recordDate, true
        );
    }
    
    @Override
    public int deleteMainRecordsAfterDate(UserId userId, LocalDate fromDate) {
        return jpaHabitRecordRepository.deleteByUserIdAndRecordDateAfterAndIsMainRecord(
            userId.getValue(), fromDate, true
        );
    }

    @Override
    public List<HabitRecord> findHabitsForNotification(LocalTime currentTime, LocalDate today) {
        return jpaHabitRecordRepository.findHabitsForNotification(currentTime, today)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
}