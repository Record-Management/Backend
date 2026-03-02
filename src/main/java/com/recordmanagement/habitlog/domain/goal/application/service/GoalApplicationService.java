package com.recordmanagement.habitlog.domain.goal.application.service;

import com.recordmanagement.habitlog.domain.goal.domain.model.Goal;
import com.recordmanagement.habitlog.domain.goal.domain.model.GoalId;
import com.recordmanagement.habitlog.domain.goal.domain.model.GoalStatus;
import com.recordmanagement.habitlog.domain.goal.domain.repository.GoalRepository;
import com.recordmanagement.habitlog.domain.habit.domain.repository.HabitRecordRepository;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final HabitRecordRepository habitRecordRepository;

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

        // 중복 IN_PROGRESS 목표 자동 정리 (데이터 정합성 보장)
        int cleanedCount = cleanupDuplicateInProgressGoals(userId);
        if (cleanedCount > 0) {
            log.info("목표 생성 전 중복 목표 정리 완료 - {}개 정리됨", cleanedCount);
        }

        // 기존 진행중인 목표가 있는지 확인
        Optional<Goal> existingGoal = goalRepository.findCurrentGoalByUserId(userId);
        if (existingGoal.isPresent()) {
            throw new CustomException(ErrorCode.GOAL_ALREADY_IN_PROGRESS);
        }

        // 목표일수 유효성 검증
        if (goalDays != 10 && goalDays != 20 && goalDays != 30) {
            throw new IllegalArgumentException("목표일수는 10, 20, 30일만 가능합니다.");
        }

        try {
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
        } catch (DataIntegrityViolationException e) {
            // DB UNIQUE INDEX 위반 (race condition으로 동시에 목표 생성 시도)
            log.warn("목표 생성 중 DB 제약 위반 발생 - userId: {}, 이미 진행 중인 목표 존재", userId.getValue());
            throw new CustomException(ErrorCode.GOAL_ALREADY_IN_PROGRESS);
        }
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
            
            // 목표 자연 완료 시 내일부터의 미래 메인 습관 기록들 삭제
            if (goal.getRecordType() == RecordType.HABIT) {
                LocalDate tomorrow = LocalDate.now().plusDays(1);
                int deletedCount = habitRecordRepository.deleteMainRecordsAfterDate(userId, tomorrow);
                log.info("목표 자연 완료로 미래 메인 습관 기록 삭제 완료: userId={}, deletedCount={}", 
                        userId.getValue(), deletedCount);
            }
            
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

        // 목표 삭제 시 내일부터의 미래 메인 습관 기록들 삭제
        if (goal.getRecordType() == RecordType.HABIT) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            int deletedCount = habitRecordRepository.deleteMainRecordsAfterDate(userId, tomorrow);
            log.info("목표 삭제로 미래 메인 습관 기록 삭제 완료: userId={}, deletedCount={}, fromDate={}", 
                    userId.getValue(), deletedCount, tomorrow);
        }

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
     * 현재 목표 강제 완료 (QA 테스트 전용)
     * 
     * ### 목적
     * - **QA 테스트 편의성**: 목표 기간이 끝나지 않아도 강제로 완료 처리
     * - **달성 보고서 테스트**: currentPeriod 및 recentHistory 정상 동작 확인 가능
     * - **목표 재설정**: 완료 후 즉시 새로운 목표 설정 가능
     * 
     * ### 주의사항
     * - **테스트 전용**: 프로덕션에서는 사용하지 않는 것을 권장
     * - **User 동기화**: mainRecordType, goalDays가 null로 초기화됨
     * 
     * @param userId 사용자 ID
     * @throws CustomException 현재 목표가 없는 경우
     */
    @Transactional
    @CacheEvict(value = "user", key = "#userId.getValue()")
    public void forceCompleteCurrentGoal(UserId userId) {
        log.info("QA 테스트: 현재 목표 강제 완료 요청 - userId: {}", userId.getValue());

        // 중복 IN_PROGRESS 목표 자동 정리 (데이터 정합성 보장)
        int cleanedCount = cleanupDuplicateInProgressGoals(userId);
        if (cleanedCount > 0) {
            log.info("목표 초기화 전 중복 목표 정리 완료 - {}개 정리됨", cleanedCount);
        }

        // 1. 현재 목표 조회
        Optional<Goal> currentGoalOpt = goalRepository.findCurrentGoalByUserId(userId);
        if (currentGoalOpt.isEmpty()) {
            throw new CustomException(ErrorCode.GOAL_NOT_FOUND);
        }
        
        Goal currentGoal = currentGoalOpt.get();
        log.info("강제 완료할 목표 - goalId: {}, recordType: {}, goalDays: {}, 현재 진행률: {}%", 
                currentGoal.getId().getValue(), currentGoal.getRecordType(), currentGoal.getGoalDays(),
                currentGoal.getAchievementRate());
        
        // 2. 목표 상태를 COMPLETED로 변경 및 완료 시간 설정
        currentGoal.forceComplete(); // Goal 엔터티에 강제 완료 메서드 필요
        goalRepository.save(currentGoal);
        
        log.info("목표 강제 완료 처리 완료 - goalId: {}", currentGoal.getId().getValue());
        
        // 3. User 정보 초기화 (새 목표 설정 가능하도록)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 목표 완료 시 내일부터의 미래 메인 습관 기록들 삭제
        if (currentGoal.getRecordType() == RecordType.HABIT) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            int deletedCount = habitRecordRepository.deleteMainRecordsAfterDate(userId, tomorrow);
            log.info("목표 완료로 미래 메인 습관 기록 삭제 완료: userId={}, deletedCount={}, fromDate={}", 
                    userId.getValue(), deletedCount, tomorrow);
        }
        
        user.clearGoalSettings(); // mainRecordType, goalDays를 null로 설정
        userRepository.save(user);
        
        log.info("사용자 목표 설정 초기화 완료 - userId: {}", userId.getValue());
        log.info("QA 테스트: 목표 강제 완료 완료 - goalId: {}, 새 목표 설정 가능", currentGoal.getId().getValue());
    }

    /**
     * 중복된 IN_PROGRESS 목표 정리
     * - 여러 개의 IN_PROGRESS 목표가 존재하는 경우, 가장 최근 목표만 남기고 나머지는 COMPLETED로 변경
     * - 데이터 정합성 문제 해결용 유틸리티 메서드
     *
     * @param userId 사용자 ID
     * @return 정리된 목표 개수
     */
    @Transactional
    public int cleanupDuplicateInProgressGoals(UserId userId) {
        log.info("중복 IN_PROGRESS 목표 정리 시작 - userId: {}", userId.getValue());

        // 모든 IN_PROGRESS 목표 조회
        List<Goal> inProgressGoals = goalRepository.findByUserIdAndStatus(userId, GoalStatus.IN_PROGRESS);

        if (inProgressGoals.size() <= 1) {
            log.info("중복 목표 없음 - IN_PROGRESS 목표 개수: {}", inProgressGoals.size());
            return 0;
        }

        log.warn("중복 IN_PROGRESS 목표 발견 - 개수: {}", inProgressGoals.size());

        // 가장 최근에 생성된 목표만 유지하고 나머지는 COMPLETED로 변경
        List<Goal> sortedGoals = inProgressGoals.stream()
                .sorted((g1, g2) -> g2.getCreatedAt().compareTo(g1.getCreatedAt()))
                .toList();

        Goal latestGoal = sortedGoals.get(0);
        List<Goal> duplicateGoals = sortedGoals.subList(1, sortedGoals.size());

        int cleanedCount = 0;
        for (Goal duplicateGoal : duplicateGoals) {
            log.info("중복 목표 강제 완료 처리 - goalId: {}, createdAt: {}",
                    duplicateGoal.getId().getValue(), duplicateGoal.getCreatedAt());

            duplicateGoal.forceComplete();
            goalRepository.save(duplicateGoal);
            cleanedCount++;
        }

        log.info("중복 IN_PROGRESS 목표 정리 완료 - 유지: goalId={}, 정리: {}개",
                latestGoal.getId().getValue(), cleanedCount);

        return cleanedCount;
    }
}