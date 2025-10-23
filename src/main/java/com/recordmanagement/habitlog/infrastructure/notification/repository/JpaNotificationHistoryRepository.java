package com.recordmanagement.habitlog.infrastructure.notification.repository;

import com.recordmanagement.habitlog.infrastructure.notification.entity.NotificationHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 알림 히스토리 JPA 레포지토리
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Repository
public interface JpaNotificationHistoryRepository extends JpaRepository<NotificationHistoryEntity, String> {

    /**
     * 사용자 ID로 알림 히스토리 페이징 조회 (최신순)
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 알림 히스토리 페이지
     */
    Page<NotificationHistoryEntity> findByUserIdOrderBySentAtDesc(String userId, Pageable pageable);

    /**
     * 사용자의 미읽은 알림 개수 조회
     *
     * @param userId 사용자 ID
     * @return 미읽은 알림 개수
     */
    @Query("SELECT COUNT(n) FROM NotificationHistoryEntity n WHERE n.userId = :userId AND n.isRead = false")
    long countUnreadByUserId(@Param("userId") String userId);

    /**
     * 사용자 ID로 모든 알림을 읽음으로 처리
     *
     * @param userId 사용자 ID
     * @return 업데이트된 알림 개수
     */
    @Modifying
    @Query("UPDATE NotificationHistoryEntity n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") String userId);

    /**
     * 사용자 ID로 알림 히스토리 삭제
     *
     * @param userId 사용자 ID
     */
    void deleteByUserId(String userId);
}