package com.recordmanagement.habitlog.domain.notification.application.strategy;

import com.recordmanagement.habitlog.domain.notification.application.dto.NotificationMessage;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;

/**
 * 알림 메시지 생성 전략 인터페이스
 * 
 * OCP (Open-Closed Principle) 적용:
 * - 새로운 기록 타입 추가 시 기존 코드 수정 없이 확장 가능
 * - Strategy 패턴을 통해 각 기록 타입별 알림 메시지 생성 로직을 분리
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0 (OCP 적용)
 */
public interface NotificationMessageStrategy {

    /**
     * 기록 타입에 맞는 알림 메시지 생성
     * 
     * @return 알림 메시지
     */
    NotificationMessage createDailyRecordReminderMessage();

    /**
     * 지원하는 기록 타입 반환
     * 
     * @return 이 전략이 처리하는 기록 타입
     */
    RecordType getSupportedType();
}