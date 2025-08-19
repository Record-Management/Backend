package com.recordmanagement.habitlog.application.record;

import com.recordmanagement.habitlog.application.record.dto.ScheduleRecordCreateCommand;
import com.recordmanagement.habitlog.application.record.dto.ScheduleRecordResponse;
import com.recordmanagement.habitlog.domain.record.model.ScheduleRecord;
import com.recordmanagement.habitlog.domain.record.model.ScheduleType;
import com.recordmanagement.habitlog.domain.record.repository.ScheduleRecordRepository;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScheduleRecordApplicationService {

    private final ScheduleRecordRepository scheduleRecordRepository;

    /**
     * 일정 기록 생성
     */
    public ScheduleRecordResponse createScheduleRecord(ScheduleRecordCreateCommand command) {
        log.info("일정 기록 생성: userId={}, title={}", command.getUserId(), command.getTitle());
        
        UserId userId = UserId.of(command.getUserId());
        
        ScheduleRecord scheduleRecord = new ScheduleRecord(
                userId,
                command.getTitle(),
                command.getScheduleType(),
                command.getStartDate(),
                command.getEndDate(),
                command.getStartTime(),
                command.getEndTime(),
                command.getMemo()
        );
        
        ScheduleRecord savedRecord = scheduleRecordRepository.save(scheduleRecord);
        log.info("일정 기록 생성됨: recordId={}", savedRecord.getId());
        
        return ScheduleRecordResponse.from(savedRecord);
    }

    /**
     * 일정 기록 수정
     */
    public ScheduleRecordResponse updateScheduleRecord(String scheduleId, String userId, String title, 
                                                     ScheduleType scheduleType, LocalDate startDate, 
                                                     LocalDate endDate, LocalTime startTime, 
                                                     LocalTime endTime, String memo) {
        log.info("일정 기록 수정: scheduleId={}, userId={}", scheduleId, userId);
        
        ScheduleRecord scheduleRecord = scheduleRecordRepository.findByIdAndUserId(scheduleId, UserId.of(userId))
                .orElseThrow(() -> new IllegalArgumentException("일정 기록을 찾을 수 없습니다."));
        
        scheduleRecord.updateSchedule(title, scheduleType, startDate, endDate, startTime, endTime, memo);
        
        ScheduleRecord savedRecord = scheduleRecordRepository.save(scheduleRecord);
        log.info("일정 기록 수정됨: recordId={}", savedRecord.getId());
        
        return ScheduleRecordResponse.from(savedRecord);
    }

    /**
     * 일정 기록 삭제
     */
    public void deleteScheduleRecord(String scheduleId, String userId) {
        log.info("일정 기록 삭제: scheduleId={}, userId={}", scheduleId, userId);
        
        ScheduleRecord scheduleRecord = scheduleRecordRepository.findByIdAndUserId(scheduleId, UserId.of(userId))
                .orElseThrow(() -> new IllegalArgumentException("일정 기록을 찾을 수 없습니다."));
        
        scheduleRecordRepository.delete(scheduleRecord);
        log.info("일정 기록 삭제됨: recordId={}", scheduleRecord.getId());
    }

    /**
     * 특정 날짜의 일정 기록 조회
     */
    @Transactional(readOnly = true)
    public List<ScheduleRecordResponse> getScheduleRecordsByDate(String userId, String date) {
        log.info("특정 날짜 일정 기록 조회: userId={}, date={}", userId, date);
        
        LocalDate targetDate = LocalDate.parse(date);
        UserId userIdObj = UserId.of(userId);
        
        List<ScheduleRecord> scheduleRecords = scheduleRecordRepository.findByUserIdAndDate(userIdObj, targetDate);
        
        return scheduleRecords.stream()
                .map(ScheduleRecordResponse::from)
                .collect(Collectors.toList());
    }
}