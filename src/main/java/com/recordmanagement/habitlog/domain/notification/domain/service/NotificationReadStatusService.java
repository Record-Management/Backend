package com.recordmanagement.habitlog.domain.notification.domain.service;

import com.recordmanagement.habitlog.domain.user.domain.model.UserId;

import java.time.LocalDateTime;

/**
 * 알림 읽음 상태 관리 서비스 인터페이스
 * 
 * ISP(Interface Segregation Principle) 적용:
 * - 알림 읽음 처리 관련 기능만 분리
 * - 클라이언트가 사용하지 않는 메서드에 의존하지 않도록 설계
 * 
 * @author 전우선
 * @since 2025.10.25
 * @version 1.0.0
 */
public interface NotificationReadStatusService {

    /**
     * 알림 센터 마지막 확인 시간 업데이트
     * 
     * @param userId 사용자 ID
     */
    void updateLastCheckedAt(UserId userId);

    /**
     * 사용자의 마지막 확인 시간 조회
     * 
     * @param userId 사용자 ID
     * @return 마지막 확인 시간 (없으면 null)
     */
    LocalDateTime getLastCheckedAt(UserId userId);
}