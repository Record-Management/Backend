package com.recordmanagement.habitlog.application.calendar;

import com.recordmanagement.habitlog.application.calendar.dto.DailyRecordsResponse;
import com.recordmanagement.habitlog.domain.record.repository.DailyRecordRepository;
import com.recordmanagement.habitlog.domain.record.repository.ScheduleRecordRepository;
import com.recordmanagement.habitlog.domain.record.repository.HabitRecordRepository;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CalendarApplicationService {

    private final DailyRecordRepository dailyRecordRepository;
    private final ScheduleRecordRepository scheduleRecordRepository;
    private final HabitRecordRepository habitRecordRepository;

    /**
     * 특정 날짜의 모든 기록 조회 (운동 기록 제외)
     */
    public DailyRecordsResponse getDailyRecords(String userId, LocalDate date) {
        log.info("특정 날짜 기록 조회: userId={}, date={}", userId, date);
        
        UserId userIdObj = UserId.of(userId);
        
        // 각 기록 타입별로 조회
        var dailyRecord = dailyRecordRepository.findByUserIdAndRecordDate(userIdObj, date);
        var scheduleRecords = scheduleRecordRepository.findByUserIdAndDate(userIdObj, date);
        var habitRecords = habitRecordRepository.findByUserIdAndRecordDate(userIdObj, date);
        
        return DailyRecordsResponse.builder()
                .date(date)
                .dailyRecord(dailyRecord.orElse(null))
                .scheduleRecords(scheduleRecords)
                .habitRecords(habitRecords)
                .build();
    }
}