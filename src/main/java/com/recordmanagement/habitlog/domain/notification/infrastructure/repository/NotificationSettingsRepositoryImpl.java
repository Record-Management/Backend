package com.recordmanagement.habitlog.domain.auth.infrastructure.notification.repository;

import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationSettings;
import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationSettingsId;
import com.recordmanagement.habitlog.domain.notification.domain.repository.NotificationSettingsRepository;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.notification.infrastructure.entity.NotificationSettingsEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 알림 설정 레포지토리 구현체
 *
 * - 도메인 레포지토리 인터페이스의 구현
 * - JPA를 통한 데이터베이스 연동
 * - 도메인 모델과 JPA 엔터티 간 변환 담당
 *
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Repository
@RequiredArgsConstructor
@Transactional
public class NotificationSettingsRepositoryImpl implements NotificationSettingsRepository {

    private final JpaNotificationSettingsRepository jpaRepository;

    @Override
    public NotificationSettings save(NotificationSettings notificationSettings) {
        NotificationSettingsEntity entity = new NotificationSettingsEntity(notificationSettings);
        NotificationSettingsEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationSettings> findById(NotificationSettingsId id) {
        return jpaRepository.findById(id.getValue())
                .map(NotificationSettingsEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationSettings> findByUserId(UserId userId) {
        return jpaRepository.findByUserId(userId.getValue())
                .map(NotificationSettingsEntity::toDomain);
    }

    @Override
    public void deleteByUserId(UserId userId) {
        jpaRepository.deleteByUserId(userId.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserId(UserId userId) {
        return jpaRepository.existsByUserId(userId.getValue());
    }
    
    @Override
    public void deleteByUserId(String userId) {
        jpaRepository.deleteByUserId(userId);
    }
}