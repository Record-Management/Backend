package com.recordmanagement.habitlog.global.config.scheduler;

import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.record.domain.repository.RecordRepository;
import com.recordmanagement.habitlog.domain.exercise.domain.repository.ExerciseRecordQueryRepository;
import com.recordmanagement.habitlog.domain.habit.domain.repository.HabitRecordRepository;
import com.recordmanagement.habitlog.domain.notification.application.service.NotificationApplicationService;
import com.recordmanagement.habitlog.domain.notification.infrastructure.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 매일 알림 스케줄러
 * 
 * 매일 오후 7시(한국시간)에 실행되어 다음 조건에 해당하는 사용자들에게 알림을 발송합니다:
 * 1. 오늘 기록을 등록하지 않은 사용자
 * 2. 목표를 설정하지 않은 사용자
 * 
 * @author 전우선
 * @since 2025.11.01
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyNotificationScheduler {
    
    private final UserRepository userRepository;
    private final RecordRepository recordRepository;
    private final ExerciseRecordQueryRepository exerciseRecordQueryRepository;
    private final HabitRecordRepository habitRecordRepository;
    private final NotificationApplicationService notificationApplicationService;
    private final PushNotificationService pushNotificationService;
    
    /**
     * 매일 오후 7시에 알림 발송
     * 한국시간(KST) 기준으로 실행
     */
    @Scheduled(cron = "0 0 19 * * *", zone = "Asia/Seoul")
    public void sendDailyNotifications() {
        log.info("매일 알림 스케줄러 시작 - 오후 7시 실행");
        
        try {
            LocalDate today = LocalDate.now();
            
            // 활성 사용자 조회 (탈퇴하지 않은 사용자)
            List<User> activeUsers = userRepository.findActiveUsers();
            log.info("활성 사용자 수: {}", activeUsers.size());
            
            int notificationSentCount = 0;
            
            for (User user : activeUsers) {
                try {
                    // FCM 토큰이 없으면 스킵
                    if (user.getFcmToken() == null || user.getFcmToken().trim().isEmpty()) {
                        continue;
                    }
                    
                    boolean shouldSendNotification = false;
                    String notificationMessage = "";
                    
                    // 1. 목표 설정 안 한 사용자 체크
                    if (user.getMainRecordType() == null) {
                        if (isGoalSettingNotificationEnabled(user.getId())) {
                            shouldSendNotification = true;
                            notificationMessage = "목표를 설정해서 습관을 시작해보세요! 🎯";
                        }
                    } 
                    // 2. 오늘 기록 안 한 사용자 체크
                    else if (!hasRecordToday(user, today)) {
                        if (isRecordNotificationEnabled(user)) {
                            shouldSendNotification = true;
                            notificationMessage = getRecordReminderMessage(user.getMainRecordType());
                        }
                    }
                    
                    // 알림 발송
                    if (shouldSendNotification) {
                        boolean sent = pushNotificationService.sendNotification(
                            user.getFcmToken(),
                            "HabitLog",
                            notificationMessage,
                            Map.of("type", "daily_reminder")
                        );
                        
                        if (sent) {
                            notificationSentCount++;
                            log.debug("알림 발송 성공: userId={}, message={}", 
                                    user.getId().getValue(), notificationMessage);
                        }
                    }
                    
                } catch (Exception e) {
                    log.error("사용자별 알림 처리 실패: userId={}, error={}", 
                            user.getId().getValue(), e.getMessage(), e);
                }
            }
            
            log.info("매일 알림 스케줄러 완료 - 총 {}명에게 알림 발송", notificationSentCount);
            
        } catch (Exception e) {
            log.error("매일 알림 스케줄러 실행 실패: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 오늘 기록이 있는지 확인
     */
    private boolean hasRecordToday(User user, LocalDate today) {
        UserId userId = user.getId();
        RecordType mainRecordType = user.getMainRecordType();
        
        try {
            switch (mainRecordType) {
                case DAILY:
                    return recordRepository.existsByUserIdAndRecordDateAndType(userId, today, RecordType.DAILY);
                case EXERCISE:
                    return !exerciseRecordQueryRepository.findByUserIdAndRecordDate(userId, today).isEmpty();
                case HABIT:
                    return !habitRecordRepository.findByUserIdAndRecordDate(userId, today).isEmpty();
                default:
                    return false;
            }
        } catch (Exception e) {
            log.warn("기록 확인 실패: userId={}, mainRecordType={}, error={}", 
                    userId.getValue(), mainRecordType, e.getMessage());
            return false; // 확인 실패 시 알림 발송하지 않음
        }
    }
    
    /**
     * 기록 타입별 알림 활성화 여부 확인
     */
    private boolean isRecordNotificationEnabled(User user) {
        try {
            switch (user.getMainRecordType()) {
                case DAILY:
                    return notificationApplicationService.isDailyRecordNotificationEnabled(user.getId());
                case EXERCISE:
                    return notificationApplicationService.isExerciseNotificationEnabled(user.getId());
                case HABIT:
                    return notificationApplicationService.isHabitNotificationEnabled(user.getId());
                default:
                    return false;
            }
        } catch (Exception e) {
            log.warn("알림 설정 확인 실패: userId={}, error={}", user.getId().getValue(), e.getMessage());
            return false;
        }
    }
    
    /**
     * 목표 미설정 알림 활성화 여부 확인
     */
    private boolean isGoalSettingNotificationEnabled(UserId userId) {
        try {
            return notificationApplicationService.isGoalSettingNotificationEnabled(userId);
        } catch (Exception e) {
            log.warn("목표 미설정 알림 설정 확인 실패: userId={}, error={}", userId.getValue(), e.getMessage());
            return false;
        }
    }
    
    /**
     * 기록 타입별 알림 메시지 생성
     */
    private String getRecordReminderMessage(RecordType recordType) {
        return switch (recordType) {
            case DAILY -> "오늘의 소중한 순간을 기록으로 남겨보세요! 📝";
            case EXERCISE -> "오늘 운동은 어떠셨나요? 기록해보세요! 💪";
            case HABIT -> "오늘 습관 실천은 어떠셨나요? 기록해보세요! ✨";
            default -> "오늘 기록을 남겨보세요! 📋";
        };
    }
}