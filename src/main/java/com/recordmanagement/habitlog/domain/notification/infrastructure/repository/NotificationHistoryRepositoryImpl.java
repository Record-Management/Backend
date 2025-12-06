package com.recordmanagement.habitlog.domain.notification.infrastructure.repository;

import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationHistory;
import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationHistoryId;
import com.recordmanagement.habitlog.domain.notification.domain.repository.NotificationHistoryRepository;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.notification.infrastructure.entity.NotificationHistoryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 알림 히스토리 레포지토리 구현체
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
public class NotificationHistoryRepositoryImpl implements NotificationHistoryRepository {

    private final JpaNotificationHistoryRepository jpaRepository;

    @Override
    public NotificationHistory save(NotificationHistory notificationHistory) {
        NotificationHistoryEntity entity = new NotificationHistoryEntity(notificationHistory);
        NotificationHistoryEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationHistory> findById(NotificationHistoryId id) {
        return jpaRepository.findById(id.getValue())
                .map(NotificationHistoryEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationHistory> findByUserIdOrderBySentAtDesc(UserId userId, Pageable pageable) {
        Page<NotificationHistoryEntity> entityPage = jpaRepository.findByUserIdOrderBySentAtDesc(
                userId.getValue(), pageable);
        return entityPage.map(NotificationHistoryEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationHistory> findByUserIdAndSentAtAfterOrderBySentAtDesc(UserId userId, LocalDateTime sentAtAfter, Pageable pageable) {
        Page<NotificationHistoryEntity> entityPage = jpaRepository.findByUserIdAndSentAtAfterOrderBySentAtDesc(
                userId.getValue(), sentAtAfter, pageable);
        return entityPage.map(NotificationHistoryEntity::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadByUserId(UserId userId) {
        return jpaRepository.countUnreadByUserId(userId.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadByUserIdAndSentAtAfter(UserId userId, LocalDateTime sentAtAfter) {
        return jpaRepository.countUnreadByUserIdAndSentAtAfter(userId.getValue(), sentAtAfter);
    }

    @Override
    public int markAllAsReadByUserId(UserId userId) {
        return jpaRepository.markAllAsReadByUserId(userId.getValue());
    }

    @Override
    public void deleteByUserId(UserId userId) {
        jpaRepository.deleteByUserId(userId.getValue());
    }
    
    @Override
    public void deleteByUserId(String userId) {
        jpaRepository.deleteByUserId(userId);
    }
}