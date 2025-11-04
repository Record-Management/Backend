package com.recordmanagement.habitlog.domain.notification.application.service;

import com.recordmanagement.habitlog.domain.notification.application.dto.NotificationSettingsCommand;
import com.recordmanagement.habitlog.domain.notification.application.dto.NotificationSettingsResponse;
import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationSettings;
import com.recordmanagement.habitlog.domain.notification.domain.repository.NotificationSettingsRepository;
import com.recordmanagement.habitlog.domain.notification.domain.service.NotificationReadStatusService;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 알림 설정 애플리케이션 서비스
 *
 * - 알림 설정 관련 비즈니스 로직 처리
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
public class NotificationApplicationService implements NotificationReadStatusService {

    private final NotificationSettingsRepository notificationSettingsRepository;

    /**
     * 사용자의 알림 설정 조회
     * 설정이 없으면 기본 설정으로 새로 생성
     *
     * @param userId 사용자 ID
     * @return 알림 설정
     */
    @Transactional(readOnly = true)
    public NotificationSettingsResponse getNotificationSettings(UserId userId) {
        log.info("알림 설정 조회 시작: userId={}", userId.getValue());

        NotificationSettings settings = notificationSettingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("알림 설정이 없어 기본 설정으로 생성: userId={}", userId.getValue());
                    NotificationSettings newSettings = new NotificationSettings(userId);
                    return notificationSettingsRepository.save(newSettings);
                });

        log.info("알림 설정 조회 완료: userId={}", userId.getValue());
        return NotificationSettingsResponse.from(settings);
    }

    /**
     * 사용자의 알림 설정 업데이트
     * 선택적 업데이트 지원 (null이 아닌 값만 업데이트)
     *
     * @param command 알림 설정 업데이트 명령
     * @return 업데이트된 알림 설정
     */
    public NotificationSettingsResponse updateNotificationSettings(NotificationSettingsCommand command) {
        log.info("알림 설정 업데이트 시작: userId={}", command.getUserId().getValue());

        NotificationSettings settings = notificationSettingsRepository.findByUserId(command.getUserId())
                .orElseGet(() -> {
                    log.info("알림 설정이 없어 새로 생성: userId={}", command.getUserId().getValue());
                    return new NotificationSettings(command.getUserId());
                });

        // 선택적 업데이트 (null이 아닌 값만 업데이트)
        if (command.getDailyRecordNotificationEnabled() != null) {
            settings.updateDailyRecordNotification(command.getDailyRecordNotificationEnabled());
        }
        if (command.getExerciseNotificationEnabled() != null) {
            settings.updateExerciseNotification(command.getExerciseNotificationEnabled());
        }
        if (command.getHabitNotificationEnabled() != null) {
            settings.updateHabitNotification(command.getHabitNotificationEnabled());
        }
        if (command.getGoalSettingNotificationEnabled() != null) {
            settings.updateGoalSettingNotification(command.getGoalSettingNotificationEnabled());
        }

        NotificationSettings savedSettings = notificationSettingsRepository.save(settings);

        log.info("알림 설정 업데이트 완료: userId={}", command.getUserId().getValue());
        return NotificationSettingsResponse.from(savedSettings);
    }

    /**
     * 알림 센터 마지막 확인 시간 업데이트
     * 알림 센터 화면 진입 시 자동으로 호출되어 읽음 처리 시점을 기록
     *
     * @param userId 사용자 ID
     */
    public void updateLastCheckedAt(UserId userId) {
        log.info("알림 센터 마지막 확인 시간 업데이트 시작: userId={}", userId.getValue());

        NotificationSettings settings = notificationSettingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("알림 설정이 없어 기본 설정으로 생성: userId={}", userId.getValue());
                    return new NotificationSettings(userId);
                });

        settings.updateLastCheckedAt();
        notificationSettingsRepository.save(settings);

        log.info("알림 센터 마지막 확인 시간 업데이트 완료: userId={}", userId.getValue());
    }

    /**
     * 사용자의 마지막 확인 시간 조회
     * 
     * @param userId 사용자 ID
     * @return 마지막 확인 시간 (없으면 null)
     */
    @Override
    @Transactional(readOnly = true)
    public LocalDateTime getLastCheckedAt(UserId userId) {
        return notificationSettingsRepository.findByUserId(userId)
                .map(NotificationSettings::getLastCheckedAt)
                .orElse(null);
    }

    /**
     * 사용자의 알림 설정 삭제
     * 회원탈퇴 시 호출
     *
     * @param userId 사용자 ID
     */
    public void deleteNotificationSettings(UserId userId) {
        log.info("알림 설정 삭제 시작: userId={}", userId.getValue());

        notificationSettingsRepository.deleteByUserId(userId);

        log.info("알림 설정 삭제 완료: userId={}", userId.getValue());
    }

    /**
     * 사용자의 특정 알림이 활성화되어 있는지 확인
     * 설정이 없으면 기본 설정을 생성하여 일관성 보장
     *
     * @param userId 사용자 ID
     * @return 메인 기록 알림 활성화 여부
     */
    @Transactional
    public boolean isDailyRecordNotificationEnabled(UserId userId) {
        return notificationSettingsRepository.findByUserId(userId)
                .map(NotificationSettings::isDailyRecordNotificationEnabled)
                .orElseGet(() -> {
                    log.info("알림 설정이 없어 기본 설정으로 생성: userId={}", userId.getValue());
                    NotificationSettings newSettings = new NotificationSettings(userId);
                    notificationSettingsRepository.save(newSettings);
                    return true; // 기본값: 활성화
                });
    }

    /**
     * 사용자의 운동 알림이 활성화되어 있는지 확인
     * 설정이 없으면 기본 설정을 생성하여 일관성 보장
     *
     * @param userId 사용자 ID
     * @return 운동 기록 알림 활성화 여부
     */
    @Transactional
    public boolean isExerciseNotificationEnabled(UserId userId) {
        return notificationSettingsRepository.findByUserId(userId)
                .map(NotificationSettings::isExerciseNotificationEnabled)
                .orElseGet(() -> {
                    log.info("알림 설정이 없어 기본 설정으로 생성: userId={}", userId.getValue());
                    NotificationSettings newSettings = new NotificationSettings(userId);
                    notificationSettingsRepository.save(newSettings);
                    return true; // 기본값: 활성화
                });
    }

    /**
     * 사용자의 습관 알림이 활성화되어 있는지 확인
     * 설정이 없으면 기본 설정을 생성하여 일관성 보장
     *
     * @param userId 사용자 ID
     * @return 습관 기록 알림 활성화 여부
     */
    @Transactional
    public boolean isHabitNotificationEnabled(UserId userId) {
        return notificationSettingsRepository.findByUserId(userId)
                .map(NotificationSettings::isHabitNotificationEnabled)
                .orElseGet(() -> {
                    log.info("알림 설정이 없어 기본 설정으로 생성: userId={}", userId.getValue());
                    NotificationSettings newSettings = new NotificationSettings(userId);
                    notificationSettingsRepository.save(newSettings);
                    return true; // 기본값: 활성화
                });
    }

    /**
     * 사용자의 목표 미설정 알림이 활성화되어 있는지 확인
     * 설정이 없으면 기본 설정을 생성하여 일관성 보장
     *
     * @param userId 사용자 ID
     * @return 목표 미설정 알림 활성화 여부
     */
    @Transactional
    public boolean isGoalSettingNotificationEnabled(UserId userId) {
        return notificationSettingsRepository.findByUserId(userId)
                .map(NotificationSettings::isGoalSettingNotificationEnabled)
                .orElseGet(() -> {
                    log.info("알림 설정이 없어 기본 설정으로 생성: userId={}", userId.getValue());
                    NotificationSettings newSettings = new NotificationSettings(userId);
                    notificationSettingsRepository.save(newSettings);
                    return true; // 기본값: 활성화
                });
    }
}