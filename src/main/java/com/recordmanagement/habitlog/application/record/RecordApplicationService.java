package com.recordmanagement.habitlog.application.record;

import com.recordmanagement.habitlog.application.record.dto.CalendarRecordResponse;
import com.recordmanagement.habitlog.application.record.dto.CalendarResponse;
import com.recordmanagement.habitlog.application.record.dto.CreateRecordCommand;
import com.recordmanagement.habitlog.application.record.dto.DailyRecordResponse;
import com.recordmanagement.habitlog.application.record.dto.RecordResponse;
import com.recordmanagement.habitlog.application.record.dto.UnifiedRecordResponse;
import com.recordmanagement.habitlog.application.record.dto.UpdateRecordCommand;
import com.recordmanagement.habitlog.config.exception.CustomException;
import com.recordmanagement.habitlog.config.exception.ErrorCode;
import com.recordmanagement.habitlog.domain.record.model.Record;
import com.recordmanagement.habitlog.domain.record.model.RecordId;
import com.recordmanagement.habitlog.domain.record.repository.RecordRepository;
import com.recordmanagement.habitlog.domain.exercise.repository.ExerciseRecordRepository;
import com.recordmanagement.habitlog.domain.exercise.model.ExerciseRecord;
import com.recordmanagement.habitlog.domain.exercise.model.ExerciseRecordId;
import com.recordmanagement.habitlog.application.exercise.dto.ExerciseRecordResponse;
import com.recordmanagement.habitlog.domain.user.model.RecordType;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import com.recordmanagement.habitlog.infrastructure.file.service.S3FileService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecordApplicationService {
    
    private final RecordRepository recordRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final S3FileService s3FileService;
    
    public RecordApplicationService(RecordRepository recordRepository, 
                                   ExerciseRecordRepository exerciseRecordRepository,
                                   S3FileService s3FileService) {
        this.recordRepository = recordRepository;
        this.exerciseRecordRepository = exerciseRecordRepository;
        this.s3FileService = s3FileService;
    }
    
    @CacheEvict(value = "calendar", allEntries = true)
    public RecordResponse createRecord(CreateRecordCommand command) {
        // 일상 기록인 경우 하루 최대 2개 제한 검증
        if (command.type() == RecordType.DAILY) {
            int dailyRecordCount = recordRepository.countByUserIdAndRecordDateAndType(
                command.userId(), 
                command.recordDate(), 
                RecordType.DAILY
            );
            
            if (dailyRecordCount >= 2) {
                throw new CustomException(ErrorCode.DAILY_RECORD_LIMIT_EXCEEDED);
            }
        }
        
        Record record = Record.create(
            command.userId(),
            command.type(),
            command.emotion(),
            command.content(),
            command.imageUrls(),
            command.recordDate(),
            command.recordTime()
        );
        
        Record savedRecord = recordRepository.save(record);
        return RecordResponse.from(savedRecord);
    }
    
    public RecordResponse updateRecord(UpdateRecordCommand command) {
        Record existingRecord = recordRepository.findById(command.recordId())
            .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
        
        // 작성자 확인
        validateRecordOwnership(existingRecord, command.userId());
        
        // 기록 수정 (날짜와 시간은 불변)
        Record updatedRecord = existingRecord
            .updateType(command.type())
            .updateEmotion(command.emotion())
            .updateContent(command.content())
            .updateImages(command.imageUrls());
        
        Record savedRecord = recordRepository.save(updatedRecord);
        return RecordResponse.from(savedRecord);
    }
    
    public void deleteRecord(String recordId, String userId) {
        Record existingRecord = recordRepository.findById(RecordId.from(recordId))
            .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
        
        // 작성자 확인
        validateRecordOwnership(existingRecord, UserId.of(userId));
        
        recordRepository.deleteById(RecordId.from(recordId));
    }
    
    @Cacheable(value = "calendar", key = "#userId + '_' + #year + '_' + #month + '_' + (#type != null ? #type.toString() : 'ALL')")
    @Transactional(readOnly = true)
    public CalendarResponse getCalendar(String userId, int year, int month, RecordType type) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        UserId userIdObj = UserId.of(userId);
        
        // 모든 타입의 기록을 통합하여 조회
        List<UnifiedRecordResponse> allRecords = new ArrayList<>();
        
        // 1. 일상 기록 조회 (type이 null이거나 DAILY인 경우)
        if (type == null || type == RecordType.DAILY) {
            List<Record> dailyRecords = recordRepository.findByUserIdAndRecordDateBetweenAndTypeIn(
                userIdObj, startDate, endDate, List.of(RecordType.DAILY)
            );
            allRecords.addAll(dailyRecords.stream()
                .map(UnifiedRecordResponse::fromRecord)
                .toList());
        }
        
        // 2. 운동 기록 조회 (type이 null이거나 EXERCISE인 경우)
        if (type == null || type == RecordType.EXERCISE) {
            List<ExerciseRecord> exerciseRecords = exerciseRecordRepository.findByUserIdAndRecordDateBetween(
                userIdObj, startDate, endDate
            );
            allRecords.addAll(exerciseRecords.stream()
                .map(UnifiedRecordResponse::fromExerciseRecord)
                .toList());
        }
        
        // TODO: 3. 습관 기록 조회 (type이 null이거나 HABIT인 경우)
        // TODO: 4. 일정 기록 조회 (type이 null이거나 SCHEDULE인 경우)
        
        // 날짜별로 그룹핑 (UnifiedRecordResponse 기준)
        Map<LocalDate, List<UnifiedRecordResponse>> recordsByDate = allRecords.stream()
            .collect(Collectors.groupingBy(UnifiedRecordResponse::recordDate));
        
        // 캘린더 응답 생성 - CalendarRecordResponse.RecordSummary로 변환
        List<CalendarRecordResponse> calendarRecords = recordsByDate.entrySet()
            .stream()
            .map(entry -> {
                List<CalendarRecordResponse.RecordSummary> summaries = entry.getValue().stream()
                    .map(CalendarRecordResponse.RecordSummary::from)
                    .toList();
                return new CalendarRecordResponse(entry.getKey(), summaries);
            })
            .sorted((a, b) -> a.date().compareTo(b.date()))
            .toList();
        
        return CalendarResponse.of(yearMonth, calendarRecords);
    }
    
    @Transactional(readOnly = true)
    public DailyRecordResponse getRecordsByDate(String userId, LocalDate date) {
        UserId userIdObj = UserId.of(userId);
        
        // 모든 타입의 기록을 조회
        List<UnifiedRecordResponse> allRecords = new ArrayList<>();
        
        // 1. 일상 기록 조회
        List<Record> dailyRecords = recordRepository.findByUserIdAndRecordDate(userIdObj, date);
        allRecords.addAll(dailyRecords.stream()
            .map(UnifiedRecordResponse::fromRecord)
            .toList());
        
        // 2. 운동 기록 조회
        List<ExerciseRecord> exerciseRecords = exerciseRecordRepository.findByUserIdAndRecordDate(userIdObj, date);
        allRecords.addAll(exerciseRecords.stream()
            .map(UnifiedRecordResponse::fromExerciseRecord)
            .toList());
        
        // TODO: 3. 습관 기록 조회
        // TODO: 4. 일정 기록 조회
        
        // Pre-signed URL 재생성
        List<UnifiedRecordResponse> recordsWithUpdatedUrls = allRecords.stream()
            .map(this::updateImageUrls)
            .toList();
        
        return DailyRecordResponse.of(date, recordsWithUpdatedUrls);
    }
    
    @Transactional(readOnly = true)
    public UnifiedRecordResponse getRecordById(String userId, String recordId) {
        UserId userIdObj = UserId.of(userId);
        
        // 먼저 일상 기록에서 조회 시도
        try {
            RecordId recordIdObj = RecordId.from(recordId);
            Optional<Record> dailyRecord = recordRepository.findById(recordIdObj);
            
            if (dailyRecord.isPresent()) {
                Record record = dailyRecord.get();
                // 작성자 확인
                validateRecordOwnership(record, userIdObj);
                
                UnifiedRecordResponse response = UnifiedRecordResponse.fromRecord(record);
                return updateImageUrls(response);
            }
        } catch (Exception e) {
            // RecordId 파싱 실패 시 운동 기록 조회 시도
        }
        
        // 운동 기록에서 조회 시도
        try {
            ExerciseRecordId exerciseRecordId = ExerciseRecordId.from(recordId);
            Optional<ExerciseRecord> exerciseRecord = exerciseRecordRepository.findByIdAndUserId(exerciseRecordId, userIdObj);
            
            if (exerciseRecord.isPresent()) {
                UnifiedRecordResponse response = UnifiedRecordResponse.fromExerciseRecord(exerciseRecord.get());
                return updateImageUrls(response);
            }
        } catch (Exception e) {
            // ExerciseRecordId 파싱 실패
        }
        
        // 모든 조회 시도가 실패한 경우
        throw new CustomException(ErrorCode.RECORD_NOT_FOUND);
    }
    
    /**
     * 기록 소유권 검증 (중복 코드 제거)
     */
    private void validateRecordOwnership(Record record, UserId userId) {
        if (!record.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.RECORD_ACCESS_DENIED);
        }
    }
    
    /**
     * UnifiedRecordResponse의 이미지 URL을 새로운 Pre-signed URL로 업데이트
     */
    private UnifiedRecordResponse updateImageUrls(UnifiedRecordResponse record) {
        if (record.imageUrls() == null || record.imageUrls().isEmpty()) {
            return record;
        }
        
        List<String> updatedUrls = s3FileService.regeneratePresignedUrls(record.imageUrls());
        return record.withUpdatedImageUrls(updatedUrls);
    }
}