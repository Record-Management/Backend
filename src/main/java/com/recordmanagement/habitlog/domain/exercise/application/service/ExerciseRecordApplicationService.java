package com.recordmanagement.habitlog.domain.exercise.application.service;

import com.recordmanagement.habitlog.domain.exercise.application.dto.*;
import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import com.recordmanagement.habitlog.domain.exercise.domain.model.ExerciseRecord;
import com.recordmanagement.habitlog.domain.exercise.domain.model.ExerciseRecordId;
import com.recordmanagement.habitlog.domain.exercise.domain.repository.ExerciseRecordQueryRepository;
import com.recordmanagement.habitlog.domain.exercise.domain.repository.ExerciseRecordRepository;
import com.recordmanagement.habitlog.domain.record.domain.repository.RecordRepository;
import com.recordmanagement.habitlog.domain.habit.domain.repository.HabitRecordRepository;
import com.recordmanagement.habitlog.domain.record.domain.service.MainRecordDeterminationService;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.file.infrastructure.service.S3FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExerciseRecordApplicationService {
    
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final ExerciseRecordQueryRepository exerciseRecordQueryRepository;
    private final RecordRepository recordRepository;
    private final HabitRecordRepository habitRecordRepository;
    private final S3FileService s3FileService;
    private final MainRecordDeterminationService mainRecordDeterminationService;
    private final ApplicationContext applicationContext;
    
    @CacheEvict(value = "calendar", allEntries = true)
    public ExerciseRecordResponse createExerciseRecord(CreateExerciseRecordCommand command) {
        log.info("운동기록 생성 시작: userId=[{}], exerciseType=[{}], recordDate=[{}]",
                command.userId().getValue(), command.exerciseType(), command.recordDate());

        // 하루 최대 2개 기록 제한 검증 (전체 타입 합쳐서)
        int totalRecordCount = getTotalRecordCount(command.userId(), command.recordDate());

        if (totalRecordCount >= 2) {
            throw new CustomException(ErrorCode.EXERCISE_RECORD_LIMIT_EXCEEDED);
        }

        // 전체 기록 종류 최대 2가지 제한 검증
        int exerciseRecordCount = exerciseRecordRepository.countByUserIdAndRecordDate(
            command.userId(),
            command.recordDate()
        );

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
        
        // 메인 기록 상태 설정 (ExerciseRecord에는 isMainRecord 필드가 없음)
        // 추후 ExerciseRecord에 메인/서브 필드 추가 시 활성화
        // exerciseRecord = exerciseRecord.updateMainRecordStatus(isMainRecord);
        
        ExerciseRecord savedRecord = exerciseRecordRepository.save(exerciseRecord);
        
        // 운동 기록 생성 후 목표 진행률 업데이트
        updateGoalProgress(command.userId());
        
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
        
        // 메인 기록 결정 (사용자 메인 기록 타입과 일치 여부 확인)
        boolean isMainRecord = mainRecordDeterminationService.determineMainRecordOnUpdate(
            command.userId(), 
            RecordType.EXERCISE
        );
        log.info("운동기록 수정으로 메인 기록 결정: recordId={}, isMain={}", 
                command.exerciseRecordId().getValue(), isMainRecord);
        
        ExerciseRecord updatedRecord = existingRecord
                .updateExerciseDetails(command.exerciseType(), command.caloriesBurned(), 
                                     command.exerciseTimeMinutes(), command.stepCount())
                .updateWeight(command.weight())
                .updateDailyNote(command.dailyNote())
                .updateImages(command.imageUrls())
                .updateRecordTime(command.recordTime());
        
        // 메인 기록 상태 업데이트 (ExerciseRecord에는 isMainRecord 필드가 없음)
        // 추후 ExerciseRecord에 메인/서브 필드 추가 시 활성화
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
        
        // 습관 기록 확인
        int habitCount = habitRecordRepository.countByUserIdAndRecordDate(userId, recordDate);
        if (habitCount > 0) recordTypeCount++;
        
        // 이미 2가지 기록 종류가 있다면 운동기록을 추가할 수 없음
        if (recordTypeCount >= 2) {
            throw new CustomException(ErrorCode.RECORD_TYPE_LIMIT_EXCEEDED);
        }
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
            
            // 목표의 기록 타입이 EXERCISE인 경우만 업데이트
            if (currentGoal.getRecordType() != RecordType.EXERCISE) {
                log.debug("운동 목표가 아니어서 진행률 업데이트를 건너뜁니다: userId={}, recordType={}", 
                    userId.getValue(), currentGoal.getRecordType());
                return;
            }
            
            // 목표 시작일부터 현재까지의 완료일수 계산
            int completedDays = calculateCompletedDaysForExercise(userId, 
                currentGoal.getStartDate(), java.time.LocalDate.now());
            
            // 목표 진행률 업데이트
            goalApplicationService.updateGoalProgress(userId, completedDays);
            
            log.debug("목표 진행률 업데이트 완료: userId={}, completedDays={}", 
                userId.getValue(), completedDays);
                
        } catch (Exception e) {
            log.error("목표 진행률 업데이트 중 오류 발생: userId={}, error={}", 
                userId.getValue(), e.getMessage(), e);
            // 목표 진행률 업데이트 실패가 운동 기록 처리를 실패시키지 않도록 함
        }
    }
    
    /**
     * 운동 목표의 완료일수 계산
     * 하루에 운동 기록이 하나라도 있으면 완료로 간주
     * (성능 최적화: N번 쿼리 대신 1번 DISTINCT COUNT 쿼리)
     */
    private int calculateCompletedDaysForExercise(UserId userId,
                                                 java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return exerciseRecordQueryRepository.countDistinctRecordDatesByUserIdAndDateRange(
            userId, startDate, endDate);
    }
}