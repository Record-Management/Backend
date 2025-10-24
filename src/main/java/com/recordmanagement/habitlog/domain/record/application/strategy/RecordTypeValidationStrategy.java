package com.recordmanagement.habitlog.domain.record.application.strategy;

import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;

import java.time.LocalDate;

/**
 * 기록 타입 검증 전략 인터페이스
 * 
 * OCP (Open-Closed Principle) 적용:
 * - 새로운 기록 타입 추가 시 기존 코드 수정 없이 확장 가능
 * - Strategy 패턴을 통해 각 기록 타입별 검증 로직을 분리
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0 (OCP 적용)
 */
public interface RecordTypeValidationStrategy {

    /**
     * 해당 기록 타입의 기록이 이미 존재하는지 확인
     * 
     * @param userId 사용자 ID
     * @param recordDate 기록 날짜
     * @return 기록이 존재하면 true, 없으면 false
     */
    boolean hasExistingRecord(UserId userId, LocalDate recordDate);

    /**
     * 지원하는 기록 타입 반환
     * 
     * @return 이 전략이 처리하는 기록 타입
     */
    RecordType getSupportedType();
}