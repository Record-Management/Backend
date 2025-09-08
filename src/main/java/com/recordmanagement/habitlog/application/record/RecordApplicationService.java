package com.recordmanagement.habitlog.application.record;

import com.recordmanagement.habitlog.application.record.dto.CalendarRecordResponse;
import com.recordmanagement.habitlog.application.record.dto.CalendarResponse;
import com.recordmanagement.habitlog.application.record.dto.CreateRecordCommand;
import com.recordmanagement.habitlog.application.record.dto.DailyRecordResponse;
import com.recordmanagement.habitlog.application.record.dto.RecordResponse;
import com.recordmanagement.habitlog.application.record.dto.UpdateRecordCommand;
import com.recordmanagement.habitlog.config.exception.CustomException;
import com.recordmanagement.habitlog.config.exception.ErrorCode;
import com.recordmanagement.habitlog.domain.record.model.Record;
import com.recordmanagement.habitlog.domain.record.model.RecordId;
import com.recordmanagement.habitlog.domain.record.repository.RecordRepository;
import com.recordmanagement.habitlog.domain.user.model.RecordType;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecordApplicationService {
    
    private final RecordRepository recordRepository;
    
    public RecordApplicationService(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }
    
    @CacheEvict(value = "calendar", key = "#command.userId().getValue() + '_*'", allEntries = true)
    public RecordResponse createRecord(CreateRecordCommand command) {
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
        
        // 기록 수정
        Record updatedRecord = existingRecord
            .updateType(command.type())
            .updateEmotion(command.emotion())
            .updateContent(command.content())
            .updateImages(command.imageUrls());
            
        // recordTime이 제공되면 업데이트
        if (command.recordTime() != null) {
            updatedRecord = updatedRecord.updateTime(command.recordTime());
        }
        
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
    
    @Cacheable(value = "calendar", key = "#userId + '_' + #year + '_' + #month + '_' + (#types != null ? #types.toString() : 'ALL')")
    @Transactional(readOnly = true)
    public CalendarResponse getCalendar(String userId, int year, int month, List<RecordType> types) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        List<Record> records;
        
        // 타입 필터링이 있으면 필터링된 결과, 없으면 전체 조회
        if (types != null && !types.isEmpty()) {
            records = recordRepository.findByUserIdAndRecordDateBetweenAndTypeIn(
                UserId.of(userId), startDate, endDate, types
            );
        } else {
            records = recordRepository.findByUserIdAndRecordDateBetween(
                UserId.of(userId), startDate, endDate
            );
        }
        
        // 날짜별로 그룹핑
        Map<LocalDate, List<Record>> recordsByDate = records.stream()
            .collect(Collectors.groupingBy(Record::getRecordDate));
        
        // 캘린더 응답 생성
        List<CalendarRecordResponse> dailyRecords = recordsByDate.entrySet()
            .stream()
            .map(entry -> CalendarRecordResponse.of(entry.getKey(), entry.getValue()))
            .sorted((a, b) -> a.date().compareTo(b.date()))
            .toList();
        
        return CalendarResponse.of(yearMonth, dailyRecords);
    }
    
    @Transactional(readOnly = true)
    public DailyRecordResponse getDailyRecords(String userId, LocalDate date) {
        List<Record> records = recordRepository.findByUserIdAndRecordDate(
            UserId.of(userId), date
        );
        
        return DailyRecordResponse.of(date, records);
    }
    
    /**
     * 기록 소유권 검증 (중복 코드 제거)
     */
    private void validateRecordOwnership(Record record, UserId userId) {
        if (!record.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.RECORD_ACCESS_DENIED);
        }
    }
}