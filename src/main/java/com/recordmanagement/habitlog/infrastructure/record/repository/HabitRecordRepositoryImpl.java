package com.recordmanagement.habitlog.infrastructure.record.repository;

import com.recordmanagement.habitlog.domain.record.model.HabitType;
import com.recordmanagement.habitlog.domain.record.model.HabitRecord;
import com.recordmanagement.habitlog.domain.record.repository.HabitRecordRepository;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import com.recordmanagement.habitlog.infrastructure.record.entity.HabitRecordEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 습관 기록 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class HabitRecordRepositoryImpl implements HabitRecordRepository {

    private final JpaHabitRecordRepository jpaHabitRecordRepository;

    @Override
    public HabitRecord save(HabitRecord habitRecord) {
        HabitRecordEntity entity = HabitRecordEntity.from(habitRecord);
        HabitRecordEntity savedEntity = jpaHabitRecordRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public List<HabitRecord> findByUserIdAndRecordDate(UserId userId, LocalDate recordDate) {
        List<HabitRecordEntity> entities = jpaHabitRecordRepository.findByUserIdAndRecordDate(
                userId.getValue(), recordDate);
        return entities.stream()
                .map(HabitRecordEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<HabitRecord> findByUserIdAndRecordDateAndHabitType(UserId userId, LocalDate recordDate, HabitType habitType) {
        Optional<HabitRecordEntity> entity = jpaHabitRecordRepository.findByUserIdAndRecordDateAndHabitType(
                userId.getValue(), recordDate, habitType);
        return entity.map(HabitRecordEntity::toDomain);
    }

    @Override
    public void delete(HabitRecord habitRecord) {
        HabitRecordEntity entity = HabitRecordEntity.from(habitRecord);
        jpaHabitRecordRepository.delete(entity);
    }

    @Override
    public Optional<HabitRecord> findById(String id) {
        Optional<HabitRecordEntity> entity = jpaHabitRecordRepository.findById(id);
        return entity.map(HabitRecordEntity::toDomain);
    }
}