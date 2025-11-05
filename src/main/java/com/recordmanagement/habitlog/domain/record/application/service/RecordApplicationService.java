package com.recordmanagement.habitlog.domain.record.application.service;

import com.recordmanagement.habitlog.domain.record.application.dto.CalendarRecordResponse;
import com.recordmanagement.habitlog.domain.record.application.dto.CalendarResponse;
import com.recordmanagement.habitlog.domain.record.application.dto.CreateRecordCommand;
import com.recordmanagement.habitlog.domain.record.application.dto.DailyRecordResponse;
import com.recordmanagement.habitlog.domain.record.application.dto.RecordResponse;
import com.recordmanagement.habitlog.domain.record.application.dto.UnifiedRecordResponse;
import com.recordmanagement.habitlog.domain.record.application.dto.UpdateRecordCommand;
import com.recordmanagement.habitlog.domain.record.application.strategy.RecordTypeValidationStrategyFactory;
import com.recordmanagement.habitlog.domain.record.domain.service.MainRecordDeterminationService;
import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import org.springframework.context.ApplicationContext;
import com.recordmanagement.habitlog.domain.record.domain.model.Record;
import com.recordmanagement.habitlog.domain.record.domain.model.RecordId;
import com.recordmanagement.habitlog.domain.record.domain.repository.RecordRepository;
import com.recordmanagement.habitlog.domain.exercise.domain.repository.ExerciseRecordQueryRepository;
import com.recordmanagement.habitlog.domain.exercise.domain.repository.ExerciseRecordSecurityRepository;
import com.recordmanagement.habitlog.domain.exercise.domain.model.ExerciseRecord;
import com.recordmanagement.habitlog.domain.exercise.domain.model.ExerciseRecordId;
import com.recordmanagement.habitlog.domain.exercise.application.dto.ExerciseRecordResponse;
import com.recordmanagement.habitlog.domain.habit.domain.repository.HabitRecordRepository;
import com.recordmanagement.habitlog.domain.habit.domain.model.HabitRecord;
import com.recordmanagement.habitlog.domain.habit.domain.model.HabitRecordId;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
import com.recordmanagement.habitlog.domain.file.infrastructure.service.S3FileService;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 기록 애플리케이션 서비스
 * 
 * OCP 적용: Strategy 패턴을 통한 확장 가능한 설계
 * ISP 적용: 필요한 기능별로 분리된 Repository 인터페이스 사용
 * - 새로운 기록 타입 추가 시 기존 코드 수정 없이 확장 가능
 * - Factory 패턴을 통한 전략 관리로 switch 문 제거
 * - 조회 전용 기능에는 QueryRepository만 의존하여 불필요한 의존성 제거
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 3.0.0 (OCP + ISP 적용)
 */
@Slf4j
@Service
@Transactional
public class RecordApplicationService {
    
    private final RecordRepository recordRepository;
    private final ExerciseRecordQueryRepository exerciseRecordQueryRepository;
    private final ExerciseRecordSecurityRepository exerciseRecordSecurityRepository;
    private final HabitRecordRepository habitRecordRepository;
    private final UserRepository userRepository;
    private final S3FileService s3FileService;
    private final MainRecordDeterminationService mainRecordDeterminationService;
    private final RecordTypeValidationStrategyFactory validationStrategyFactory;
    private final ApplicationContext applicationContext;
    
    public RecordApplicationService(RecordRepository recordRepository, 
                                   ExerciseRecordQueryRepository exerciseRecordQueryRepository,
                                   ExerciseRecordSecurityRepository exerciseRecordSecurityRepository,
                                   HabitRecordRepository habitRecordRepository,
                                   UserRepository userRepository,
                                   S3FileService s3FileService,
                                   MainRecordDeterminationService mainRecordDeterminationService,
                                   RecordTypeValidationStrategyFactory validationStrategyFactory,
                                   ApplicationContext applicationContext) {
        this.recordRepository = recordRepository;
        this.exerciseRecordQueryRepository = exerciseRecordQueryRepository;
        this.exerciseRecordSecurityRepository = exerciseRecordSecurityRepository;
        this.habitRecordRepository = habitRecordRepository;
        this.userRepository = userRepository;
        this.s3FileService = s3FileService;
        this.mainRecordDeterminationService = mainRecordDeterminationService;
        this.validationStrategyFactory = validationStrategyFactory;
        this.applicationContext = applicationContext;
    }
    
