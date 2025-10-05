package com.recordmanagement.habitlog.application.exercise;

import com.recordmanagement.habitlog.application.exercise.dto.*;
import com.recordmanagement.habitlog.config.exception.CustomException;
import com.recordmanagement.habitlog.config.exception.ErrorCode;
import com.recordmanagement.habitlog.domain.exercise.model.ExerciseRecord;
import com.recordmanagement.habitlog.domain.exercise.model.ExerciseRecordId;
import com.recordmanagement.habitlog.domain.exercise.repository.ExerciseRecordRepository;
import com.recordmanagement.habitlog.domain.user.model.RecordType;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import com.recordmanagement.habitlog.infrastructure.file.service.S3FileService;
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
    private final S3FileService s3FileService;
    
    public ExerciseRecordApplicationService(ExerciseRecordRepository exerciseRecordRepository,
                                          S3FileService s3FileService) {
        this.exerciseRecordRepository = exerciseRecordRepository;
        this.s3FileService = s3FileService;
    }
    
    @CacheEvict(value = "calendar", allEntries = true)
    public ExerciseRecordResponse createExerciseRecord(CreateExerciseRecordCommand command) {
        log.info("운동기록 생성 시작: userId=[{}], exerciseType=[{}], recordDate=[{}]", 
                command.userId().getValue(), command.exerciseType(), command.recordDate());
        
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
        
        ExerciseRecord updatedRecord = existingRecord
                .updateExerciseDetails(command.exerciseType(), command.caloriesBurned(), 
                                     command.exerciseTimeMinutes(), command.stepCount())
                .updateWeight(command.weight())
                .updateDailyNote(command.dailyNote())
                .updateImages(command.imageUrls())
                .updateRecordTime(command.recordTime());
        
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
}