package com.recordmanagement.habitlog.domain.notification.infrastructure.repository;

import com.recordmanagement.habitlog.domain.notification.infrastructure.entity.NotificationSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 알림 설정 JPA 레포지토리
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Repository
public interface JpaNotificationSettingsRepository extends JpaRepository<NotificationSettingsEntity, String> {

    /**
     * 사용자 ID로 알림 설정 조회
     *
     * @param userId 사용자 ID
     * @return 알림 설정 엔터티 (Optional)
     */
    Optional<NotificationSettingsEntity> findByUserId(String userId);

    /**
     * 사용자 ID로 알림 설정 삭제
     *
     * @param userId 사용자 ID
     */
    void deleteByUserId(String userId);

    /**
     * 사용자의 알림 설정 존재 여부 확인
     *
     * @param userId 사용자 ID
     * @return true 존재, false 미존재
     */
    boolean existsByUserId(String userId);
}