    @CacheEvict(value = "calendar", allEntries = true)
    public RecordResponse createRecord(CreateRecordCommand command) {
        // 기존 기록 개수 조회 (메인 기록 결정에 필요)
        int existingRecordCount = 0;
        
        // 일상 기록인 경우 하루 최대 2개 제한 검증
        if (command.type() == RecordType.DAILY) {
            existingRecordCount = recordRepository.countByUserIdAndRecordDateAndType(
                command.userId(), 
                command.recordDate(), 
                RecordType.DAILY
            );
            
            if (existingRecordCount >= 2) {
                throw new CustomException(ErrorCode.DAILY_RECORD_LIMIT_EXCEEDED);
            }
        }
        
        // 전체 기록 종류 최대 2가지 제한 검증
        validateRecordTypeLimit(command.userId(), command.recordDate(), RecordType.DAILY);
        
        // 메인 기록 결정
        boolean isMainRecord = mainRecordDeterminationService.determineMainRecord(
            command.userId(), 
            command.type(), 
            command.recordDate(), 
            existingRecordCount
        );
        
        Record record = Record.create(
            command.userId(),
            command.type(),
            command.emotion(),
            command.content(),
            command.imageUrls(),
            command.recordDate(),
            command.recordTime()
        );
        
        // 메인 기록 상태 설정 (Record 도메인에 isMainRecord 필드가 있다면)
        // record = record.updateMainRecordStatus(isMainRecord);
        
        Record savedRecord = recordRepository.save(record);
        
        // 기록 생성 후 목표 진행률 업데이트
        updateGoalProgress(command.userId());
        
        return RecordResponse.from(savedRecord);
    }
    
