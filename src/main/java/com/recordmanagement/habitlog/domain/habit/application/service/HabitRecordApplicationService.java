package com.recordmanagement.habitlog.domain.habit.application.service;

import com.recordmanagement.habitlog.domain.habit.application.dto.*;
import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import com.recordmanagement.habitlog.domain.habit.domain.model.HabitRecord;
import com.recordmanagement.habitlog.domain.habit.domain.model.HabitRecordId;
import com.recordmanagement.habitlog.domain.habit.domain.repository.HabitRecordRepository;
import com.recordmanagement.habitlog.domain.record.domain.repository.RecordRepository;
import com.recordmanagement.habitlog.domain.exercise.domain.repository.ExerciseRecordRepository;
import com.recordmanagement.habitlog.domain.record.domain.service.MainRecordDeterminationService;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
import com.recordmanagement.habitlog.domain.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class HabitRecordApplicationService {
    
    private final HabitRecordRepository habitRecordRepository;
    private final RecordRepository recordRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final MainRecordDeterminationService mainRecordDeterminationService;
    private final UserRepository userRepository;
    
    @CacheEvict(value = "calendar", allEntries = true)
    public HabitRecordResponse createHabitRecord(CreateHabitRecordCommand command) {
        log.info("습관기록 생성 시작: userId=[{}], habitType=[{}], recordDate=[{}]", 
                command.userId().getValue(), command.habitType(), command.recordDate());
        
        // 사용자 정보 조회
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> UserException.notFound(command.userId().getValue()));
        
        // 습관 타입 사용자만 습관 기록 생성 가능
        if (user.getMainRecordType() != RecordType.HABIT) {
            throw new CustomException(ErrorCode.INVALID_RECORD_TYPE_FOR_USER);
        }
        
        // 습관 시작일이 설정되어 있어야 함 (온보딩 시점에 설정됨)
        if (user.getHabitStartDate() == null) {
            log.error("습관 시작일이 설정되지 않음: userId=[{}]", command.userId().getValue());
            throw new CustomException(ErrorCode.INVALID_RECORD_TYPE_FOR_USER);
        }
        
        // 습관 목표 기간 내 날짜인지 검증
        if (!user.isWithinHabitPeriod(command.recordDate())) {
            User.HabitPeriodInfo periodInfo = user.getHabitPeriodInfo();
            log.warn("습관 기록 기간 외 등록 시도: userId=[{}], recordDate=[{}], habitPeriod=[{} ~ {}]", 
                    command.userId().getValue(), command.recordDate(), 
                    periodInfo.startDate(), periodInfo.endDate());
            throw new CustomException(ErrorCode.HABIT_RECORD_OUT_OF_PERIOD);
        }
        
        // 하루 최대 2개 습관기록 제한 검증
        int habitRecordCount = habitRecordRepository.countByUserIdAndRecordDate(
            command.userId(), 
            command.recordDate()
        );
        
        if (habitRecordCount >= 2) {
            throw new CustomException(ErrorCode.HABIT_RECORD_LIMIT_EXCEEDED);
        }
        
        // 전체 기록 종류 최대 2가지 제한 검증 (습관기록이 없는 경우에만)
        if (habitRecordCount == 0) {
            validateRecordTypeLimit(command.userId(), command.recordDate());
        }
        
        // 메인 기록 결정 (명시적으로 설정되지 않은 경우)
        boolean isMainRecord = true; // 기본값
        if (command.isMainRecord() == null) {
            isMainRecord = mainRecordDeterminationService.determineMainRecord(
                command.userId(), 
                RecordType.HABIT, 
                command.recordDate(), 
                habitRecordCount
            );
        } else {
            isMainRecord = command.isMainRecord();
        }
        
        HabitRecord habitRecord = HabitRecord.create(
            command.userId(),
            command.habitType(),
            command.notificationEnabled(),
            command.notificationTime(),
            command.memo(),
            command.recordDate()
        );
        
        // 메인 기록 상태 설정
        habitRecord = habitRecord.updateMainRecordStatus(isMainRecord);
        
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
        
        // 메인 기록 자동 결정 (명시적 설정이 없는 경우)
        boolean autoIsMainRecord = mainRecordDeterminationService.determineMainRecordOnUpdate(
            command.userId(), 
            RecordType.HABIT
        );
        log.info("습관기록 수정으로 메인 기록 자동 결정: recordId={}, autoIsMain={}", 
                habitRecordId, autoIsMainRecord);
        
        HabitRecord updatedRecord = existingRecord
                .updateHabitType(command.habitType())
                .updateNotificationSettings(command.notificationEnabled(), command.notificationTime())
                .updateMemo(command.memo());
        
        // 메인 기록 상태 결정: 명시적 설정 > 자동 결정 > 기존 값 유지
        if (command.isMainRecord() != null) {
            // 명시적으로 설정된 경우
            updatedRecord = updatedRecord.updateMainRecordStatus(command.isMainRecord());
            log.info("습관기록 메인 상태 명시적 설정: recordId={}, isMain={}", 
                    habitRecordId, command.isMainRecord());
        } else {
            // 자동 결정된 값 적용
            updatedRecord = updatedRecord.updateMainRecordStatus(autoIsMainRecord);
            log.info("습관기록 메인 상태 자동 적용: recordId={}, isMain={}", 
                    habitRecordId, autoIsMainRecord);
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