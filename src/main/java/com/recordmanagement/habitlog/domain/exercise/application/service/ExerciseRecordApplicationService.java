package com.recordmanagement.habitlog.domain.exercise.application.service;

import com.recordmanagement.habitlog.domain.exercise.application.dto.*;
import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import com.recordmanagement.habitlog.domain.exercise.domain.model.ExerciseRecord;
import com.recordmanagement.habitlog.domain.exercise.domain.model.ExerciseRecordId;
import com.recordmanagement.habitlog.domain.exercise.domain.repository.ExerciseRecordRepository;
import com.recordmanagement.habitlog.domain.record.domain.repository.RecordRepository;
import com.recordmanagement.habitlog.domain.habit.domain.repository.HabitRecordRepository;
import com.recordmanagement.habitlog.domain.record.domain.service.MainRecordDeterminationService;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.file.infrastructure.service.S3FileService;
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
public class ExerciseRecordApplicationService {
    
    private static final Logger log = LoggerFactory.getLogger(ExerciseRecordApplicationService.class);
    
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final RecordRepository recordRepository;
    private final HabitRecordRepository habitRecordRepository;
    private final S3FileService s3FileService;
    private final MainRecordDeterminationService mainRecordDeterminationService;
    
    public ExerciseRecordApplicationService(ExerciseRecordRepository exerciseRecordRepository,
                                          RecordRepository recordRepository,
                                          HabitRecordRepository habitRecordRepository,
                                          S3FileService s3FileService,
                                          MainRecordDeterminationService mainRecordDeterminationService) {
        this.exerciseRecordRepository = exerciseRecordRepository;
        this.recordRepository = recordRepository;
        this.habitRecordRepository = habitRecordRepository;
        this.s3FileService = s3FileService;
        this.mainRecordDeterminationService = mainRecordDeterminationService;
    }
    
    @CacheEvict(value = "calendar", allEntries = true)
    public ExerciseRecordResponse createExerciseRecord(CreateExerciseRecordCommand command) {
        log.info("운동기록 생성 시작: userId=[{}], exerciseType=[{}], recordDate=[{}]", 
                command.userId().getValue(), command.exerciseType(), command.recordDate());
        
        // 하루 최대 2개 운동기록 제한 검증
        int exerciseRecordCount = exerciseRecordRepository.countByUserIdAndRecordDate(
            command.userId(), 
            command.recordDate()
        );
        
        if (exerciseRecordCount >= 2) {
            throw new CustomException(ErrorCode.EXERCISE_RECORD_LIMIT_EXCEEDED);
        }
        
        // 전체 기록 종류 최대 2가지 제한 검증 (운동기록이 없는 경우에만)
        if (exerciseRecordCount == 0) {
            validateRecordTypeLimit(command.userId(), command.recordDate());
        }
        
        // 메인 기록 결정
        boolean isMainRecord = mainRecordDeterminationService.determineMainRecord(
            command.userId(), 
            RecordType.EXERCISE, 
            command.recordDate(), 
            exerciseRecordCount
        );
        
        ExerciseRecord exerciseRecord = ExerciseRecord.create(
            command.userId(),
            command.exerciseType(),
            command.caloriesBurned(),
            command.exerciseTimeMinutes(),
            command.stepCount(),
            command.weight(),
            command.dailyNote(),
            command.imageUrls(),
            command.recordDate(),
            command.recordTime()
        );
        
        // 메인 기록 상태 설정 (ExerciseRecord 도메인에 isMainRecord 필드가 있다면)
        // exerciseRecord = exerciseRecord.updateMainRecordStatus(isMainRecord);
        
        ExerciseRecord savedRecord = exerciseRecordRepository.save(exerciseRecord);
        
        log.info("운동기록 생성 완료: exerciseRecordId=[{}]", savedRecord.getId().getValue());
        
        return toResponse(savedRecord);
    }
    
    @CacheEvict(value = "calendar", allEntries = true)
    public ExerciseRecordResponse updateExerciseRecord(UpdateExerciseRecordCommand command) {
        log.info("운동기록 수정 시작: exerciseRecordId=[{}], userId=[{}]", 
                command.exerciseRecordId().getValue(), command.userId().getValue());
        
        ExerciseRecord existingRecord = exerciseRecordRepository.findByIdAndUserId(
                command.exerciseRecordId(), command.userId())
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
        
        // 운동 타입이 변경되는 경우 메인 기록 결정 (현재 ExerciseRecord에서는 타입 변경이 없으므로 주석 처리)
        // boolean isMainRecord = mainRecordDeterminationService.determineMainRecordOnUpdate(
        //     command.userId(), 
        //     RecordType.EXERCISE
        // );
        // log.info("운동기록 수정으로 메인 기록 재결정: recordId={}, isMain={}", 
        //         command.exerciseRecordId().getValue(), isMainRecord);
        
        ExerciseRecord updatedRecord = existingRecord
                .updateExerciseDetails(command.exerciseType(), command.caloriesBurned(), 
                                     command.exerciseTimeMinutes(), command.stepCount())
                .updateWeight(command.weight())
                .updateDailyNote(command.dailyNote())
                .updateImages(command.imageUrls())
                .updateRecordTime(command.recordTime());
        
        // 메인 기록 상태 업데이트 (ExerciseRecord 도메인에 해당 메서드가 있다면)
        // updatedRecord = updatedRecord.updateMainRecordStatus(isMainRecord);
        
        ExerciseRecord savedRecord = exerciseRecordRepository.save(updatedRecord);
        
        log.info("운동기록 수정 완료: exerciseRecordId=[{}]", savedRecord.getId().getValue());
        
        return toResponse(savedRecord);
    }
    