    @CacheEvict(value = "calendar", allEntries = true)
    public RecordResponse updateRecord(UpdateRecordCommand command) {
        Record existingRecord = recordRepository.findById(command.recordId())
            .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));
        
        // 작성자 확인
        validateRecordOwnership(existingRecord, command.userId());
        
        // 기록 타입이 변경되는 경우 메인 기록 결정
        boolean isMainRecord = false;
        if (!existingRecord.getType().equals(command.type())) {
            isMainRecord = mainRecordDeterminationService.determineMainRecordOnUpdate(
                command.userId(), 
                command.type()
            );
            log.info("기록 타입 변경으로 메인 기록 재결정: recordId={}, oldType={}, newType={}, isMain={}", 
                    command.recordId().value(), existingRecord.getType(), command.type(), isMainRecord);
        }
        
        // 기록 수정 (날짜와 시간은 불변)
        Record updatedRecord = existingRecord
            .updateType(command.type())
            .updateEmotion(command.emotion())
            .updateContent(command.content())
            .updateImages(command.imageUrls());
        
        // 타입이 변경된 경우 메인 기록 상태도 업데이트 (Record 도메인에 해당 메서드가 있다면)
        // if (!existingRecord.getType().equals(command.type())) {
        //     updatedRecord = updatedRecord.updateMainRecordStatus(isMainRecord);
        // }
        
        Record savedRecord = recordRepository.save(updatedRecord);
        
        return RecordResponse.from(savedRecord);
    }
    
    @CacheEvict(value = "calendar", allEntries = true)
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
        
        // 사용자 정보 조회 (메인 기록 타입 확인용)
        User user = userRepository.findById(userIdObj)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        
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
            List<ExerciseRecord> exerciseRecords = exerciseRecordQueryRepository.findByUserIdAndRecordDateBetween(
                userIdObj, startDate, endDate
            );
            allRecords.addAll(exerciseRecords.stream()
                .map(UnifiedRecordResponse::fromExerciseRecord)
                .toList());
        }
        
        // 3. 습관 기록 조회 (type이 null이거나 HABIT인 경우)
        if (type == null || type == RecordType.HABIT) {
            List<HabitRecord> habitRecords = habitRecordRepository.findByUserIdAndRecordDateBetween(
                userIdObj, startDate, endDate
            );
            allRecords.addAll(habitRecords.stream()
                .map(UnifiedRecordResponse::fromHabitRecord)
                .toList());
            
            // 메인 습관 기록 자동 생성 로직 추가
            generatePlaceholderMainHabitRecords(userIdObj, startDate, endDate, allRecords);
        }
        
        // TODO: 4. 일정 기록 조회 (type이 null이거나 SCHEDULE인 경우)
        
        // 날짜별로 그룹핑 (UnifiedRecordResponse 기준)
        Map<LocalDate, List<UnifiedRecordResponse>> recordsByDate = allRecords.stream()
            .collect(Collectors.groupingBy(UnifiedRecordResponse::recordDate));
        
        // 캘린더 응답 생성 - CalendarRecordResponse.RecordSummary로 변환
        List<CalendarRecordResponse> calendarRecords = recordsByDate.entrySet()
            .stream()
            .map(entry -> {
                LocalDate date = entry.getKey();
                List<CalendarRecordResponse.RecordSummary> summaries = entry.getValue().stream()
                    .map(CalendarRecordResponse.RecordSummary::from)
                    .toList();
                
                // 해당 날짜의 메인 기록 타입 결정
                RecordType mainRecordTypeForDate = determineMainRecordTypeForDate(user, date);
                
                return new CalendarRecordResponse(date, mainRecordTypeForDate, summaries);
            })
            .sorted((a, b) -> a.date().compareTo(b.date()))
            .toList();
        
        return CalendarResponse.of(yearMonth, calendarRecords);
    }
    
    /**
     * 특정 날짜의 메인 기록 타입을 결정합니다.
     * 현재 구현: 사용자의 현재 메인 기록 타입을 반환
     * TODO: 향후 목표 이력 기능 구현시 해당 날짜의 실제 목표 기간 메인 타입 반환
     */
    private RecordType determineMainRecordTypeForDate(User user, LocalDate date) {
        // 현재는 사용자의 현재 메인 기록 타입을 반환
        // 목표 포기 기능이 없고, 목표 재설정만 가능하므로 단순하게 처리
        return user.getMainRecordType();
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
        List<ExerciseRecord> exerciseRecords = exerciseRecordQueryRepository.findByUserIdAndRecordDate(userIdObj, date);
        allRecords.addAll(exerciseRecords.stream()
            .map(UnifiedRecordResponse::fromExerciseRecord)
            .toList());
        
        // 3. 습관 기록 조회
        List<HabitRecord> habitRecords = habitRecordRepository.findByUserIdAndRecordDate(userIdObj, date);
        allRecords.addAll(habitRecords.stream()
            .map(UnifiedRecordResponse::fromHabitRecord)
            .toList());
        
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
            Optional<ExerciseRecord> exerciseRecord = exerciseRecordSecurityRepository.findByIdAndUserId(exerciseRecordId, userIdObj);
            
            if (exerciseRecord.isPresent()) {
                UnifiedRecordResponse response = UnifiedRecordResponse.fromExerciseRecord(exerciseRecord.get());
                return updateImageUrls(response);
            }
        } catch (Exception e) {
            // ExerciseRecordId 파싱 실패
        }
        
        // 습관 기록에서 조회 시도
        try {
            HabitRecordId habitRecordId = HabitRecordId.from(recordId);
            Optional<HabitRecord> habitRecord = habitRecordRepository.findByIdAndUserId(habitRecordId, userIdObj);
            
            if (habitRecord.isPresent()) {
                UnifiedRecordResponse response = UnifiedRecordResponse.fromHabitRecord(habitRecord.get());
                return updateImageUrls(response);
            }
        } catch (Exception e) {
            // HabitRecordId 파싱 실패
        }
        
        // 모든 조회 시도가 실패한 경우
        throw new CustomException(ErrorCode.RECORD_NOT_FOUND);
    }
    
    /**
     * 메인 습관 기록 자동 생성 로직 (캘린더 조회용)
     * 사용자의 메인 기록 타입이 HABIT인 경우, 습관 시작일부터 목표 날짜까지의 기간 중
     * 실제 기록이 없는 날짜에 대해 플레이스홀더 메인 습관 기록을 생성
     * 
     * 단, 사용자가 모든 습관 기록을 삭제한 경우에는 플레이스홀더를 생성하지 않음
     */
    private void generatePlaceholderMainHabitRecords(UserId userId, LocalDate startDate, LocalDate endDate, 
                                                   List<UnifiedRecordResponse> allRecords) {
        // 사용자 정보 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 메인 기록 타입이 HABIT이 아닌 경우 처리하지 않음
        if (user.getMainRecordType() != RecordType.HABIT) {
            return;
        }
        
        // 습관 시작일이 설정되지 않은 경우 처리하지 않음
        if (user.getHabitStartDate() == null) {
            return;
        }
        
        // 습관 기간 범위 계산
        LocalDate habitStartDate = user.getHabitStartDate();
        LocalDate habitEndDate = habitStartDate.plusDays(user.getGoalDays() - 1);
        
        // 사용자가 모든 습관을 포기했는지 확인 (전체 습관 기간에 습관 기록이 하나도 없는 경우)
        boolean hasAnyHabitRecord = habitRecordRepository.findByUserIdAndRecordDateBetween(
            userId, habitStartDate, habitEndDate
        ).size() > 0;
        
        if (!hasAnyHabitRecord) {
            log.info("전체 습관 기간에 습관 기록이 없어 플레이스홀더 생성 생략: userId={}, habitPeriod=[{} ~ {}]", 
                    userId.getValue(), habitStartDate, habitEndDate);
            return;
        }
        
        // 캘린더 조회 범위와 습관 기간의 교집합 계산
        LocalDate rangeStart = habitStartDate.isAfter(startDate) ? habitStartDate : startDate;
        LocalDate rangeEnd = habitEndDate.isBefore(endDate) ? habitEndDate : endDate;
        
        // 교집합이 없는 경우 처리하지 않음
        if (rangeStart.isAfter(rangeEnd)) {
            return;
        }
        
        // 기존 습관 기록이 있는 날짜 집합 생성
        Set<LocalDate> existingHabitDates = allRecords.stream()
            .filter(record -> record.type() == RecordType.HABIT)
            .map(UnifiedRecordResponse::recordDate)
            .collect(Collectors.toSet());
        
        // 습관 기간 내 각 날짜에 대해 플레이스홀더 생성
        for (LocalDate date = rangeStart; !date.isAfter(rangeEnd); date = date.plusDays(1)) {
            // 이미 습관 기록이 있는 날짜는 스킵
            if (!existingHabitDates.contains(date)) {
                // 플레이스홀더 습관 기록 생성
                UnifiedRecordResponse placeholderRecord = createPlaceholderHabitRecord(user, date);
                allRecords.add(placeholderRecord);
            }
        }
        
        log.info("메인 습관 플레이스홀더 생성 완료: userId={}, habitPeriod=[{} ~ {}], calendarRange=[{} ~ {}]", 
                userId.getValue(), habitStartDate, habitEndDate, startDate, endDate);
    }
    
    /**
     * 전체 습관 기간에 대한 메인 습관 기록 실제 생성
     * 메인 기록 타입이 HABIT로 변경될 때 호출되어 전체 목표 기간에 대한 메인 습관 기록을 실제 DB에 생성
     */
    @CacheEvict(value = "calendar", allEntries = true)
    public void generatePlaceholderMainHabitRecordsForEntirePeriod(UserId userId) {
        // 사용자 정보 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 메인 기록 타입이 HABIT이 아닌 경우 처리하지 않음
        if (user.getMainRecordType() != RecordType.HABIT) {
            log.info("메인 기록 타입이 HABIT이 아니므로 메인 습관 기록 생성 생략: userId={}, mainRecordType={}", 
                    userId.getValue(), user.getMainRecordType());
            return;
        }
        
        // 습관 시작일이 설정되지 않은 경우 처리하지 않음
        if (user.getHabitStartDate() == null) {
            log.warn("습관 시작일이 설정되지 않아 메인 습관 기록 생성 불가: userId={}", userId.getValue());
            return;
        }
        
        // 습관 기간 범위 계산
        LocalDate habitStartDate = user.getHabitStartDate();
        LocalDate habitEndDate = habitStartDate.plusDays(user.getGoalDays() - 1);
        
        // 전체 습관 기간의 기존 습관 기록 조회
        List<HabitRecord> existingHabitRecords = habitRecordRepository.findByUserIdAndRecordDateBetween(
            userId, habitStartDate, habitEndDate);
        
        // 기존 메인 습관 기록이 있는 날짜들을 서브로 변경
        List<HabitRecord> existingMainRecords = existingHabitRecords.stream()
            .filter(HabitRecord::isMainRecord)
            .toList();
        
        for (HabitRecord existingMainRecord : existingMainRecords) {
            HabitRecord updatedRecord = existingMainRecord.updateMainRecordStatus(false);
            habitRecordRepository.save(updatedRecord);
            log.info("기존 메인 습관 기록을 서브로 변경: habitRecordId={}, recordDate={}", 
                    existingMainRecord.getId().getValue(), existingMainRecord.getRecordDate());
        }
        
        // 기존 습관 기록이 있는 날짜 집합 생성
        Set<LocalDate> existingHabitDates = existingHabitRecords.stream()
            .map(HabitRecord::getRecordDate)
            .collect(Collectors.toSet());
        
        int createdCount = 0;
        
        // 전체 습관 기간에 대해 실제 메인 습관 기록을 DB에 생성
        for (LocalDate date = habitStartDate; !date.isAfter(habitEndDate); date = date.plusDays(1)) {
            // 이미 습관 기록이 있는 날짜는 스킵
            if (!existingHabitDates.contains(date)) {
                // 기본 메인 습관 기록 생성
                HabitRecord mainHabitRecord = HabitRecord.create(
                    userId,
                    com.recordmanagement.habitlog.domain.habit.domain.model.HabitType.WATER_DRINKING, // 기본 습관 타입
                    false, // 기본 알림 비활성화
                    null, // 알림 시간 없음
                    "자동 생성된 메인 습관 기록", // 기본 메모
                    date
                ).updateMainRecordStatus(true); // 메인 기록으로 설정
                
                habitRecordRepository.save(mainHabitRecord);
                createdCount++;
                
                log.debug("자동 메인 습관 기록 생성: habitRecordId={}, date={}", 
                        mainHabitRecord.getId().getValue(), date);
            }
        }
        
        log.info("전체 습관 기간 메인 습관 기록 생성 완료: userId={}, habitPeriod=[{} ~ {}], createdCount={}", 
                userId.getValue(), habitStartDate, habitEndDate, createdCount);
    }
    
    /**
     * 플레이스홀더 습관 기록 생성
     */
    private UnifiedRecordResponse createPlaceholderHabitRecord(User user, LocalDate date) {
        return new UnifiedRecordResponse(
            "placeholder-" + user.getId().getValue() + "-" + date.toString(), // 임시 ID
            RecordType.HABIT,
            date,
            null, // recordTime
            null, // createdAt
            null, // updatedAt
            null, // imageUrls
            null, null, // emotion, content (일상 기록 필드)
            null, null, null, null, null, null, // 운동 기록 필드들
            null, // habitType (실제 습관 타입 없음)
            null, // notificationEnabled
            null, // notificationTime
            null, // memo
            false, // isCompleted (미완료 상태)
            true // isMainRecord (메인 기록)
        );
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
    
    /**
     * 하루에 등록할 수 있는 기록 종류가 최대 2가지인지 검증
     * 
     * OCP 적용: Strategy 패턴으로 switch 문 제거
     * - 새로운 기록 타입 추가 시 기존 코드 수정 불필요
     * - 각 기록 타입별 검증 전략이 독립적으로 동작
     */
    public void validateRecordTypeLimit(UserId userId, LocalDate recordDate, RecordType newRecordType) {
        // 현재 등록된 기록 종류 수를 확인 (Strategy 패턴 사용)
        int recordTypeCount = 0;
        
        for (RecordType recordType : validationStrategyFactory.getSupportedTypes()) {
            boolean hasRecord = validationStrategyFactory
                    .getStrategy(recordType)
                    .hasExistingRecord(userId, recordDate);
            if (hasRecord) {
                recordTypeCount++;
            }
        }
        
        // 새로 추가하려는 기록 종류가 기존에 없는지 확인 (Strategy 패턴 사용)
        boolean hasNewType = !validationStrategyFactory
                .getStrategy(newRecordType)
                .hasExistingRecord(userId, recordDate);
        
        if (hasNewType && recordTypeCount >= 2) {
            throw new CustomException(ErrorCode.RECORD_TYPE_LIMIT_EXCEEDED);
        }
    }
    
    /**
     * 목표 진행률 업데이트
     * 사용자의 현재 진행중인 목표에 대해 완료일수를 계산하여 업데이트
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
            
            // 목표 시작일부터 현재까지의 완료일수 계산
            int completedDays = calculateCompletedDays(userId, currentGoal.getRecordType(), 
                currentGoal.getStartDate(), java.time.LocalDate.now());
            
            // 목표 진행률 업데이트
            goalApplicationService.updateGoalProgress(userId, completedDays);
            
            log.debug("목표 진행률 업데이트 완료: userId={}, completedDays={}", 
                userId.getValue(), completedDays);
                
        } catch (Exception e) {
            log.error("목표 진행률 업데이트 중 오류 발생: userId={}, error={}", 
                userId.getValue(), e.getMessage(), e);
            // 목표 진행률 업데이트 실패가 기록 생성/삭제를 실패시키지 않도록 함
        }
    }
    
    /**
     * 특정 기간 동안의 완료일수 계산
     * 하루에 해당 기록 타입의 기록이 하나라도 있으면 완료로 간주
     */
    private int calculateCompletedDays(UserId userId, RecordType recordType, 
                                      java.time.LocalDate startDate, java.time.LocalDate endDate) {
        int completedDays = 0;
        java.time.LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            boolean hasRecord = false;
            
            // 기록 타입에 따라 완료 여부 판단
            switch (recordType) {
                case DAILY -> {
                    // 일상 기록: 해당 날짜에 일상 기록이 있으면 완료
                    int dailyRecordCount = recordRepository.countByUserIdAndRecordDateAndType(
                        userId, currentDate, RecordType.DAILY);
                    hasRecord = dailyRecordCount > 0;
                }
                case EXERCISE -> {
                    // 운동 기록: 해당 날짜에 운동 기록이 있으면 완료
                    var exerciseRecords = exerciseRecordQueryRepository.findByUserIdAndRecordDate(
                        userId, currentDate);
                    hasRecord = !exerciseRecords.isEmpty();
                }
                case HABIT -> {
                    // 습관 기록: 해당 날짜에 완료된 습관 기록이 있으면 완료
                    var habitRecords = habitRecordRepository.findByUserIdAndRecordDate(
                        userId, currentDate);
                    hasRecord = habitRecords.stream().anyMatch(
                        habit -> habit.isCompleted());
                }
            }
            
            if (hasRecord) {
                completedDays++;
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return completedDays;
    }
}