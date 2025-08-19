package com.recordmanagement.habitlog.application.record;

import com.recordmanagement.habitlog.application.record.dto.HabitRecordCreateCommand;
import com.recordmanagement.habitlog.application.record.dto.HabitRecordResponse;
import com.recordmanagement.habitlog.domain.record.model.HabitType;
import com.recordmanagement.habitlog.domain.record.model.HabitRecord;
import com.recordmanagement.habitlog.domain.record.repository.HabitRecordRepository;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HabitRecordApplicationService {

    private final HabitRecordRepository habitRecordRepository;

    /**
     * 습관 기록 생성 또는 수정
     */
    public HabitRecordResponse createOrUpdateHabitRecord(HabitRecordCreateCommand command) {
        log.info("습관 기록 생성/수정: userId={}, date={}, habitType={}", 
                command.getUserId(), command.getRecordDate(), command.getHabitType());
        
        UserId userId = UserId.of(command.getUserId());
        
        // 기존 기록이 있는지 확인
        Optional<HabitRecord> existingRecord = habitRecordRepository.findByUserIdAndRecordDateAndHabitType(
                userId, command.getRecordDate(), command.getHabitType());
        
        HabitRecord habitRecord;
        
        if (existingRecord.isPresent()) {
            // 기존 기록 수정
            habitRecord = existingRecord.get();
            habitRecord.updateRecord(command.isCompleted(), command.getMemo());
            log.info("습관 기록 수정됨: recordId={}", habitRecord.getId());
        } else {
            // 새 기록 생성
            habitRecord = new HabitRecord(
                    userId,
                    command.getRecordDate(),
                    command.getHabitType(),
                    command.isCompleted(),
                    command.getMemo()
            );
            log.info("새 습관 기록 생성됨: recordId={}", habitRecord.getId());
        }
        
        HabitRecord savedRecord = habitRecordRepository.save(habitRecord);
        
        return HabitRecordResponse.from(savedRecord);
    }

    /**
     * 습관 기록 삭제
     */
    public void deleteHabitRecord(String userId, LocalDate recordDate, String habitType) {
        log.info("습관 기록 삭제: userId={}, date={}, habitType={}", userId, recordDate, habitType);
        
        UserId userIdObj = UserId.of(userId);
        HabitType habitTypeEnum = HabitType.valueOf(habitType);
        
        Optional<HabitRecord> habitRecord = habitRecordRepository.findByUserIdAndRecordDateAndHabitType(
                userIdObj, recordDate, habitTypeEnum);
        
        if (habitRecord.isPresent()) {
            habitRecordRepository.delete(habitRecord.get());
            log.info("습관 기록 삭제됨: recordId={}", habitRecord.get().getId());
        } else {
            log.warn("삭제할 습관 기록이 없습니다: userId={}, date={}, habitType={}", 
                    userId, recordDate, habitType);
        }
    }

    /**
     * 특정 날짜의 습관 기록 조회
     */
    @Transactional(readOnly = true)
    public List<HabitRecordResponse> getHabitRecordsByDate(String userId, LocalDate recordDate) {
        log.info("특정 날짜 습관 기록 조회: userId={}, date={}", userId, recordDate);
        
        UserId userIdObj = UserId.of(userId);
        
        List<HabitRecord> habitRecords = habitRecordRepository.findByUserIdAndRecordDate(userIdObj, recordDate);
        
        return habitRecords.stream()
                .map(HabitRecordResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 습관 완료 상태 토글
     */
    public HabitRecordResponse toggleHabitCompletion(String userId, LocalDate recordDate, String habitType) {
        log.info("습관 완료 상태 토글: userId={}, date={}, habitType={}", userId, recordDate, habitType);
        
        UserId userIdObj = UserId.of(userId);
        HabitType habitTypeEnum = HabitType.valueOf(habitType);
        
        Optional<HabitRecord> existingRecord = habitRecordRepository.findByUserIdAndRecordDateAndHabitType(
                userIdObj, recordDate, habitTypeEnum);
        
        HabitRecord habitRecord;
        
        if (existingRecord.isPresent()) {
            // 기존 기록의 완료 상태 토글
            habitRecord = existingRecord.get();
            habitRecord.toggleCompletion();
            log.info("습관 완료 상태 토글됨: recordId={}, completed={}", 
                    habitRecord.getId(), habitRecord.isCompleted());
        } else {
            // 새 기록 생성 (완료 상태로)
            habitRecord = new HabitRecord(userIdObj, recordDate, habitTypeEnum, true, null);
            log.info("새 습관 기록 생성됨 (완료 상태): recordId={}", habitRecord.getId());
        }
        
        HabitRecord savedRecord = habitRecordRepository.save(habitRecord);
        
        return HabitRecordResponse.from(savedRecord);
    }
}