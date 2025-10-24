package com.recordmanagement.habitlog.config.scheduler;

import com.recordmanagement.habitlog.domain.user.repository.UserRepository;
import com.recordmanagement.habitlog.domain.user.model.User;
import com.recordmanagement.habitlog.domain.auth.repository.RefreshTokenRepository;
import com.recordmanagement.habitlog.domain.record.repository.RecordRepository;
import com.recordmanagement.habitlog.domain.exercise.repository.ExerciseRecordRepository;
import com.recordmanagement.habitlog.domain.habit.repository.HabitRecordRepository;
import com.recordmanagement.habitlog.domain.notification.repository.NotificationSettingsRepository;
import com.recordmanagement.habitlog.domain.notification.repository.NotificationHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 데이터 정리 스케줄러
 * 
 * 7일이 경과한 탈퇴 사용자의 데이터를 영구 삭제합니다.
 * 매일 새벽 3시에 실행되어 삭제 대상 사용자를 확인하고 정리합니다.
 * 
 * @author 전우선
 * @since 2025.10.20
 * @version 1.0.0
 */
@Slf4j
@Component
public class UserCleanupScheduler {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RecordRepository recordRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final HabitRecordRepository habitRecordRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;
    
    public UserCleanupScheduler(UserRepository userRepository, 
                               RefreshTokenRepository refreshTokenRepository,
                               RecordRepository recordRepository,
                               ExerciseRecordRepository exerciseRecordRepository,
                               HabitRecordRepository habitRecordRepository,
                               NotificationSettingsRepository notificationSettingsRepository,
                               NotificationHistoryRepository notificationHistoryRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.recordRepository = recordRepository;
        this.exerciseRecordRepository = exerciseRecordRepository;
        this.habitRecordRepository = habitRecordRepository;
        this.notificationSettingsRepository = notificationSettingsRepository;
        this.notificationHistoryRepository = notificationHistoryRepository;
    }
    
    /**
     * 탈퇴 후 7일이 경과한 사용자 데이터 영구 삭제
     * 매일 새벽 3시에 실행
     */
    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    @Transactional
    public void cleanupExpiredWithdrawnUsers() {
        log.info("탈퇴 사용자 데이터 정리 작업 시작");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 7일이 경과한 탈퇴 사용자 조회
            List<User> expiredUsers = userRepository.findExpiredWithdrawnUsers(now);
            
            if (expiredUsers.isEmpty()) {
                log.info("삭제 대상 사용자 없음");
                return;
            }
            
            log.info("삭제 대상 사용자 수: {}", expiredUsers.size());
            
            // 각 사용자별로 관련 데이터 삭제
            for (User user : expiredUsers) {
                deleteUserData(user);
            }
            
            log.info("탈퇴 사용자 데이터 정리 작업 완료: {}명 삭제", expiredUsers.size());
            
        } catch (Exception e) {
            log.error("탈퇴 사용자 데이터 정리 작업 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 개별 사용자 데이터 영구 삭제
     * 
     * @param user 삭제할 사용자
     */
    private void deleteUserData(User user) {
        try {
            String userId = user.getId().getValue();
            
            log.info("사용자 데이터 삭제 시작: userId={}, withdrawalReason={}", 
                    userId, user.getWithdrawalReason());
            
            // 1. RefreshToken 삭제
            refreshTokenRepository.deleteByUserId(userId);
            
            // 2. 사용자 관련 데이터 완전 삭제
            deleteRelatedData(userId);
            
            // 3. 사용자 계정 영구 삭제
            userRepository.delete(user);
            
            log.info("사용자 데이터 삭제 완료: userId={}", userId);
            
        } catch (Exception e) {
            log.error("사용자 데이터 삭제 실패: userId={}, error={}", 
                    user.getId().getValue(), e.getMessage(), e);
            // 개별 사용자 삭제 실패 시에도 다른 사용자는 계속 처리
        }
    }
    
    /**
     * 사용자와 연관된 모든 데이터 삭제
     * 
     * @param userId 사용자 ID
     */
    private void deleteRelatedData(String userId) {
        try {
            // 일상 기록 삭제
            recordRepository.deleteByUserId(userId);
            log.debug("일상 기록 삭제 완료: userId={}", userId);
            
            // 운동 기록 삭제 (이미지 포함)
            exerciseRecordRepository.deleteByUserId(userId);
            log.debug("운동 기록 삭제 완료: userId={}", userId);
            
            // 습관 기록 삭제
            habitRecordRepository.deleteByUserId(userId);
            log.debug("습관 기록 삭제 완료: userId={}", userId);
            
            // 알림 설정 삭제
            notificationSettingsRepository.deleteByUserId(userId);
            log.debug("알림 설정 삭제 완료: userId={}", userId);
            
            // 알림 히스토리 삭제
            notificationHistoryRepository.deleteByUserId(userId);
            log.debug("알림 히스토리 삭제 완료: userId={}", userId);
            
            log.info("사용자 관련 데이터 삭제 완료: userId={}", userId);
            
        } catch (Exception e) {
            log.error("사용자 관련 데이터 삭제 실패: userId={}, error={}", userId, e.getMessage(), e);
            throw e; // 상위에서 처리하도록 재발생
        }
    }
}