    @Transactional(readOnly = true)
    public DailyExerciseRecordResponse getDailyExerciseRecords(String userIdValue, LocalDate date) {
        log.info("일일 운동기록 조회 시작: userId=[{}], date=[{}]", userIdValue, date);
        
        UserId userId = UserId.of(userIdValue);
        List<ExerciseRecord> exerciseRecords = exerciseRecordRepository.findByUserIdAndRecordDate(userId, date);
        
        List<ExerciseRecordResponse> responseList = exerciseRecords.stream()
                .map(this::toResponse)
                .map(this::updateImageUrls)
                .collect(Collectors.toList());
        
        log.info("일일 운동기록 조회 완료: userId=[{}], date=[{}], count=[{}]", 
                userIdValue, date, responseList.size());
        
        return new DailyExerciseRecordResponse(date, responseList);
    }
    
    @Transactional(readOnly = true)
    public List<ExerciseRecordResponse> getExerciseRecordsBetween(String userIdValue, LocalDate startDate, LocalDate endDate) {
        log.info("기간별 운동기록 조회 시작: userId=[{}], startDate=[{}], endDate=[{}]", 
                userIdValue, startDate, endDate);
        
        UserId userId = UserId.of(userIdValue);
        List<ExerciseRecord> exerciseRecords = exerciseRecordRepository.findByUserIdAndRecordDateBetween(
                userId, startDate, endDate);
        
        List<ExerciseRecordResponse> responseList = exerciseRecords.stream()
                .map(this::toResponse)
                .map(this::updateImageUrls)
                .collect(Collectors.toList());
        
        log.info("기간별 운동기록 조회 완료: userId=[{}], startDate=[{}], endDate=[{}], count=[{}]", 
                userIdValue, startDate, endDate, responseList.size());
        
        return responseList;
    }
    
    @CacheEvict(value = "calendar", allEntries = true)
    public void deleteExerciseRecord(String exerciseRecordId, String userIdValue) {
        log.info("운동기록 삭제 시작: exerciseRecordId=[{}], userId=[{}]", exerciseRecordId, userIdValue);
        
        ExerciseRecordId recordId = ExerciseRecordId.from(exerciseRecordId);
        UserId userId = UserId.of(userIdValue);
        
        if (!exerciseRecordRepository.existsByIdAndUserId(recordId, userId)) {
            throw new CustomException(ErrorCode.RECORD_NOT_FOUND);
        }
        
        exerciseRecordRepository.deleteByIdAndUserId(recordId, userId);
        
        log.info("운동기록 삭제 완료: exerciseRecordId=[{}]", exerciseRecordId);
    }
    
    private ExerciseRecordResponse toResponse(ExerciseRecord exerciseRecord) {
        return new ExerciseRecordResponse(
            // 공통 필드
            exerciseRecord.getId().getValue(),
            RecordType.EXERCISE,
            exerciseRecord.getRecordDate(),
            exerciseRecord.getRecordTime(), // 운동 기록 시간 추가
            exerciseRecord.getCreatedAt(),
            exerciseRecord.getUpdatedAt(),
            
            // 운동기록 전용 필드
            exerciseRecord.getExerciseType(),
            exerciseRecord.getCaloriesBurned(),
            exerciseRecord.getExerciseTimeMinutes(),
            exerciseRecord.getStepCount(),
            exerciseRecord.getWeight(),
            exerciseRecord.getDailyNote(),
            exerciseRecord.getImageUrls()
        );
    }
    
    /**
     * ExerciseRecordResponse의 이미지 URL을 새로운 Pre-signed URL로 업데이트
     */
    private ExerciseRecordResponse updateImageUrls(ExerciseRecordResponse response) {
        if (response.imageUrls() == null || response.imageUrls().isEmpty()) {
            return response;
        }
        
        List<String> updatedUrls = s3FileService.regeneratePresignedUrls(response.imageUrls());
        return response.withUpdatedImageUrls(updatedUrls);
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
        
        // 습관 기록 확인
        int habitCount = habitRecordRepository.countByUserIdAndRecordDate(userId, recordDate);
        if (habitCount > 0) recordTypeCount++;
        
        // 이미 2가지 기록 종류가 있다면 운동기록을 추가할 수 없음
        if (recordTypeCount >= 2) {
            throw new CustomException(ErrorCode.RECORD_TYPE_LIMIT_EXCEEDED);
        }
    }
}