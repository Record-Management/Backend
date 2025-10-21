package com.recordmanagement.habitlog.application.habit;

import com.recordmanagement.habitlog.application.habit.dto.*;
import com.recordmanagement.habitlog.config.exception.CustomException;
import com.recordmanagement.habitlog.config.exception.ErrorCode;
import com.recordmanagement.habitlog.domain.habit.model.HabitRecord;
import com.recordmanagement.habitlog.domain.habit.model.HabitRecordId;
import com.recordmanagement.habitlog.domain.habit.repository.HabitRecordRepository;
import com.recordmanagement.habitlog.domain.record.repository.RecordRepository;
import com.recordmanagement.habitlog.domain.exercise.repository.ExerciseRecordRepository;
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
    private final RecordRepository recordRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    
    public HabitRecordApplicationService(HabitRecordRepository habitRecordRepository,
                                       RecordRepository recordRepository,
                                       ExerciseRecordRepository exerciseRecordRepository) {
        this.habitRecordRepository = habitRecordRepository;
        this.recordRepository = recordRepository;
        this.exerciseRecordRepository = exerciseRecordRepository;
    }
    
    @CacheEvict(value = "calendar", allEntries = true)
    public HabitRecordResponse createHabitRecord(CreateHabitRecordCommand command) {
        log.info("습관기록 생성 시작: userId=[{}], habitType=[{}], recordDate=[{}]", 
                command.userId().getValue(), command.habitType(), command.recordDate());
        
        // 하루 최대 1개 습관기록 제한 검증
        int habitRecordCount = habitRecordRepository.countByUserIdAndRecordDate(
            command.userId(), 
            command.recordDate()
        );
        
        if (habitRecordCount >= 1) {
            throw new CustomException(ErrorCode.HABIT_RECORD_LIMIT_EXCEEDED);
        }
        
        // 전체 기록 종류 최대 2가지 제한 검증 (습관기록이 없는 경우에만)
        if (habitRecordCount == 0) {
            validateRecordTypeLimit(command.userId(), command.recordDate());
        }
        
        HabitRecord habitRecord = HabitRecord.create(
            command.userId(),
            command.habitType(),
            command.notificationEnabled(),
            command.notificationTime(),
            command.memo(),
            command.recordDate()
        );
        
        // isMainRecord가 명시적으로 설정된 경우 적용
        if (command.isMainRecord() != null && !command.isMainRecord()) {
            habitRecord = habitRecord.updateMainRecordStatus(command.isMainRecord());
        }
        
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
        
        // isMainRecord가 명시적으로 설정된 경우 적용
        if (command.isMainRecord() != null) {
            updatedRecord = updatedRecord.updateMainRecordStatus(command.isMainRecord());
        }
        
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
    
    @CacheEvict(value = "calendar", allEntries = true)
    public HabitRecordResponse updateCompletionStatus(String habitRecordId, UserId userId, boolean isCompleted) {
        log.info("습관기록 완료 상태 변경 시작: habitRecordId=[{}], userId=[{}], isCompleted=[{}]", 
                habitRecordId, userId.getValue(), isCompleted);
        
        HabitRecord existingRecord = habitRecordRepository.findByIdAndUserId(
                HabitRecordId.from(habitRecordId), 
                userId
        ).orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
        
        HabitRecord updatedRecord = existingRecord.updateCompletionStatus(isCompleted);
        
        HabitRecord savedRecord = habitRecordRepository.save(updatedRecord);
        
        log.info("습관기록 완료 상태 변경 완료: habitRecordId=[{}], isCompleted=[{}]", habitRecordId, isCompleted);
        
        return toResponse(savedRecord);
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
            habitRecord.getMemo(),
            habitRecord.isCompleted(),
            habitRecord.isMainRecord()
        );
    }
    
    /**
     * 하루에 등록할 수 있는 기록 종류가 최대 2가지인지 검증
     */
    private void validateRecordTypeLimit(UserId userId, LocalDate recordDate) {
        // 현재 등록된 기록 종류 수를 확인
        int recordTypeCount = 0;
        
        // 일상 기록 확인
        int dailyCount = recordRepository.countByUserIdAndRecordDateAndType(userId, recordDate, RecordType.DAILY);
        if (dailyCount > 0) recordTypeCount++;
        
        // 운동 기록 확인
        int exerciseCount = exerciseRecordRepository.countByUserIdAndRecordDate(userId, recordDate);
        if (exerciseCount > 0) recordTypeCount++;
        
        // 이미 2가지 기록 종류가 있다면 습관기록을 추가할 수 없음
        if (recordTypeCount >= 2) {
            throw new CustomException(ErrorCode.RECORD_TYPE_LIMIT_EXCEEDED);
        }
    }
}