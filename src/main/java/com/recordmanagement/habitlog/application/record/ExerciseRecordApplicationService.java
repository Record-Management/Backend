package com.recordmanagement.habitlog.application.record;

import com.recordmanagement.habitlog.application.record.dto.ExerciseRecordCreateCommand;
import com.recordmanagement.habitlog.application.record.dto.ExerciseRecordResponse;
import com.recordmanagement.habitlog.config.exception.CustomException;
import com.recordmanagement.habitlog.config.exception.ErrorCode;
import com.recordmanagement.habitlog.domain.record.model.ExerciseRecord;
import com.recordmanagement.habitlog.domain.record.repository.ExerciseRecordRepository;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 운동 기록 애플리케이션 서비스
 */
@Slf4j
@Service
@Transactional
public class ExerciseRecordApplicationService {
    
    private final ExerciseRecordRepository exerciseRecordRepository;
    
    public ExerciseRecordApplicationService(ExerciseRecordRepository exerciseRecordRepository) {
        this.exerciseRecordRepository = exerciseRecordRepository;
    }
    
    /**
     * 운동 기록 생성 또는 수정
     * - 하루에 운동 기록은 하나만 등록 가능 (회의 결정사항)
     */
    public ExerciseRecordResponse createOrUpdateExerciseRecord(ExerciseRecordCreateCommand command) {
        log.info("운동 기록 생성/수정 시작: userId={}, date={}", 
                command.getUserId(), command.getRecordDate());
        
        UserId userId = UserId.of(command.getUserId());
        
        // 기존 운동 기록 확인
        Optional<ExerciseRecord> existingRecord = exerciseRecordRepository
                .findByUserIdAndRecordDate(userId, command.getRecordDate());
        
        ExerciseRecord savedRecord;
        if (existingRecord.isPresent()) {
            // 기존 기록 수정
            ExerciseRecord record = existingRecord.get();
            record.updateRecord(
                    command.getExerciseType(),
                    command.getCalories(),
                    command.getDurationMinutes(),
                    command.getWeight(),
                    command.getSteps(),
                    command.getMemo()
            );
            savedRecord = exerciseRecordRepository.save(record);
            log.info("운동 기록 수정 완료: recordId={}", savedRecord.getId());
        } else {
            // 새 기록 생성
            ExerciseRecord record = new ExerciseRecord(
                    userId,
                    command.getRecordDate(),
                    command.getExerciseType(),
                    command.getCalories(),
                    command.getDurationMinutes(),
                    command.getWeight(),
                    command.getSteps(),
                    command.getMemo()
            );
            savedRecord = exerciseRecordRepository.save(record);
            log.info("운동 기록 생성 완료: recordId={}", savedRecord.getId());
        }
        
        return ExerciseRecordResponse.from(savedRecord);
    }
    
    /**
     * 사용자의 기간별 운동 기록 조회
     */
    @Transactional(readOnly = true)
    public List<ExerciseRecordResponse> getExerciseRecords(String userId, LocalDate startDate, LocalDate endDate) {
        return exerciseRecordRepository.findByUserIdAndRecordDateBetween(
                        UserId.of(userId), startDate, endDate)
                .stream()
                .map(ExerciseRecordResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 날짜의 운동 기록 조회
     */
    @Transactional(readOnly = true)
    public Optional<ExerciseRecordResponse> getExerciseRecord(String userId, LocalDate recordDate) {
        return exerciseRecordRepository.findByUserIdAndRecordDate(UserId.of(userId), recordDate)
                .map(ExerciseRecordResponse::from);
    }
}