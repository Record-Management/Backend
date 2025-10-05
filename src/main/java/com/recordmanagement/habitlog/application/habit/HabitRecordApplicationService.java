package com.recordmanagement.habitlog.application.habit;

import com.recordmanagement.habitlog.application.habit.dto.*;
import com.recordmanagement.habitlog.config.exception.CustomException;
import com.recordmanagement.habitlog.config.exception.ErrorCode;
import com.recordmanagement.habitlog.domain.habit.model.HabitRecord;
import com.recordmanagement.habitlog.domain.habit.model.HabitRecordId;
import com.recordmanagement.habitlog.domain.habit.repository.HabitRecordRepository;
import com.recordmanagement.habitlog.domain.user.model.RecordType;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class HabitRecordApplicationService {
    
    private static final Logger log = LoggerFactory.getLogger(HabitRecordApplicationService.class);
    
    private final HabitRecordRepository habitRecordRepository;
    
    public HabitRecordApplicationService(HabitRecordRepository habitRecordRepository) {
        this.habitRecordRepository = habitRecordRepository;
    }
    
    @CacheEvict(value = "calendar", allEntries = true)
    public HabitRecordResponse createHabitRecord(CreateHabitRecordCommand command) {
        log.info("습관기록 생성 시작: userId=[{}], habitType=[{}], recordDate=[{}]", 
                command.userId().getValue(), command.habitType(), command.recordDate());
        
        HabitRecord habitRecord = HabitRecord.create(
            command.userId(),
            command.habitType(),
            command.notificationEnabled(),
            command.notificationTime(),
            command.memo(),
            command.recordDate()
        );
        
        HabitRecord savedHabitRecord = habitRecordRepository.save(habitRecord);
        
        log.info("습관기록 생성 완료: habitRecordId=[{}]", savedHabitRecord.getId().getValue());
        
        return toResponse(savedHabitRecord);
    }
    
    @Transactional(readOnly = true)
    public HabitRecordResponse getHabitRecord(String habitRecordId, UserId userId) {
        log.info("습관기록 조회: habitRecordId=[{}], userId=[{}]", habitRecordId, userId.getValue());
        
        HabitRecord habitRecord = habitRecordRepository.findByIdAndUserId(
                HabitRecordId.from(habitRecordId), 
                userId
        ).orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
        
        return toResponse(habitRecord);
    }
    
    @Transactional(readOnly = true)
    public List<HabitRecordResponse> getDailyHabitRecords(UserId userId, LocalDate recordDate) {
        log.info("특정 날짜 습관기록 조회: userId=[{}], recordDate=[{}]", userId.getValue(), recordDate);
        
        List<HabitRecord> habitRecords = habitRecordRepository.findByUserIdAndRecordDate(userId, recordDate);
        
        return habitRecords.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @CacheEvict(value = "calendar", allEntries = true)
    public HabitRecordResponse updateHabitRecord(String habitRecordId, UpdateHabitRecordCommand command) {
        log.info("습관기록 수정 시작: habitRecordId=[{}]", habitRecordId);
        
        HabitRecord existingRecord = habitRecordRepository.findByIdAndUserId(
                HabitRecordId.from(habitRecordId), 
                command.userId()
        ).orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
        
        HabitRecord updatedRecord = existingRecord
                .updateHabitType(command.habitType())
                .updateNotificationSettings(command.notificationEnabled(), command.notificationTime())
                .updateMemo(command.memo());
        
        HabitRecord savedRecord = habitRecordRepository.save(updatedRecord);
        
        log.info("습관기록 수정 완료: habitRecordId=[{}]", habitRecordId);
        
        return toResponse(savedRecord);
    }
    
    @CacheEvict(value = "calendar", allEntries = true)
    public void deleteHabitRecord(String habitRecordId, UserId userId) {
        log.info("습관기록 삭제 시작: habitRecordId=[{}], userId=[{}]", habitRecordId, userId.getValue());
        
        if (!habitRecordRepository.existsByIdAndUserId(HabitRecordId.from(habitRecordId), userId)) {
            throw new CustomException(ErrorCode.RECORD_NOT_FOUND);
        }
        
        habitRecordRepository.deleteByIdAndUserId(HabitRecordId.from(habitRecordId), userId);
        
        log.info("습관기록 삭제 완료: habitRecordId=[{}]", habitRecordId);
    }
    
    private HabitRecordResponse toResponse(HabitRecord habitRecord) {
        return new HabitRecordResponse(
            // 공통 필드
            habitRecord.getId().getValue(),
            RecordType.HABIT,
            habitRecord.getRecordDate(),
            null, // 습관 기록은 recordTime이 없음 (운동기록과 동일한 패턴)
            habitRecord.getCreatedAt(),
            habitRecord.getUpdatedAt(),
            
            // 습관기록 전용 필드
            habitRecord.getHabitType(),
            habitRecord.isNotificationEnabled(),
            habitRecord.getNotificationTime(),
            habitRecord.getMemo()
        );
    }
}