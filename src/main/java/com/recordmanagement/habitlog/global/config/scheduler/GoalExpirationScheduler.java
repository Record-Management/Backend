package com.recordmanagement.habitlog.global.config.scheduler;

import com.recordmanagement.habitlog.domain.goal.domain.model.Goal;
import com.recordmanagement.habitlog.domain.goal.domain.model.GoalStatus;
import com.recordmanagement.habitlog.domain.goal.domain.repository.GoalRepository;
import com.recordmanagement.habitlog.domain.goal.infrastructure.repository.GoalJpaRepository;
import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 목표 만료 자동 완료 스케줄러
 * 
 * 매일 자정에 실행되어 기간이 만료된 IN_PROGRESS 상태의 목표들을 자동으로 완료 처리합니다.
 * 
 * 처리 과정:
 * 1. endDate < 현재날짜 && status = IN_PROGRESS인 목표들 조회
 * 2. 목표 상태를 COMPLETED로 변경
 * 3. 해당 사용자의 User 정보 동기화 (mainRecordType, goalDays, habitStartDate를 null로 설정)
 * 
 * 실행 시점: 매일 자정 (00:00:00) - 한국시간 기준
 * 
 * @author 전우선
 * @since 2025.11.13
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoalExpirationScheduler {
    
    private final GoalJpaRepository goalJpaRepository;
    private final UserRepository userRepository;
    
    /**
     * 매일 자정에 만료된 목표들을 자동 완료 처리
     * 한국시간(KST) 기준으로 실행
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void completeExpiredGoals() {
        log.info("목표 만료 자동 완료 스케줄러 시작 - 자정 실행");
        
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        try {
            // 어제까지가 기간인 IN_PROGRESS 목표들 조회
            List<String> expiredGoalIds = goalJpaRepository.findExpiredInProgressGoalIds(yesterday);
            
            if (expiredGoalIds.isEmpty()) {
                log.info("만료된 목표가 없습니다. 스케줄러 종료");
                return;
            }
            
            log.info("만료된 목표 {}개 발견. 자동 완료 처리 시작", expiredGoalIds.size());
            
            int completedCount = 0;
            int userSyncCount = 0;
            
            for (String goalId : expiredGoalIds) {
                try {
                    // 목표 상태를 COMPLETED로 변경
                    int updatedGoals = goalJpaRepository.updateGoalStatusToCompleted(goalId);
                    
                    if (updatedGoals > 0) {
                        completedCount++;
                        log.debug("목표 완료 처리: goalId={}", goalId);
                        
                        // 해당 목표의 사용자 ID 조회
                        String userId = goalJpaRepository.findUserIdByGoalId(goalId);
                        
                        if (userId != null) {
                            // 사용자에게 다른 진행중인 목표가 있는지 확인
                            boolean hasOtherGoals = goalJpaRepository.hasActiveGoalsByUserId(userId, today);
                            
                            if (!hasOtherGoals) {
                                // 다른 진행중인 목표가 없다면 User 정보 동기화
                                User user = userRepository.findById(UserId.of(userId)).orElse(null);
                                if (user != null) {
                                    user.clearGoalSettings();
                                    userRepository.save(user);
                                    userSyncCount++;
                                    log.debug("사용자 정보 동기화 완료: userId={}", userId);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("목표 완료 처리 중 오류 발생: goalId={}, error={}", goalId, e.getMessage(), e);
                }
            }
            
            log.info("목표 만료 자동 완료 처리 완료: 목표 {}개 완료, 사용자 {}명 동기화", 
                    completedCount, userSyncCount);
            
        } catch (Exception e) {
            log.error("목표 만료 스케줄러 실행 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}