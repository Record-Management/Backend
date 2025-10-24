package com.recordmanagement.habitlog.domain.notification.domain.repository;

import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationHistory;
import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationHistoryId;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 알림 히스토리 도메인 레포지토리 인터페이스
 *
 * - 알림 히스토리 관련 데이터 접근 추상화
 * - 도메인 레이어에서 정의하여 인프라스트럭처 레이어에서 구현
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
public interface NotificationHistoryRepository {

    /**
     * 알림 히스토리 저장
     *
     * @param notificationHistory 저장할 알림 히스토리
     * @return 저장된 알림 히스토리
     */
    NotificationHistory save(NotificationHistory notificationHistory);

    /**
     * ID로 알림 히스토리 조회
     *
     * @param id 알림 히스토리 ID
     * @return 알림 히스토리 (Optional)
     */
    Optional<NotificationHistory> findById(NotificationHistoryId id);

    /**
     * 사용자 ID로 알림 히스토리 페이징 조회 (최신순)
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 알림 히스토리 페이지
     */
    Page<NotificationHistory> findByUserIdOrderBySentAtDesc(UserId userId, Pageable pageable);

    /**
     * 사용자의 미읽은 알림 개수 조회
     *
     * @param userId 사용자 ID
     * @return 미읽은 알림 개수
     */
    long countUnreadByUserId(UserId userId);

    /**
     * 사용자 ID로 모든 알림을 읽음으로 처리
     *
     * @param userId 사용자 ID
     * @return 업데이트된 알림 개수
     */
    int markAllAsReadByUserId(UserId userId);

    /**
     * 사용자 ID로 알림 히스토리 삭제
     *
     * @param userId 사용자 ID
     */
    void deleteByUserId(UserId userId);
    
    /**
     * 사용자 ID(String)로 알림 히스토리 삭제
     * 회원 탈퇴 시 사용
     *
     * @param userId 사용자 ID (String)
     */
    void deleteByUserId(String userId);
}