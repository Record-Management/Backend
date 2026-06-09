package com.recordmanagement.habitlog.domain.record.application.service;

import com.recordmanagement.habitlog.domain.record.application.dto.CalendarRecordResponse;
import com.recordmanagement.habitlog.domain.record.application.dto.CalendarResponse;
import com.recordmanagement.habitlog.domain.record.application.dto.CreateRecordCommand;
import com.recordmanagement.habitlog.domain.record.application.dto.DailyRecordResponse;
import com.recordmanagement.habitlog.domain.record.application.dto.RecordResponse;
import com.recordmanagement.habitlog.domain.record.application.dto.ScheduleDetail;
import com.recordmanagement.habitlog.domain.record.application.dto.ScheduleSummary;
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
import com.recordmanagement.habitlog.domain.goal.domain.model.Goal;
import com.recordmanagement.habitlog.domain.goal.domain.model.GoalStatus;
import com.recordmanagement.habitlog.domain.goal.domain.repository.GoalRepository;
import com.recordmanagement.habitlog.domain.file.infrastructure.service.S3FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
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

    // 비즈니스 상수
    private static final int MAX_DAILY_RECORDS = 2;
    private static final int MAX_RECORD_TYPES_PER_DAY = 2;
    private static final String AUTO_GENERATED_MEMO_PREFIX = "자동 생성된";

    private final RecordRepository recordRepository;
    private final ExerciseRecordQueryRepository exerciseRecordQueryRepository;
    private final ExerciseRecordSecurityRepository exerciseRecordSecurityRepository;
    private final HabitRecordRepository habitRecordRepository;
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final S3FileService s3FileService;
    private final MainRecordDeterminationService mainRecordDeterminationService;
    private final RecordTypeValidationStrategyFactory validationStrategyFactory;
    private final ApplicationContext applicationContext;
    private final com.recordmanagement.habitlog.domain.schedule.domain.repository.ScheduleRecordRepository scheduleRecordRepository;
    
    public RecordApplicationService(RecordRepository recordRepository,
                                   ExerciseRecordQueryRepository exerciseRecordQueryRepository,
                                   ExerciseRecordSecurityRepository exerciseRecordSecurityRepository,
                                   HabitRecordRepository habitRecordRepository,
                                   UserRepository userRepository,
                                   GoalRepository goalRepository,
                                   S3FileService s3FileService,
                                   MainRecordDeterminationService mainRecordDeterminationService,
                                   RecordTypeValidationStrategyFactory validationStrategyFactory,
                                   ApplicationContext applicationContext,
                                   com.recordmanagement.habitlog.domain.schedule.domain.repository.ScheduleRecordRepository scheduleRecordRepository) {
        this.recordRepository = recordRepository;
        this.exerciseRecordQueryRepository = exerciseRecordQueryRepository;
        this.exerciseRecordSecurityRepository = exerciseRecordSecurityRepository;
        this.habitRecordRepository = habitRecordRepository;
        this.userRepository = userRepository;
        this.goalRepository = goalRepository;
        this.s3FileService = s3FileService;
        this.mainRecordDeterminationService = mainRecordDeterminationService;
        this.validationStrategyFactory = validationStrategyFactory;
        this.applicationContext = applicationContext;
        this.scheduleRecordRepository = scheduleRecordRepository;
    }
    
    @CacheEvict(value = "calendar", allEntries = true)
    public RecordResponse createRecord(CreateRecordCommand command) {
        // 하루 최대 기록 제한 검증 (전체 타입 합쳐서)
        int totalRecordCount = getTotalRecordCount(command.userId(), command.recordDate());

        if (totalRecordCount >= MAX_DAILY_RECORDS) {
            throw new CustomException(ErrorCode.DAILY_RECORD_LIMIT_EXCEEDED);
        }

        // 전체 기록 종류 최대 2가지 제한 검증
        validateRecordTypeLimit(command.userId(), command.recordDate(), RecordType.DAILY);

        // 기존 기록 개수 조회 (메인 기록 결정에 필요)
        int existingRecordCount = recordRepository.countByUserIdAndRecordDateAndType(
            command.userId(),
            command.recordDate(),
            RecordType.DAILY
        );
        
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
        
        // 모든 타입의 기록을 통합하여 조회 (일정 제외)
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
            // 습관 기록은 습관 목표 기간 내에서만 조회
            List<HabitRecord> habitRecords = getHabitRecordsInGoalPeriod(userIdObj, startDate, endDate);

            // 습관 타입 사용자의 특별한 캘린더 표시 로직 적용
            List<UnifiedRecordResponse> habitResponses = applyHabitTypeCalendarLogic(
                user, habitRecords, startDate, endDate);
            allRecords.addAll(habitResponses);
        }

        // 4. 일정 기록 조회 (일반 일정 + 반복 일정)
        Map<LocalDate, List<com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord>> schedulesByDate = new HashMap<>();

        // 일반 일정 조회 (startDate ~ endDate와 겹치는 일정)
        List<com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord> scheduleRecords =
            new ArrayList<>(scheduleRecordRepository.findByUserIdAndDateRange(userIdObj, startDate, endDate));

        // 반복 일정 조회 (DB에서 사용자 필터링으로 성능 최적화)
        List<com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord> repeatSchedules =
            scheduleRecordRepository.findRepeatableSchedulesByUserId(userIdObj).stream()
                .filter(s -> {
                    // 반복 종료일이 캘린더 시작일 이전이면 제외
                    LocalDate repeatEnd = s.getRepeatEndsOn() != null ? s.getRepeatEndsOn() : endDate;
                    return !repeatEnd.isBefore(startDate);
                })
                .filter(s -> {
                    // 일정 시작일이 캘린더 종료일 이후면 제외
                    return !s.getStartDate().isAfter(endDate);
                })
                .toList();

        // 중복 제거하며 반복 일정 추가
        Set<String> existingIds = scheduleRecords.stream()
            .map(s -> s.getId().value())
            .collect(Collectors.toSet());

        for (com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord repeatSchedule : repeatSchedules) {
            if (!existingIds.contains(repeatSchedule.getId().value())) {
                scheduleRecords.add(repeatSchedule);
            }
        }

        // 각 일정을 반복 타입에 따라 날짜별로 그룹핑
        for (com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord schedule : scheduleRecords) {
            // 반복 타입에 따라 표시할 날짜 계산
            List<LocalDate> scheduleDates = calculateScheduleDates(schedule, startDate, endDate);

            // 각 날짜에 일정 추가
            for (LocalDate date : scheduleDates) {
                schedulesByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(schedule);
            }
        }
        
        // 날짜별로 그룹핑 (UnifiedRecordResponse 기준)
        Map<LocalDate, List<UnifiedRecordResponse>> recordsByDate = allRecords.stream()
            .collect(Collectors.groupingBy(UnifiedRecordResponse::recordDate));
        
        LocalDate today = LocalDate.now();
        
        // 전체 날짜 범위에 대해 캘린더 응답 생성 (요구사항에 맞는 표시 로직 적용)
        List<CalendarRecordResponse> calendarRecords = new ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // 해당 날짜의 실제 기록들
            List<UnifiedRecordResponse> dateRecords = recordsByDate.getOrDefault(date, new ArrayList<>());

            // 해당 날짜의 메인 기록 타입 결정
            RecordType mainRecordTypeForDate = determineMainRecordTypeForDate(user, date);

            // 요구사항에 맞는 캘린더 레코드 생성
            List<CalendarRecordResponse.RecordSummary> summaries =
                createCalendarSummariesWithDisplayLogic(user, mainRecordTypeForDate, date, dateRecords, today);

            // 해당 날짜의 일정 요약 정보 생성
            ScheduleSummary scheduleSummary = createScheduleSummary(schedulesByDate.get(date));

            calendarRecords.add(new CalendarRecordResponse(date, mainRecordTypeForDate, summaries, scheduleSummary));
        }
        
        calendarRecords.sort((a, b) -> a.date().compareTo(b.date()));
        
        return CalendarResponse.of(yearMonth, calendarRecords);
    }
    
    /**
     * 일정 요약 정보 생성
     * - title: 대표 일정명 (첫 번째 일정)
     * - extraScheduleCount: 추가 일정 개수 (표시되지 않은 일정 수, 1개면 null)
     * - color: 대표 일정 색상 (첫 번째 일정)
     */
    private ScheduleSummary createScheduleSummary(List<com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return null;
        }

        // 첫 번째 일정을 대표로 사용
        com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord firstSchedule = schedules.get(0);

        // 추가 일정 개수 계산 (1개면 null, 2개 이상이면 size - 1)
        Integer extraScheduleCount = schedules.size() > 1 ? schedules.size() - 1 : null;

        return ScheduleSummary.builder()
                .title(firstSchedule.getTitle())
                .extraScheduleCount(extraScheduleCount)
                .color(firstSchedule.getColor())
                .build();
    }

    /**
     * 요구사항에 맞는 캘린더 표시 로직 적용
     *
     * 과거: 하루/운동 미작성 시 빈 배열(프론트에서 회색 처리), 습관 미완료 시 회색 아이콘
     * 현재: 하루/운동 미작성 시 빈칸, 습관 미작성 시 빈칸/작성 시 회색/완료 시 색상
     * 미래: 모든 기록 빈칸 (이미 상위에서 필터링됨)
     */
    private List<CalendarRecordResponse.RecordSummary> createCalendarSummariesWithDisplayLogic(
            User user, RecordType mainRecordTypeForDate, LocalDate date, List<UnifiedRecordResponse> dateRecords, LocalDate today) {
        
        List<CalendarRecordResponse.RecordSummary> summaries = new ArrayList<>();
        
        // 실제 기록들을 타입별로 분류
        Map<RecordType, List<UnifiedRecordResponse>> recordsByType = dateRecords.stream()
            .collect(Collectors.groupingBy(UnifiedRecordResponse::type));
        
        if (date.isBefore(today)) {
            // === 과거 날짜 처리 ===
            summaries.addAll(createPastDateSummaries(recordsByType, date));
            
        } else if (date.equals(today)) {
            // === 현재 날짜 처리 ===
            summaries.addAll(createCurrentDateSummaries(user, mainRecordTypeForDate, recordsByType, date));
            
        }
        // 미래 날짜는 빈 리스트 반환 (빈칸 처리)
        
        return summaries;
    }
    
    /**
     * 과거 날짜 캘린더 표시 로직
     * - 하루/운동: 작성 시 색상, 미작성 시 빈 배열 (프론트에서 회색 처리)
     * - 습관: 완료 시 색상, 미완료 시 회색, 미작성 시 빈 배열 (프론트에서 회색 처리)
     */
    private List<CalendarRecordResponse.RecordSummary> createPastDateSummaries(
            Map<RecordType, List<UnifiedRecordResponse>> recordsByType, LocalDate date) {
        
        List<CalendarRecordResponse.RecordSummary> summaries = new ArrayList<>();
        
        // 하루 기록 처리
        List<UnifiedRecordResponse> dailyRecords = recordsByType.getOrDefault(RecordType.DAILY, new ArrayList<>());
        if (!dailyRecords.isEmpty()) {
            // 작성된 경우: 색상 (isCompleted는 항상 true)
            summaries.addAll(dailyRecords.stream()
                .map(CalendarRecordResponse.RecordSummary::from)
                .toList());
        }
        // 미작성인 경우: 아무것도 추가하지 않음 (빈 배열로 프론트에서 처리)
        
        // 운동 기록 처리
        List<UnifiedRecordResponse> exerciseRecords = recordsByType.getOrDefault(RecordType.EXERCISE, new ArrayList<>());
        if (!exerciseRecords.isEmpty()) {
            // 작성된 경우: 색상 (isCompleted는 항상 true)
            summaries.addAll(exerciseRecords.stream()
                .map(CalendarRecordResponse.RecordSummary::from)
                .toList());
        }
        // 미작성인 경우: 아무것도 추가하지 않음 (빈 배열로 프론트에서 처리)
        
        // 습관 기록 처리
        List<UnifiedRecordResponse> habitRecords = recordsByType.getOrDefault(RecordType.HABIT, new ArrayList<>());
        if (!habitRecords.isEmpty()) {
            // 실제 기록이 있는 경우: 완료 여부에 따라 색상/회색
            summaries.addAll(habitRecords.stream()
                .map(CalendarRecordResponse.RecordSummary::from)
                .toList());
        }
        // 미작성인 경우: 아무것도 추가하지 않음 (빈 배열로 프론트에서 처리)

        // 일정 기록 처리 (항상 표시)
        List<UnifiedRecordResponse> scheduleRecords = recordsByType.getOrDefault(RecordType.SCHEDULE, new ArrayList<>());
        if (!scheduleRecords.isEmpty()) {
            summaries.addAll(scheduleRecords.stream()
                .map(CalendarRecordResponse.RecordSummary::from)
                .toList());
        }

        return summaries;
    }
    
    /**
     * 현재 날짜 캘린더 표시 로직
     * - 하루/운동: 작성 시 색상, 미작성 시 빈칸
     * - 습관: 완료 시 색상, 작성 시 회색, 미작성 시 빈칸
     */
    private List<CalendarRecordResponse.RecordSummary> createCurrentDateSummaries(
            User user, RecordType mainRecordTypeForDate, Map<RecordType, List<UnifiedRecordResponse>> recordsByType, LocalDate date) {
        
        List<CalendarRecordResponse.RecordSummary> summaries = new ArrayList<>();
        
        // 하루 기록 처리: 작성된 것만 표시 (미작성 시 빈칸)
        List<UnifiedRecordResponse> dailyRecords = recordsByType.getOrDefault(RecordType.DAILY, new ArrayList<>());
        summaries.addAll(dailyRecords.stream()
            .map(CalendarRecordResponse.RecordSummary::from)
            .toList());
        
        // 운동 기록 처리: 작성된 것만 표시 (미작성 시 빈칸)  
        List<UnifiedRecordResponse> exerciseRecords = recordsByType.getOrDefault(RecordType.EXERCISE, new ArrayList<>());
        summaries.addAll(exerciseRecords.stream()
            .map(CalendarRecordResponse.RecordSummary::from)
            .toList());
        
        // 습관 기록 처리: 작성된 것만 표시 (미작성 시 빈칸, 자동 생성 기록도 실제 작성된 것으로 간주)
        List<UnifiedRecordResponse> habitRecords = recordsByType.getOrDefault(RecordType.HABIT, new ArrayList<>());

        // 습관 타입 사용자의 자동 생성 기록 특별 처리
        if (mainRecordTypeForDate == RecordType.HABIT) {
            // 메인 습관: 미작성시 숨김, 작성시 isCompleted=false로 표시, 완료시 isCompleted=true로 표시
            summaries.addAll(habitRecords.stream()
                .filter(record -> !isAutoGeneratedPlaceholder(record))
                .map(CalendarRecordResponse.RecordSummary::from)
                .toList());
        } else {
            // 다른 타입 사용자는 모든 습관 기록 표시
            summaries.addAll(habitRecords.stream()
                .map(CalendarRecordResponse.RecordSummary::from)
                .toList());
        }

        // 일정 기록 처리 (항상 표시)
        List<UnifiedRecordResponse> scheduleRecords = recordsByType.getOrDefault(RecordType.SCHEDULE, new ArrayList<>());
        summaries.addAll(scheduleRecords.stream()
            .map(CalendarRecordResponse.RecordSummary::from)
            .toList());

        return summaries;
    }
    
    /**
     * 자동 생성된 플레이스홀더 기록인지 확인
     * 사용자가 수정한 기록(완료 또는 작성)은 모두 표시
     */
    private boolean isAutoGeneratedPlaceholder(UnifiedRecordResponse record) {
        // 완료된 기록은 사용자가 실제로 수행한 것이므로 항상 표시
        if (record.isCompleted() != null && record.isCompleted()) {
            return false;
        }
        
        // 자동 생성된 기록이지만 사용자가 수정한 경우 (메모가 변경됨) 표시
        if (record.memo() != null && record.memo().contains(AUTO_GENERATED_MEMO_PREFIX)) {
            return true; // 자동 생성된 그대로 남아있는 경우만 숨김
        }
        
        // 자동 생성되지 않은 기록이거나 사용자가 수정한 기록은 항상 표시
        return false;
    }
    
    
    /**
     * 특정 날짜의 메인 기록 타입을 결정합니다.
     * 목표 이력을 조회하여 해당 날짜가 속한 목표의 실제 메인 기록 타입을 반환
     */
    private RecordType determineMainRecordTypeForDate(User user, LocalDate date) {
        // 사용자의 목표 이력을 조회 (최신순)
        List<Goal> goals = goalRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        
        // 해당 날짜가 속한 목표 찾기
        for (Goal goal : goals) {
            if (!date.isBefore(goal.getStartDate()) && !date.isAfter(goal.getEndDate())) {
                return goal.getRecordType();
            }
        }
        
        // 해당 날짜가 어떤 목표 기간에도 속하지 않는 경우 null 반환
        // (목표가 설정되지 않은 기간은 메인 기록 타입이 없음)
        return null;
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
        
        // 3. 습관 기록 조회 (습관 목표 기간 내만)
        List<HabitRecord> habitRecords = getHabitRecordsInGoalPeriod(userIdObj, date, date);
        allRecords.addAll(habitRecords.stream()
            .map(UnifiedRecordResponse::fromHabitRecord)
            .toList());

        // 4. 일정 기록 조회 (일반 일정 + 반복 일정)
        List<com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord> scheduleRecords =
            new ArrayList<>(scheduleRecordRepository.findByUserIdAndDateRange(userIdObj, date, date));

        // 반복 일정 조회 (DB에서 사용자 필터링으로 성능 최적화)
        List<com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord> repeatSchedules =
            scheduleRecordRepository.findRepeatableSchedulesByUserId(userIdObj).stream()
                .filter(s -> {
                    // 이 반복 일정이 해당 날짜에 표시되어야 하는지 확인
                    List<LocalDate> scheduleDates = calculateScheduleDates(s, date, date);
                    return !scheduleDates.isEmpty();
                })
                .toList();

        // 중복 제거하며 반복 일정 추가
        Set<String> existingIds = scheduleRecords.stream()
            .map(s -> s.getId().value())
            .collect(Collectors.toSet());

        for (com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord repeatSchedule : repeatSchedules) {
            if (!existingIds.contains(repeatSchedule.getId().value())) {
                scheduleRecords.add(repeatSchedule);
            }
        }

        List<ScheduleDetail> schedules = scheduleRecords.stream()
            .map(ScheduleDetail::from)
            .toList();

        // Pre-signed URL 재생성
        List<UnifiedRecordResponse> recordsWithUpdatedUrls = allRecords.stream()
            .map(this::updateImageUrls)
            .toList();

        return DailyRecordResponse.of(date, recordsWithUpdatedUrls, schedules);
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
            true, // isMainRecord (메인 기록)
            null, null, null, null, null, null, null // SCHEDULE 필드 없음
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
     * 하루 전체 기록 개수 조회 (DAILY + EXERCISE + HABIT 합계)
     */
    private int getTotalRecordCount(UserId userId, LocalDate recordDate) {
        int dailyCount = recordRepository.countByUserIdAndRecordDateAndType(userId, recordDate, RecordType.DAILY);
        int exerciseCount = exerciseRecordQueryRepository.countByUserIdAndRecordDate(userId, recordDate);
        int habitCount = habitRecordRepository.countByUserIdAndRecordDate(userId, recordDate);

        return dailyCount + exerciseCount + habitCount;
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

        if (hasNewType && recordTypeCount >= MAX_RECORD_TYPES_PER_DAY) {
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
        // 성능 최적화: N번 쿼리 대신 1번 쿼리로 기록이 있는 날짜 수 조회
        return switch (recordType) {
            case DAILY -> recordRepository.countDistinctRecordDatesByUserIdAndDateRangeAndType(
                userId, startDate, endDate, RecordType.DAILY);
            case EXERCISE -> exerciseRecordQueryRepository.countDistinctRecordDatesByUserIdAndDateRange(
                userId, startDate, endDate);
            case HABIT -> habitRecordRepository.countCompletedHabitsByUserIdAndDateRange(
                userId, startDate, endDate);
            default -> 0;
        };
    }
    
    /**
     * 습관 목표 기간 내에 있는 습관 기록만 조회 (오늘까지만)
     * - 목표 기간 이전의 과거 데이터는 제외하여 메인 기록 아이콘 표시 오류 방지
     * - 미래 날짜 기록은 제외하여 프론트엔드에 오늘까지만 전달
     */
    private List<HabitRecord> getHabitRecordsInGoalPeriod(UserId userId, LocalDate startDate, LocalDate endDate) {
        // 사용자 정보 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 습관 시작일이 설정되지 않은 경우, 모든 습관 기록 조회
        if (user.getHabitStartDate() == null) {
            return habitRecordRepository.findByUserIdAndRecordDateBetween(userId, startDate, endDate);
        }
        
        // 습관 목표 기간 계산
        LocalDate habitStartDate = user.getHabitStartDate();
        LocalDate habitEndDate = habitStartDate.plusDays(user.getGoalDays() - 1);
        LocalDate today = LocalDate.now();
        
        // 목표 기간을 오늘까지로 제한 (미래 날짜 제외)
        LocalDate effectiveHabitEndDate = habitEndDate.isBefore(today) ? habitEndDate : today;
        
        // 조회 범위와 습관 기간의 교집합 계산
        LocalDate rangeStart = habitStartDate.isAfter(startDate) ? habitStartDate : startDate;
        LocalDate rangeEnd = effectiveHabitEndDate.isBefore(endDate) ? effectiveHabitEndDate : endDate;
        
        // 교집합이 없는 경우 빈 리스트 반환
        if (rangeStart.isAfter(rangeEnd)) {
            log.debug("습관 목표 기간과 조회 범위의 교집합이 없음: userId={}, habitPeriod=[{} ~ {}], effectiveRange=[{} ~ {}], queryRange=[{} ~ {}]", 
                    userId.getValue(), habitStartDate, effectiveHabitEndDate, startDate, endDate);
            return new ArrayList<>();
        }
        
        // 습관 목표 기간 내의 습관 기록만 조회
        List<HabitRecord> habitRecords = habitRecordRepository.findByUserIdAndRecordDateBetween(
            userId, rangeStart, rangeEnd);
        
        log.debug("습관 목표 기간 내 습관 기록 조회 완료: userId={}, 조회범위=[{} ~ {}], 기록수={}", 
                userId.getValue(), rangeStart, rangeEnd, habitRecords.size());
        
        return habitRecords;
    }
    
    /**
     * 습관 타입 사용자의 특별한 캘린더 표시 로직 적용
     * 
     * ### 습관 타입 사용자 특별 규칙:
     * 1. **모든 날짜**: 작성된 습관 기록 표시 (과거/현재 모두)
     * 2. **미래 날짜**: 표시 안함 (생성 자체 안함)
     * 
     * ### 다른 타입 사용자:
     * - 모든 습관 기록을 서브 기록으로 표시 (기존 로직 유지)
     */
    private List<UnifiedRecordResponse> applyHabitTypeCalendarLogic(User user, List<HabitRecord> habitRecords, 
                                                                  LocalDate startDate, LocalDate endDate) {
        // 습관 타입 사용자가 아닌 경우 기존 로직 유지
        if (user.getMainRecordType() != RecordType.HABIT) {
            return habitRecords.stream()
                .map(UnifiedRecordResponse::fromHabitRecord)
                .toList();
        }
        
        // 습관 타입 사용자의 경우 모든 작성된 습관 기록을 표시
        List<UnifiedRecordResponse> result = habitRecords.stream()
            .map(UnifiedRecordResponse::fromHabitRecord)
            .toList();
        
        log.debug("습관 타입 사용자의 습관 기록 표시: userId={}, 표시된 기록 수={}",
                user.getId().getValue(), result.size());

        return result;
    }

    /**
     * 기록/일정 생성 제한 조회
     *
     * @param userId 사용자 ID
     * @param date 조회할 날짜 (기록은 recordDate 기준, 일정은 createdAt 기준)
     * @return 생성 제한 정보 (canCreateRecord, canCreateSchedule)
     */
    @Transactional(readOnly = true)
    public com.recordmanagement.habitlog.domain.record.application.dto.CreationLimitsResponse getCreationLimits(
            String userId, LocalDate date) {
        log.info("생성 제한 조회: userId={}, date={}", userId, date);

        UserId userIdObj = UserId.of(userId);

        // 기록 생성 가능 여부 (recordDate 기준 DAILY+EXERCISE+HABIT 합계 < 2)
        int totalRecordCount = getTotalRecordCount(userIdObj, date);
        boolean canCreateRecord = totalRecordCount < 2;

        // 일정 생성 가능 여부 (createdAt 기준 일정 개수 < 2)
        int scheduleCount = scheduleRecordRepository.countByUserIdAndCreatedAtToday(userIdObj, date);
        boolean canCreateSchedule = scheduleCount < 2;

        log.info("생성 제한 조회 결과: canCreateRecord={}, canCreateSchedule={}",
                canCreateRecord, canCreateSchedule);

        return com.recordmanagement.habitlog.domain.record.application.dto.CreationLimitsResponse.builder()
                .canCreateRecord(canCreateRecord)
                .canCreateSchedule(canCreateSchedule)
                .build();
    }

    /**
     * 일정의 반복 타입에 따라 캘린더에 표시할 날짜 계산
     *
     * @param schedule 일정
     * @param calendarStart 캘린더 조회 시작일
     * @param calendarEnd 캘린더 조회 종료일
     * @return 표시할 날짜 리스트
     */
    private List<LocalDate> calculateScheduleDates(
            com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleRecord schedule,
            LocalDate calendarStart,
            LocalDate calendarEnd) {

        List<LocalDate> dates = new ArrayList<>();
        LocalDate scheduleStart = schedule.getStartDate();
        LocalDate scheduleEnd = schedule.getEndDate();
        com.recordmanagement.habitlog.domain.schedule.domain.model.RepeatType repeatType = schedule.getRepeatType();
        LocalDate repeatEndsOn = schedule.getRepeatEndsOn();

        // 반복 종료일 결정 (설정된 경우 그 값 사용, 아니면 캘린더 끝)
        LocalDate effectiveRepeatEnd = repeatEndsOn != null ? repeatEndsOn : calendarEnd;

        switch (repeatType) {
            case NONE -> {
                // 반복 없음: startDate ~ endDate 범위만 표시
                LocalDate displayStart = scheduleStart.isBefore(calendarStart) ? calendarStart : scheduleStart;
                LocalDate displayEnd = scheduleEnd.isAfter(calendarEnd) ? calendarEnd : scheduleEnd;

                for (LocalDate date = displayStart; !date.isAfter(displayEnd); date = date.plusDays(1)) {
                    dates.add(date);
                }
            }
            case DAY -> {
                // 매일 반복: startDate부터 매일, repeatEndsOn까지
                LocalDate displayStart = scheduleStart.isBefore(calendarStart) ? calendarStart : scheduleStart;
                LocalDate displayEnd = effectiveRepeatEnd.isAfter(calendarEnd) ? calendarEnd : effectiveRepeatEnd;

                // startDate ~ endDate 범위가 1일 이상인 경우 각 날짜의 범위를 반복
                long daysInSchedule = java.time.temporal.ChronoUnit.DAYS.between(scheduleStart, scheduleEnd) + 1;

                for (LocalDate repeatDate = displayStart; !repeatDate.isAfter(displayEnd); repeatDate = repeatDate.plusDays(1)) {
                    // 일정의 각 날짜 범위 추가 (예: 3일 일정이면 각 반복마다 3일씩)
                    for (int i = 0; i < daysInSchedule && !repeatDate.plusDays(i).isAfter(displayEnd); i++) {
                        LocalDate date = repeatDate.plusDays(i);
                        if (!date.isBefore(calendarStart) && !date.isAfter(calendarEnd)) {
                            dates.add(date);
                        }
                    }
                }
            }
            case WEEK -> {
                // 매주 반복: startDate부터 매주 같은 요일, repeatEndsOn까지
                LocalDate repeatDate = scheduleStart;
                long daysInSchedule = java.time.temporal.ChronoUnit.DAYS.between(scheduleStart, scheduleEnd) + 1;

                while (!repeatDate.isAfter(effectiveRepeatEnd)) {
                    // 일정의 각 날짜 범위 추가
                    for (int i = 0; i < daysInSchedule; i++) {
                        LocalDate date = repeatDate.plusDays(i);
                        if (!date.isBefore(calendarStart) && !date.isAfter(calendarEnd) && !date.isAfter(effectiveRepeatEnd)) {
                            dates.add(date);
                        }
                    }
                    repeatDate = repeatDate.plusWeeks(1); // 1주 후
                }
            }
            case MONTH -> {
                // 매월 반복: startDate부터 매월 같은 날, repeatEndsOn까지
                LocalDate repeatDate = scheduleStart;
                long daysInSchedule = java.time.temporal.ChronoUnit.DAYS.between(scheduleStart, scheduleEnd) + 1;

                while (!repeatDate.isAfter(effectiveRepeatEnd)) {
                    // 일정의 각 날짜 범위 추가
                    for (int i = 0; i < daysInSchedule; i++) {
                        LocalDate date = repeatDate.plusDays(i);
                        if (!date.isBefore(calendarStart) && !date.isAfter(calendarEnd) && !date.isAfter(effectiveRepeatEnd)) {
                            dates.add(date);
                        }
                    }

                    // 다음 달 같은 날로 이동 (31일이 없는 달은 스킵)
                    try {
                        repeatDate = repeatDate.plusMonths(1);
                    } catch (Exception e) {
                        // 날짜가 유효하지 않으면 (예: 1월 31일 -> 2월 31일) 해당 월은 스킵
                        break;
                    }
                }
            }
            case YEAR -> {
                // 매년 반복: startDate부터 매년 같은 날, repeatEndsOn까지
                LocalDate repeatDate = scheduleStart;
                long daysInSchedule = java.time.temporal.ChronoUnit.DAYS.between(scheduleStart, scheduleEnd) + 1;

                while (!repeatDate.isAfter(effectiveRepeatEnd)) {
                    // 일정의 각 날짜 범위 추가
                    for (int i = 0; i < daysInSchedule; i++) {
                        LocalDate date = repeatDate.plusDays(i);
                        if (!date.isBefore(calendarStart) && !date.isAfter(calendarEnd) && !date.isAfter(effectiveRepeatEnd)) {
                            dates.add(date);
                        }
                    }

                    // 다음 해 같은 날로 이동 (2월 29일이 평년에는 없으므로 스킵)
                    try {
                        repeatDate = repeatDate.plusYears(1);
                    } catch (Exception e) {
                        // 날짜가 유효하지 않으면 (예: 2월 29일 윤년 -> 평년) 해당 년도는 스킵
                        break;
                    }
                }
            }
        }

        return dates;
    }

}