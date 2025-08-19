package com.recordmanagement.habitlog.infrastructure.record.repository;

import com.recordmanagement.habitlog.domain.record.model.ScheduleRecord;
import com.recordmanagement.habitlog.domain.record.repository.ScheduleRecordRepository;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import com.recordmanagement.habitlog.infrastructure.record.entity.ScheduleRecordEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 일정 기록 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class ScheduleRecordRepositoryImpl implements ScheduleRecordRepository {

    private final JpaScheduleRecordRepository jpaScheduleRecordRepository;

    @Override
    public ScheduleRecord save(ScheduleRecord scheduleRecord) {
        ScheduleRecordEntity entity = ScheduleRecordEntity.from(scheduleRecord);
        ScheduleRecordEntity savedEntity = jpaScheduleRecordRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public List<ScheduleRecord> findByUserIdAndDate(UserId userId, LocalDate date) {
        List<ScheduleRecordEntity> entities = jpaScheduleRecordRepository.findByUserIdAndDate(
                userId.getValue(), date);
        return entities.stream()
                .map(ScheduleRecordEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ScheduleRecord> findByIdAndUserId(String id, UserId userId) {
        Optional<ScheduleRecordEntity> entity = jpaScheduleRecordRepository.findByIdAndUserId(
                id, userId.getValue());
        return entity.map(ScheduleRecordEntity::toDomain);
    }

    @Override
    public void delete(ScheduleRecord scheduleRecord) {
        ScheduleRecordEntity entity = ScheduleRecordEntity.from(scheduleRecord);
        jpaScheduleRecordRepository.delete(entity);
    }

    @Override
    public Optional<ScheduleRecord> findById(String id) {
        Optional<ScheduleRecordEntity> entity = jpaScheduleRecordRepository.findById(id);
        return entity.map(ScheduleRecordEntity::toDomain);
    }
}