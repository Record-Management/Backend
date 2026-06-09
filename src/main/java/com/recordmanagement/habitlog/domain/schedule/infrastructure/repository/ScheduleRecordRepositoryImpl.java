package com.recordmanagement.habitlog.domain.schedule.infrastructure.repository;

import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord;
import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecordId;
import com.recordmanagement.habitlog.domain.schedule.domain.repository.ScheduleRecordRepository;
import com.recordmanagement.habitlog.domain.schedule.infrastructure.entity.ScheduleRecordEntity;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Transactional
public class ScheduleRecordRepositoryImpl implements ScheduleRecordRepository {

    private final JpaScheduleRecordRepository jpaScheduleRecordRepository;

    public ScheduleRecordRepositoryImpl(JpaScheduleRecordRepository jpaScheduleRecordRepository) {
        this.jpaScheduleRecordRepository = jpaScheduleRecordRepository;
    }

    @Override
    public ScheduleRecord save(ScheduleRecord scheduleRecord) {
        ScheduleRecordEntity entity = ScheduleRecordEntity.from(scheduleRecord);
        ScheduleRecordEntity savedEntity = jpaScheduleRecordRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<ScheduleRecord> findById(ScheduleRecordId id) {
        return jpaScheduleRecordRepository.findById(id.value())
                .map(ScheduleRecordEntity::toDomain);
    }

    @Override
    public Optional<ScheduleRecord> findByIdAndUserId(ScheduleRecordId id, UserId userId) {
        return jpaScheduleRecordRepository.findByScheduleRecordIdAndUserId(id.value(), userId.getValue())
                .map(ScheduleRecordEntity::toDomain);
    }

    @Override
    public List<ScheduleRecord> findByUserIdAndDateRange(UserId userId, LocalDate startDate, LocalDate endDate) {
        return jpaScheduleRecordRepository.findByUserIdAndDateRange(userId.getValue(), startDate, endDate)
                .stream()
                .map(ScheduleRecordEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(ScheduleRecordId id) {
        jpaScheduleRecordRepository.deleteById(id.value());
    }

    @Override
    public void deleteByIdAndUserId(ScheduleRecordId id, UserId userId) {
        jpaScheduleRecordRepository.deleteByScheduleRecordIdAndUserId(id.value(), userId.getValue());
    }

    @Override
    public void deleteByUserId(String userId) {
        jpaScheduleRecordRepository.deleteByUserId(userId);
    }

    @Override
    public boolean existsByIdAndUserId(ScheduleRecordId id, UserId userId) {
        return jpaScheduleRecordRepository.existsByScheduleRecordIdAndUserId(id.value(), userId.getValue());
    }

    @Override
    public List<ScheduleRecord> findSchedulesForNotification(LocalDate notificationDate, int notificationHour, int notificationMinute) {
        LocalDate oneDayAfter = notificationDate.plusDays(1);
        LocalDate twoDaysAfter = notificationDate.plusDays(2);

        return jpaScheduleRecordRepository.findSchedulesForNotification(
                notificationDate, oneDayAfter, twoDaysAfter, notificationHour, notificationMinute
        ).stream()
                .map(ScheduleRecordEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleRecord> findRepeatableSchedules() {
        return jpaScheduleRecordRepository.findRepeatableSchedules(LocalDate.now())
                .stream()
                .map(ScheduleRecordEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleRecord> findRepeatableSchedulesByUserId(UserId userId) {
        return jpaScheduleRecordRepository.findRepeatableSchedulesByUserId(userId.getValue(), LocalDate.now())
                .stream()
                .map(ScheduleRecordEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public int countByUserIdAndCreatedAtToday(UserId userId, LocalDate today) {
        return jpaScheduleRecordRepository.countByUserIdAndCreatedAtToday(userId.getValue(), today);
    }
}
