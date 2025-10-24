package com.recordmanagement.habitlog.domain.notification.domain.repository;

import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationSettings;
import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationSettingsId;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;

import java.util.Optional;

/**
 * 알림 설정 도메인 레포지토리 인터페이스
 *
 * - 알림 설정 관련 데이터 접근 추상화
 * - 도메인 레이어에서 정의하여 인프라스트럭처 레이어에서 구현
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
public interface NotificationSettingsRepository {

    /**
     * 알림 설정 저장
     *
     * @param notificationSettings 저장할 알림 설정
     * @return 저장된 알림 설정
     */
    NotificationSettings save(NotificationSettings notificationSettings);

    /**
     * ID로 알림 설정 조회
     *
     * @param id 알림 설정 ID
     * @return 알림 설정 (Optional)
     */
    Optional<NotificationSettings> findById(NotificationSettingsId id);

    /**
     * 사용자 ID로 알림 설정 조회
     *
     * @param userId 사용자 ID
     * @return 알림 설정 (Optional)
     */
    Optional<NotificationSettings> findByUserId(UserId userId);

    /**
     * 사용자 ID로 알림 설정 삭제
     *
     * @param userId 사용자 ID
     */
    void deleteByUserId(UserId userId);
    
    /**
     * 사용자 ID(String)로 알림 설정 삭제
     * 회원 탈퇴 시 사용
     *
     * @param userId 사용자 ID (String)
     */
    void deleteByUserId(String userId);

    /**
     * 사용자의 알림 설정 존재 여부 확인
     *
     * @param userId 사용자 ID
     * @return true 존재, false 미존재
     */
    boolean existsByUserId(UserId userId);
}