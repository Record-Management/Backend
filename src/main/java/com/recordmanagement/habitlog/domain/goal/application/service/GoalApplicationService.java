package com.recordmanagement.habitlog.domain.goal.application.service;

import com.recordmanagement.habitlog.domain.goal.domain.model.Goal;
import com.recordmanagement.habitlog.domain.goal.domain.model.GoalId;
import com.recordmanagement.habitlog.domain.goal.domain.model.GoalStatus;
import com.recordmanagement.habitlog.domain.goal.domain.repository.GoalRepository;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 목표 Application Service
 * 
 * ### 목표 생명주기 관리
 * 1. 목표 생성 → User 정보 동기화 (mainRecordType, goalDays 설정)
 * 2. 진행률 업데이트 → 기록 생성 시마다 completedDays 증가
 * 3. 기간 만료 시 자동 완료 → GoalExpirationScheduler가 매일 자정에 처리
 * 4. 목표 완료/삭제 → User 정보 동기화 (mainRecordType, goalDays → null)
 * 
 * ### 자동화 기능
 * - **스케줄러 연동**: 만료된 목표 자동 완료 처리
 * - **User 동기화**: 목표 상태 변경 시 사용자 정보 자동 업데이트
 * - **진행률 추적**: 기록 생성과 연동된 실시간 진행률 계산
 *
 * @author 전우선
 * @since 2025.11.04
 * @version 1.1.0 (스케줄러 연동)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GoalApplicationService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    /**
     * 새로운 목표 생성
     *
     * @param userId 사용자 ID
     * @param recordType 기록 타입
     * @param goalDays 목표일수 (10, 20, 30)
     * @return 생성된 목표
     */
    @Transactional
    @CacheEvict(value = "calendar", allEntries = true)
    public Goal createGoal(UserId userId, RecordType recordType, int goalDays) {
        LocalDate startDate = LocalDate.now(); // 목표 설정 당일부터 시작
        log.info("Creating new goal for user: {}, recordType: {}, goalDays: {}, startDate: {}",
                userId.getValue(), recordType, goalDays, startDate);

        // 기존 진행중인 목표가 있는지 확인
        Optional<Goal> existingGoal = goalRepository.findCurrentGoalByUserId(userId);
        if (existingGoal.isPresent()) {
            throw new CustomException(ErrorCode.GOAL_ALREADY_IN_PROGRESS);
        }

        // 목표일수 유효성 검증
        if (goalDays != 10 && goalDays != 20 && goalDays != 30) {
            throw new IllegalArgumentException("목표일수는 10, 20, 30일만 가능합니다.");
        }

        Goal goal = new Goal(userId, recordType, goalDays, startDate);
        Goal savedGoal = goalRepository.save(goal);
        
        // User 정보와 동기화: 목표 생성 시 사용자 정보 업데이트
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.updateGoalSettings(recordType, goalDays);
        userRepository.save(user);
        
        log.info("User goal settings updated: userId={}, recordType={}, goalDays={}", 
                userId.getValue(), recordType, goalDays);
        
        return savedGoal;
    }

    /**
     * 현재 진행중인 목표 조회
     *
     * @param userId 사용자 ID
     * @return 현재 목표
     */
    public Optional<Goal> getCurrentGoal(UserId userId) {
        return goalRepository.findCurrentGoalByUserId(userId);
    }

    /**
     * 목표 완료일수 업데이트
     *
     * @param userId 사용자 ID
     * @param completedDays 완료일수
     */
    @Transactional
    public void updateGoalProgress(UserId userId, int completedDays) {
        log.info("Updating goal progress for user: {}, completedDays: {}", userId.getValue(), completedDays);

        Optional<Goal> currentGoal = goalRepository.findCurrentGoalByUserId(userId);
        if (currentGoal.isEmpty()) {
            log.warn("No current goal found for user: {}", userId.getValue());
            return;
        }

        Goal goal = currentGoal.get();
        goal.updateCompletedDays(completedDays);

        // 목표 기간이 종료되었다면 완료 처리
        if (goal.isPeriodEnded()) {
            goal.complete();
            
            // User 정보와 동기화: 목표 완료 시 사용자 정보 초기화
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            user.clearGoalSettings();
            userRepository.save(user);
            
            log.info("Goal period ended and completed for user: {}, user settings cleared", userId.getValue());
        }

        goalRepository.save(goal);
    }


    /**
     * 사용자의 목표 이력 조회
     *
     * @param userId 사용자 ID
     * @return 목표 이력 (최신순)
     */
    public List<Goal> getGoalHistory(UserId userId) {
        return goalRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 사용자의 목표 이력 조회 (종료일 기준)
     * 목표 달성 보고서에서 가장 최근 완료된 목표를 찾기 위해 사용
     *
     * @param userId 사용자 ID
     * @return 목표 이력 (endDate 기준 내림차순)
     */
    public List<Goal> getGoalHistoryByEndDate(UserId userId) {
        return goalRepository.findByUserIdOrderByEndDateDesc(userId);
    }

    /**
     * 사용자의 완료된 목표 목록 조회
     *
     * @param userId 사용자 ID
     * @return 완료된 목표 목록
     */
    public List<Goal> getCompletedGoals(UserId userId) {
        return goalRepository.findByUserIdAndStatus(userId, GoalStatus.COMPLETED);
    }

    /**
     * 사용자의 누적 달성 횟수 조회
     *
     * @param userId 사용자 ID
     * @return 누적 달성 횟수 (완료된 목표 개수)
     */
    public long getCumulativeAchievementCount(UserId userId) {
        return goalRepository.countCompletedGoalsByUserId(userId);
    }

    /**
     * 목표 삭제
     *
     * @param userId 사용자 ID
     * @param goalId 목표 ID
     */
    @Transactional
    public void deleteGoal(UserId userId, GoalId goalId) {
        log.info("Deleting goal: {} for user: {}", goalId.getValue(), userId.getValue());

        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 목표를 찾을 수 없습니다."));

        goalRepository.deleteById(goalId);
        
        // User 정보와 동기화: 목표 삭제 시 사용자 정보 초기화
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.clearGoalSettings();
        userRepository.save(user);
        
        log.info("Goal deleted and user settings cleared for user: {}", userId.getValue());
    }

    /**
     * 새로운 목표 설정 가능 여부 확인
     *
     * @param userId 사용자 ID
     * @return true: 설정 가능, false: 설정 불가
     */
    public boolean canCreateNewGoal(UserId userId) {
        // findCurrentGoalByUserId가 이미 기간 내 + 진행중인 목표만 반환하므로
        // 목표가 없으면 새 목표 생성 가능
        Optional<Goal> currentGoal = goalRepository.findCurrentGoalByUserId(userId);
        return currentGoal.isEmpty();
    }

    /**
     * 현재 목표 삭제 (QA 테스트 전용)
     * 
     * ### 주의사항
     * - **테스트 전용**: 프로덕션에서는 사용하지 않는 것을 권장
     * - **데이터 손실**: 목표와 관련된 진행률 정보가 완전히 삭제됨
     * - **User 동기화**: mainRecordType, goalDays가 null로 초기화됨
     * 
     * @param userId 사용자 ID
     * @throws CustomException 현재 목표가 없는 경우
     */
    @Transactional
    @CacheEvict(value = "user", key = "#userId.getValue()")
    public void deleteCurrentGoal(UserId userId) {
        log.info("QA 테스트: 현재 목표 삭제 요청 - userId: {}", userId.getValue());
        
        // 1. 현재 목표 조회
        Optional<Goal> currentGoalOpt = goalRepository.findCurrentGoalByUserId(userId);
        if (currentGoalOpt.isEmpty()) {
            throw new CustomException(ErrorCode.GOAL_NOT_FOUND);
        }
        
        Goal currentGoal = currentGoalOpt.get();
        log.info("삭제할 목표 - goalId: {}, recordType: {}, goalDays: {}", 
                currentGoal.getId().getValue(), currentGoal.getRecordType(), currentGoal.getGoalDays());
        
        // 2. User 정보 초기화 (mainRecordType, goalDays → null)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        user.clearGoalSettings(); // mainRecordType, goalDays를 null로 설정하는 메서드
        userRepository.save(user);
        
        log.info("사용자 목표 설정 초기화 완료 - userId: {}", userId.getValue());
        
        // 3. 목표 삭제
        goalRepository.deleteById(currentGoal.getId());
        
        log.info("QA 테스트: 목표 삭제 완료 - goalId: {}", currentGoal.getId().getValue());
    }
}