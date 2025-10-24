package com.recordmanagement.habitlog.domain.record.application.strategy;

import com.recordmanagement.habitlog.domain.record.domain.repository.RecordRepository;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 일상 기록 검증 전략 구현체
 * 
 * OCP 적용: RecordTypeValidationStrategy 인터페이스 구현
 * - 일상 기록 특화 검증 로직 캡슐화
 * - 다른 기록 타입과 독립적으로 관리
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0 (OCP 적용)
 */
@Component
@RequiredArgsConstructor
public class DailyRecordValidationStrategy implements RecordTypeValidationStrategy {

    private final RecordRepository recordRepository;

    @Override
    public boolean hasExistingRecord(UserId userId, LocalDate recordDate) {
        int count = recordRepository.countByUserIdAndRecordDateAndType(userId, recordDate, RecordType.DAILY);
        return count > 0;
    }

    @Override
    public RecordType getSupportedType() {
        return RecordType.DAILY;
    }
}