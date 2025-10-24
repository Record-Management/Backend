package com.recordmanagement.habitlog.domain.notification.application.service;

import com.recordmanagement.habitlog.domain.notification.application.dto.NotificationHistoryResponse;
import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationHistory;
import com.recordmanagement.habitlog.domain.notification.domain.repository.NotificationHistoryRepository;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 히스토리 애플리케이션 서비스
 *
 * - 알림 히스토리 관련 비즈니스 로직 처리
 * - 트랜잭션 관리 및 도메인 서비스 조율
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationHistoryApplicationService {

    private final NotificationHistoryRepository notificationHistoryRepository;

    /**
     * 사용자의 알림 히스토리 페이징 조회
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 알림 히스토리 페이지
     */
    @Transactional(readOnly = true)
    public Page<NotificationHistoryResponse> getNotificationHistory(UserId userId, Pageable pageable) {
        log.info("알림 히스토리 조회 시작: userId={}, page={}, size={}", 
                userId.getValue(), pageable.getPageNumber(), pageable.getPageSize());

        Page<NotificationHistory> historyPage = notificationHistoryRepository
                .findByUserIdOrderBySentAtDesc(userId, pageable);

        Page<NotificationHistoryResponse> responsePage = historyPage
                .map(NotificationHistoryResponse::from);

        log.info("알림 히스토리 조회 완료: userId={}, totalElements={}", 
                userId.getValue(), responsePage.getTotalElements());

        return responsePage;
    }

    /**
     * 사용자의 미읽은 알림 개수 조회
     *
     * @param userId 사용자 ID
     * @return 미읽은 알림 개수
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(UserId userId) {
        log.info("미읽은 알림 개수 조회 시작: userId={}", userId.getValue());

        long unreadCount = notificationHistoryRepository.countUnreadByUserId(userId);

        log.info("미읽은 알림 개수 조회 완료: userId={}, count={}", userId.getValue(), unreadCount);

        return unreadCount;
    }

    /**
     * 사용자의 모든 미읽은 알림을 읽음으로 처리
     *
     * @param userId 사용자 ID
     * @return 업데이트된 알림 개수
     */
    public int markAllAsRead(UserId userId) {
        log.info("모든 알림 읽음 처리 시작: userId={}", userId.getValue());

        int updatedCount = notificationHistoryRepository.markAllAsReadByUserId(userId);

        log.info("모든 알림 읽음 처리 완료: userId={}, updatedCount={}", userId.getValue(), updatedCount);

        return updatedCount;
    }

    /**
     * 알림 히스토리 저장
     * FCM 발송 시 호출되어 히스토리에 기록
     *
     * @param notificationHistory 저장할 알림 히스토리
     * @return 저장된 알림 히스토리
     */
    public NotificationHistory saveNotificationHistory(NotificationHistory notificationHistory) {
        log.info("알림 히스토리 저장 시작: userId={}, type={}", 
                notificationHistory.getUserId().getValue(), notificationHistory.getType());

        NotificationHistory savedHistory = notificationHistoryRepository.save(notificationHistory);

        log.info("알림 히스토리 저장 완료: id={}", savedHistory.getId().getValue());

        return savedHistory;
    }

    /**
     * 사용자의 알림 히스토리 삭제
     * 회원탈퇴 시 호출
     *
     * @param userId 사용자 ID
     */
    public void deleteNotificationHistory(UserId userId) {
        log.info("알림 히스토리 삭제 시작: userId={}", userId.getValue());

        notificationHistoryRepository.deleteByUserId(userId);

        log.info("알림 히스토리 삭제 완료: userId={}", userId.getValue());
    }
}