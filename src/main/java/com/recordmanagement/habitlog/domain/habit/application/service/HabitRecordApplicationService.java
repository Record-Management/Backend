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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 습관 기록 Application Service
 * 
 * ### 실제 행동 기반 습관 관리 (v1.8.2)
 * 1. **현재 날짜에만 생성**: 습관 등록 시 현재 날짜에만 기록 생성 (미래 날짜 미리 생성하지 않음)
 * 2. **완료 상태 기반 표시**: isCompleted 값으로 주황색(완료)/회색(미완료) 캘린더 아이콘 구분
 * 3. **일상/운동과 동일한 UX**: 사용자가 실제 행동할 때만 캘린더에 표시
 * 4. **메인/서브 기록 관리**: 기존 메인 기록을 서브로 변경하는 로직만 수행
 * 
 * ### 주요 기능
 * - **습관 기록 생성**: 현재 날짜 기준 단일 기록 생성
 * - **완료 상태 관리**: isCompleted 플래그로 완료/미완료 구분
 * - **메인 기록 관리**: 메인 습관 변경 시 기존 기록들의 상태 동기화
 * - **목표 진행률 연동**: 습관 기록 생성/완료 시 자동 목표 진행률 업데이트
 *
 * @author 전우선
 * @since 2025.10.24
 * @version 1.2.0 (실제 행동 기반 시스템)
 */
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
    private final ApplicationContext applicationContext;
    
    @CacheEvict(value = "calendar", allEntries = true)
    public HabitRecordResponse createHabitRecord(CreateHabitRecordCommand command) {
        log.info("습관기록 생성 시작: userId=[{}], habitType=[{}], recordDate=[{}]", 
                command.userId().getValue(), command.habitType(), command.recordDate());
        
        // 사용자 정보 조회
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> UserException.notFound(command.userId().getValue()));
        
        // 습관 기록은 모든 사용자가 작성 가능 (메인/서브 기록으로)
        
        // 메인 기록 타입이 HABIT인 사용자는 습관 시작일과 기간 검증
        if (user.getMainRecordType() == RecordType.HABIT) {
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
        }
        
        // 하루 최대 2개 기록 제한 검증 (전체 타입 합쳐서)
        int totalRecordCount = getTotalRecordCount(command.userId(), command.recordDate());

        if (totalRecordCount >= 2) {
            throw new CustomException(ErrorCode.HABIT_RECORD_LIMIT_EXCEEDED);
        }

        // 전체 기록 종류 최대 2가지 제한 검증
        int habitRecordCount = habitRecordRepository.countByUserIdAndRecordDate(
            command.userId(),
            command.recordDate()
        );

        if (habitRecordCount == 0) {
            validateRecordTypeLimit(command.userId(), command.recordDate());
        }
        
        // 메인 기록 결정
        boolean hasMainHabitRecord = habitRecordRepository.existsMainRecordByUserIdAndRecordDate(
            command.userId(),
            command.recordDate()
        );

        boolean isMainRecord;

        // 1순위: 사용자가 명시적으로 메인 기록으로 설정한 경우 (서브→메인 변경 체크 시)
        if (command.isMainRecord() != null && command.isMainRecord()) {
            isMainRecord = true;
            log.info("사용자가 명시적으로 메인 기록으로 설정: userId=[{}], recordDate=[{}]",
                    command.userId().getValue(), command.recordDate());
        }
        // 2순위: 명시적으로 서브로 설정한 경우
        else if (command.isMainRecord() != null && !command.isMainRecord()) {
            isMainRecord = false;
            log.info("사용자가 명시적으로 서브 기록으로 설정: userId=[{}], recordDate=[{}]",
                    command.userId().getValue(), command.recordDate());
        }
        // 3순위: 습관 타입 사용자 + 첫 습관 기록 = 자동으로 메인 기록
        else if (user.getMainRecordType() == RecordType.HABIT && !hasMainHabitRecord) {
            isMainRecord = true;
            log.info("습관 타입 사용자의 첫 습관 기록으로 메인 기록 자동 설정: userId=[{}], recordDate=[{}]",
                    command.userId().getValue(), command.recordDate());
        }
        // 4순위: 이미 메인 습관 기록이 있거나 습관 타입이 아닌 경우 서브 기록
        else {
            isMainRecord = false;
            log.info("서브 기록으로 설정: userId=[{}], mainRecordType=[{}], hasMainRecord=[{}]",
                    command.userId().getValue(), user.getMainRecordType(), hasMainHabitRecord);
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

        // 메인 기록이고 사용자의 메인 기록 타입이 HABIT인 경우 기존 메인 기록들을 서브로 변경
        if (isMainRecord && user.getMainRecordType() == RecordType.HABIT) {
            updateExistingMainRecordsToSub(user, savedHabitRecord);

            log.info("메인 습관 기록 생성 완료 (스케줄러가 내일부터 자동 생성): userId={}", command.userId().getValue());
        }
        
        // 습관 기록 생성 후 목표 진행률 업데이트
        updateGoalProgress(command.userId());
        
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
        
        // 사용자 정보 조회
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> UserException.notFound(command.userId().getValue()));
        
        HabitRecord existingRecord = habitRecordRepository.findByIdAndUserId(
                HabitRecordId.from(habitRecordId), 
                command.userId()
        ).orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
        
        HabitRecord updatedRecord = existingRecord
                .updateHabitType(command.habitType())
                .updateNotificationSettings(command.notificationEnabled(), command.notificationTime())
                .updateMemo(command.memo());
        
        // 메인 기록 상태 결정: 기존 작성 API와 동일한 로직 적용
        boolean finalIsMainRecord;
        if (command.isMainRecord() != null) {
            // 명시적으로 설정된 경우
            finalIsMainRecord = command.isMainRecord();
            log.info("습관기록 메인 상태 명시적 설정: recordId={}, isMain={}", 
                    habitRecordId, command.isMainRecord());
        } else {
            // isMainRecord가 null인 경우 기존 작성 API와 동일한 조건 적용
            // 해당 날짜에 이미 메인 습관 기록이 있는지 확인 (현재 수정 중인 기록 제외)
            boolean hasOtherMainHabitRecord = habitRecordRepository.findByUserIdAndRecordDate(
                command.userId(), existingRecord.getRecordDate()
            ).stream()
            .filter(record -> !record.getId().equals(existingRecord.getId())) // 현재 수정 중인 기록 제외
            .anyMatch(HabitRecord::isMainRecord);
            
            if (hasOtherMainHabitRecord) {
                // 다른 메인 습관 기록이 있으면 서브 기록으로 설정
                finalIsMainRecord = false;
                log.info("해당 날짜에 다른 메인 습관 기록이 있어 서브 기록으로 설정: recordId={}, recordDate={}", 
                        habitRecordId, existingRecord.getRecordDate());
            } else {
                // 메인 기록 자동 결정 - 사용자의 메인 기록 타입에 따라
                if (user.getMainRecordType() == RecordType.HABIT) {
                    // 습관 타입 사용자인 경우: MainRecordDeterminationService 사용
                    finalIsMainRecord = mainRecordDeterminationService.determineMainRecordOnUpdate(
                        command.userId(), 
                        RecordType.HABIT
                    );
                } else {
                    // 다른 타입 사용자인 경우: 서브 기록으로 설정
                    finalIsMainRecord = false;
                    log.info("사용자의 메인 기록 타입이 습관이 아니므로 서브 기록으로 설정: userId=[{}], mainRecordType=[{}]", 
                            command.userId().getValue(), user.getMainRecordType());
                }
                log.info("습관기록 수정으로 메인 기록 자동 결정: recordId={}, autoIsMain={}", 
                        habitRecordId, finalIsMainRecord);
            }
        }
        
        // 메인 기록 상태 업데이트
        updatedRecord = updatedRecord.updateMainRecordStatus(finalIsMainRecord);
        
        // 메인 기록으로 변경되는 경우 기존 메인 기록 처리
        if (finalIsMainRecord && !existingRecord.isMainRecord() && user.getMainRecordType() == RecordType.HABIT) {
            LocalDate recordDate = existingRecord.getRecordDate();
            LocalDate today = LocalDate.now();
            LocalDate habitEndDate = user.getHabitStartDate().plusDays(user.getGoalDays() - 1);
            LocalDate tomorrow = today.plusDays(1);

            // 1. 같은 날짜의 다른 메인 습관 기록들을 서브로 변경
            List<HabitRecord> sameDateMainRecords = habitRecordRepository.findByUserIdAndRecordDate(
                command.userId(), recordDate
            ).stream()
            .filter(record -> !record.getId().equals(existingRecord.getId()))
            .filter(HabitRecord::isMainRecord)
            .toList();

            int updatedCount = 0;
            for (HabitRecord otherMainRecord : sameDateMainRecords) {
                HabitRecord toSubRecord = otherMainRecord.updateMainRecordStatus(false);
                habitRecordRepository.save(toSubRecord);
                updatedCount++;
                log.info("서브→메인 변경으로 같은 날짜 기존 메인 습관 기록을 서브로 변경: habitRecordId={}, recordDate={}",
                        otherMainRecord.getId().getValue(), otherMainRecord.getRecordDate());
            }

            // 2. 내일부터 목표 종료일까지의 기존 메인 습관 기록들을 삭제 (미래 날짜만)
            if (tomorrow.isBefore(habitEndDate) || tomorrow.isEqual(habitEndDate)) {
                List<HabitRecord> futureMainRecords = habitRecordRepository.findByUserIdAndRecordDateBetween(
                    command.userId(), tomorrow, habitEndDate
                ).stream()
                .filter(record -> !record.getId().equals(existingRecord.getId()))
                .filter(HabitRecord::isMainRecord)
                .toList();

                int deletedCount = 0;
                for (HabitRecord futureMainRecord : futureMainRecords) {
                    habitRecordRepository.deleteById(futureMainRecord.getId());
                    deletedCount++;
                    log.info("서브→메인 변경으로 미래 메인 습관 기록 삭제: habitRecordId={}, recordDate={}",
                            futureMainRecord.getId().getValue(), futureMainRecord.getRecordDate());
                }
                log.info("서브→메인 변경 처리 완료: userId={}, 서브로 변경={}, 미래 삭제={}",
                        command.userId().getValue(), updatedCount, deletedCount);
            } else {
                log.info("서브→메인 변경 처리 완료: userId={}, 서브로 변경={}",
                        command.userId().getValue(), updatedCount);
            }
        }
        
        HabitRecord savedRecord = habitRecordRepository.save(updatedRecord);

        log.info("습관기록 수정 완료: habitRecordId=[{}] (스케줄러가 내일부터 자동 생성)", habitRecordId);

        return toResponse(savedRecord);
    }
    
    @CacheEvict(value = "calendar", allEntries = true)
    public void deleteHabitRecord(String habitRecordId, UserId userId) {
        log.info("습관기록 삭제 시작: habitRecordId=[{}], userId=[{}]", habitRecordId, userId.getValue());
        
        // 삭제할 기록 조회
        HabitRecord recordToDelete = habitRecordRepository.findByIdAndUserId(
                HabitRecordId.from(habitRecordId), userId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
        
        if (recordToDelete.isMainRecord()) {
            // 메인 기록 삭제 - 복합 로직 처리
            handleMainRecordDeletion(recordToDelete, userId);
        } else {
            // 서브 기록 삭제 - 단순 삭제
            habitRecordRepository.deleteByIdAndUserId(HabitRecordId.from(habitRecordId), userId);
            log.info("서브 습관기록 삭제 완료: habitRecordId=[{}]", habitRecordId);
        }
    }
    
    /**
     * 메인 기록 삭제 처리 로직
     * 1. 메인+서브 상황: 서브 중 하나를 메인으로 전환
     * 2. 메인만 상황: 목표기간 내 모든 습관 기록 삭제
     */
    private void handleMainRecordDeletion(HabitRecord mainRecordToDelete, UserId userId) {
        LocalDate recordDate = mainRecordToDelete.getRecordDate();
        
        // 같은 날짜의 서브 기록 조회
        List<HabitRecord> subRecordsOnSameDate = habitRecordRepository.findByUserIdAndRecordDate(userId, recordDate)
                .stream()
                .filter(record -> !record.isMainRecord())
                .filter(record -> !record.getId().equals(mainRecordToDelete.getId()))
                .toList();
        
        if (!subRecordsOnSameDate.isEmpty()) {
            // 케이스 1: 메인+서브 상황 - 서브 중 하나를 메인으로 전환
            handleMainWithSubDeletion(mainRecordToDelete, subRecordsOnSameDate, userId);
        } else {
            // 케이스 2: 메인만 상황 - 목표기간 내 모든 습관 기록 삭제
            handleMainOnlyDeletion(mainRecordToDelete, userId);
        }
    }
    
    /**
     * 메인+서브 상황에서 메인 삭제 처리
     * 서브 중 첫 번째를 메인으로 전환하고 오늘부터 목표 종료일까지 기존 메인 기록 삭제 후 내일부터 새 메인 기록 자동 생성
     */
    private void handleMainWithSubDeletion(HabitRecord mainRecord, List<HabitRecord> subRecords, UserId userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        LocalDate today = LocalDate.now();
        LocalDate habitEndDate = user.getHabitStartDate().plusDays(user.getGoalDays() - 1);
        
        // 오늘부터 목표 종료일까지의 기존 메인 습관 기록들을 삭제 (삭제하려는 메인 기록 포함)
        List<HabitRecord> existingMainRecords = habitRecordRepository.findByUserIdAndRecordDateBetween(
            userId, today, habitEndDate
        ).stream()
        .filter(HabitRecord::isMainRecord)
        .filter(record -> record.getHabitType().equals(mainRecord.getHabitType())) // 같은 습관 타입만
        .toList();
        
        int deletedCount = 0;
        for (HabitRecord existingMainRecord : existingMainRecords) {
            habitRecordRepository.deleteById(existingMainRecord.getId());
            deletedCount++;
            log.debug("메인+서브 삭제 시 기존 메인 기록 삭제: habitRecordId={}, date={}", 
                    existingMainRecord.getId().getValue(), existingMainRecord.getRecordDate());
        }
        
        // 서브 중 첫 번째를 메인으로 전환
        HabitRecord newMainRecord = subRecords.get(0).updateMainRecordStatus(true);
        habitRecordRepository.save(newMainRecord);

        log.info("메인+서브 삭제 처리 완료: 기존메인타입=[{}] 삭제된기록수=[{}], 새메인=[{}] 전환 (스케줄러가 내일부터 자동 생성)",
                mainRecord.getHabitType(), deletedCount, newMainRecord.getId().getValue());
    }
    
    /**
     * 메인만 상황에서 메인 삭제 처리
     * 목표기간 내 모든 습관 기록 삭제
     */
    private void handleMainOnlyDeletion(HabitRecord mainRecord, UserId userId) {
        // 현재 메인 기록 삭제
        habitRecordRepository.deleteById(mainRecord.getId());
        
        // 목표기간까지의 연관 습관 기록 모두 삭제
        deleteMainHabitRecordsForRemainingPeriod(userId, mainRecord);
        
        log.info("메인만 삭제 처리 완료: 메인기록=[{}] 및 연관 기록 모두 삭제", 
                mainRecord.getId().getValue());
    }
    
    @CacheEvict(value = "calendar", allEntries = true)
    public HabitRecordResponse updateCompletionStatus(String habitRecordId, UserId userId, boolean isCompleted) {
        log.info("습관기록 완료 상태 변경 시작: habitRecordId=[{}], userId=[{}], isCompleted=[{}]", 
                habitRecordId, userId.getValue(), isCompleted);
        
        HabitRecord existingRecord = habitRecordRepository.findByIdAndUserId(
                HabitRecordId.from(habitRecordId), 
                userId
        ).orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
        
        // 습관 완료는 오늘만 가능 (과거, 미래의 것은 완료/미완료 시킬 수 없음)
        LocalDate today = LocalDate.now();
        if (!existingRecord.getRecordDate().equals(today)) {
            log.warn("습관 완료 상태 변경은 오늘 날짜만 가능: habitRecordId=[{}], recordDate=[{}], today=[{}]", 
                    habitRecordId, existingRecord.getRecordDate(), today);
            throw new CustomException(ErrorCode.HABIT_COMPLETION_ONLY_TODAY);
        }
        
        HabitRecord updatedRecord = existingRecord.updateCompletionStatus(isCompleted);
        
        HabitRecord savedRecord = habitRecordRepository.save(updatedRecord);
        
        // 습관 기록 완료 상태 변경 후 목표 진행률 업데이트
        updateGoalProgress(userId);
        
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
     * 하루 전체 기록 개수 조회 (DAILY + EXERCISE + HABIT 합계)
     */
    private int getTotalRecordCount(UserId userId, LocalDate recordDate) {
        int dailyCount = recordRepository.countByUserIdAndRecordDateAndType(userId, recordDate, RecordType.DAILY);
        int exerciseCount = exerciseRecordRepository.countByUserIdAndRecordDate(userId, recordDate);
        int habitCount = habitRecordRepository.countByUserIdAndRecordDate(userId, recordDate);

        return dailyCount + exerciseCount + habitCount;
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
    
    /**
     * 서브 습관을 메인 습관으로 전환 시:
     * 1. 오늘자 기존 메인기록 → 서브로 변경  
     * 2. 내일부터 목표종료일까지 기존 메인기록 → 삭제
     */
    private void updateExistingMainRecordsToSub(User user, HabitRecord newMainRecord) {
        LocalDate today = LocalDate.now();
        LocalDate habitStartDate = user.getHabitStartDate();
        LocalDate habitEndDate = user.getHabitStartDate().plusDays(user.getGoalDays() - 1);
        LocalDate tomorrow = today.plusDays(1);
        
        // 1. 오늘자 기존 메인 습관 기록을 서브로 변경
        List<HabitRecord> todayMainRecords = habitRecordRepository.findByUserIdAndRecordDate(user.getId(), today)
                .stream()
                .filter(HabitRecord::isMainRecord)
                .filter(record -> !record.getId().equals(newMainRecord.getId())) // 새로 등록한 기록 제외
                .toList();
        
        int updatedCount = 0;
        for (HabitRecord existingMainRecord : todayMainRecords) {
            HabitRecord updatedRecord = existingMainRecord.updateMainRecordStatus(false);
            habitRecordRepository.save(updatedRecord);
            updatedCount++;
            log.debug("오늘자 기존 메인 습관 기록을 서브로 변경: habitRecordId={}, recordDate={}", 
                    existingMainRecord.getId().getValue(), existingMainRecord.getRecordDate());
        }
        
        // 2. 내일부터 목표종료일까지 기존 메인 습관 기록 삭제
        if (tomorrow.isBefore(habitEndDate) || tomorrow.isEqual(habitEndDate)) {
            List<HabitRecord> futureMainRecords = habitRecordRepository.findByUserIdAndRecordDateBetween(
                user.getId(), tomorrow, habitEndDate
            ).stream()
            .filter(HabitRecord::isMainRecord)
            .toList();
            
            int deletedCount = 0;
            for (HabitRecord futureMainRecord : futureMainRecords) {
                habitRecordRepository.deleteById(futureMainRecord.getId());
                deletedCount++;
                log.debug("미래 메인 습관 기록 삭제: habitRecordId={}, recordDate={}, habitType={}", 
                        futureMainRecord.getId().getValue(), futureMainRecord.getRecordDate(), futureMainRecord.getHabitType());
            }
            
            log.info("서브→메인 전환으로 미래 메인 습관 기록 삭제 완료: userId={}, 삭제된 기록 수={}, 기간=[{} ~ {}]", 
                    user.getId().getValue(), deletedCount, tomorrow, habitEndDate);
        }
        
        log.info("서브→메인 전환 처리 완료: userId={}, 오늘자 서브 변경={}, 내일부터 삭제 완료", 
                user.getId().getValue(), updatedCount);
    }

    /**
     * 메인 습관 기록 삭제시 목표 기간 전체의 해당 습관 모든 기록 삭제
     * (습관 포기 의도 - 시작일부터 종료일까지 모든 해당 습관 삭제)
     */
    private void deleteMainHabitRecordsForRemainingPeriod(UserId userId, HabitRecord deletedMainRecord) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        LocalDate habitStartDate = user.getHabitStartDate();
        LocalDate habitEndDate = user.getHabitStartDate().plusDays(user.getGoalDays() - 1);
        
        // 목표 기간 전체(시작일~종료일)에서 해당 습관 타입의 모든 기록 삭제
        List<HabitRecord> allHabitRecordsToDelete = habitRecordRepository.findByUserIdAndRecordDateBetween(
            userId, habitStartDate, habitEndDate
        ).stream()
        .filter(record -> record.getHabitType().equals(deletedMainRecord.getHabitType()))
        .toList();
        
        int deletedCount = 0;
        
        // 목표 기간 전체의 해당 습관 모든 기록 삭제
        for (HabitRecord habitRecord : allHabitRecordsToDelete) {
            habitRecordRepository.deleteById(habitRecord.getId());
            deletedCount++;
            
            log.debug("해당 습관 기록 삭제: habitRecordId={}, date={}, type={}", 
                    habitRecord.getId().getValue(), habitRecord.getRecordDate(), habitRecord.getHabitType());
        }
        
        log.info("습관 포기로 인한 전체 기록 삭제 완료: userId={}, 삭제된 기록 수={}, 습관타입={}, 기간=[{} ~ {}]", 
                userId.getValue(), deletedCount, deletedMainRecord.getHabitType(), habitStartDate, habitEndDate);
    }
    
    /**
     * 목표 진행률 업데이트
     * 현재 진행중인 목표에 대해 완료일수를 재계산하여 업데이트
     */
    private void updateGoalProgress(UserId userId) {
        try {
            var goalApplicationService = applicationContext.getBean("goalApplicationService", 
                com.recordmanagement.habitlog.domain.goal.application.service.GoalApplicationService.class);
            
            // 현재 진행중인 목표 조회
            var currentGoalOpt = goalApplicationService.getCurrentGoal(userId);
            if (currentGoalOpt.isEmpty()) {
                log.debug("진행중인 목표가 없어 진행률 업데이트를 건너뜁니다: userId={}", userId.getValue());
                return;
            }
            
            var currentGoal = currentGoalOpt.get();
            
            // 목표의 기록 타입이 HABIT인 경우만 업데이트
            if (currentGoal.getRecordType() != RecordType.HABIT) {
                log.debug("습관 목표가 아니어서 진행률 업데이트를 건너뜁니다: userId={}, recordType={}", 
                    userId.getValue(), currentGoal.getRecordType());
                return;
            }
            
            // 목표 시작일부터 현재까지의 완료일수 계산
            int completedDays = calculateCompletedDaysForHabit(userId, 
                currentGoal.getStartDate(), java.time.LocalDate.now());
            
            // 목표 진행률 업데이트
            goalApplicationService.updateGoalProgress(userId, completedDays);
            
            log.debug("목표 진행률 업데이트 완료: userId={}, completedDays={}", 
                userId.getValue(), completedDays);
                
        } catch (Exception e) {
            log.error("목표 진행률 업데이트 중 오류 발생: userId={}, error={}", 
                userId.getValue(), e.getMessage(), e);
            // 목표 진행률 업데이트 실패가 습관 기록 처리를 실패시키지 않도록 함
        }
    }
    
    /**
     * 습관 목표의 완료일수 계산
     * 하루에 완료된 습관 기록이 하나라도 있으면 완료로 간주
     * (성능 최적화: N번 쿼리 대신 1번 DISTINCT COUNT 쿼리)
     */
    private int calculateCompletedDaysForHabit(UserId userId,
                                              java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return habitRecordRepository.countCompletedHabitsByUserIdAndDateRange(
            userId, startDate, endDate);
    }
}