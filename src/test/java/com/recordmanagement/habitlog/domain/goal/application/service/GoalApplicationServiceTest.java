package com.recordmanagement.habitlog.domain.goal.application.service;

import com.recordmanagement.habitlog.domain.goal.domain.model.Goal;
import com.recordmanagement.habitlog.domain.goal.domain.model.GoalStatus;
import com.recordmanagement.habitlog.domain.goal.domain.repository.GoalRepository;
import com.recordmanagement.habitlog.domain.habit.domain.repository.HabitRecordRepository;
import com.recordmanagement.habitlog.domain.user.domain.model.Email;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.SocialType;
import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
import com.recordmanagement.habitlog.global.config.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * 목표 Application Service 통합 테스트
 *
 * 주요 테스트 시나리오:
 * 1. 중복 IN_PROGRESS 목표 처리
 * 2. 목표 생성 시 자동 정리
 * 3. 목표 초기화 시 자동 정리
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GoalApplicationServiceTest {

    @Autowired
    private GoalApplicationService goalApplicationService;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HabitRecordRepository habitRecordRepository;

    private UserId testUserId;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUserId = UserId.of("test-user-duplicate-goals");

        // 테스트 사용자 생성
        testUser = new User(
                "테스트유저",
                Email.of("test@example.com"),
                SocialType.KAKAO,
                "kakao-test-123"
        );

        userRepository.save(testUser);

        // 테스트에서 사용할 userId 업데이트
        testUserId = testUser.getId();
    }

    @Test
    @DisplayName("중복 IN_PROGRESS 목표가 있을 때 가장 최근 목표를 반환한다")
    void findCurrentGoalByUserId_WithDuplicateInProgressGoals_ReturnsLatestGoal() {
        // given: 2개의 IN_PROGRESS 목표를 강제로 생성
        LocalDate startDate1 = LocalDate.now().minusDays(5);
        Goal goal1 = new Goal(testUserId, RecordType.HABIT, 10, startDate1);
        goalRepository.save(goal1);

        // 잠시 대기 후 두 번째 목표 생성 (createdAt이 다르도록)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        LocalDate startDate2 = LocalDate.now().minusDays(3);
        Goal goal2 = new Goal(testUserId, RecordType.EXERCISE, 20, startDate2);
        goalRepository.save(goal2);

        // when: 현재 목표 조회
        Optional<Goal> result = goalApplicationService.getCurrentGoal(testUserId);

        // then: 가장 최근 목표(goal2)가 반환되어야 함
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(goal2.getId());
        assertThat(result.get().getRecordType()).isEqualTo(RecordType.EXERCISE);
        assertThat(result.get().getGoalDays()).isEqualTo(20);
    }

    @Test
    @DisplayName("중복 IN_PROGRESS 목표가 있을 때 createGoal 호출 시 자동으로 정리한다")
    void createGoal_WithDuplicateInProgressGoals_AutomaticallyCleans() {
        // given: 2개의 IN_PROGRESS 목표를 강제로 생성
        LocalDate startDate1 = LocalDate.now().minusDays(5);
        Goal goal1 = new Goal(testUserId, RecordType.HABIT, 10, startDate1);
        goalRepository.save(goal1);

        LocalDate startDate2 = LocalDate.now().minusDays(3);
        Goal goal2 = new Goal(testUserId, RecordType.EXERCISE, 20, startDate2);
        goalRepository.save(goal2);

        // 사용자 목표 설정 초기화 (새 목표 생성 가능하도록)
        testUser.clearGoalSettings();
        userRepository.save(testUser);

        // when: 새 목표 생성 시도 (중복 정리 후 기존 목표가 있어서 예외 발생해야 함)
        // 중복 정리 후에도 최신 목표 1개는 남아있으므로 GOAL_ALREADY_IN_PROGRESS 예외 발생
        assertThatThrownBy(() ->
            goalApplicationService.createGoal(testUserId, RecordType.DAILY, 30)
        ).isInstanceOf(CustomException.class);

        // then: 중복 목표가 정리되었는지 확인 (1개만 IN_PROGRESS 상태여야 함)
        List<Goal> inProgressGoals = goalRepository.findByUserIdAndStatus(testUserId, GoalStatus.IN_PROGRESS);
        assertThat(inProgressGoals).hasSize(1);
        assertThat(inProgressGoals.get(0).getId()).isEqualTo(goal2.getId()); // 최신 목표만 남음

        // 정리된 목표는 COMPLETED 상태여야 함
        Optional<Goal> completedGoal = goalRepository.findById(goal1.getId());
        assertThat(completedGoal).isPresent();
        assertThat(completedGoal.get().getStatus()).isEqualTo(GoalStatus.COMPLETED);
    }

    @Test
    @DisplayName("중복 IN_PROGRESS 목표가 있을 때 forceCompleteCurrentGoal 호출 시 자동으로 정리한다")
    void forceCompleteCurrentGoal_WithDuplicateInProgressGoals_AutomaticallyCleans() {
        // given: 2개의 IN_PROGRESS 목표를 강제로 생성
        LocalDate startDate1 = LocalDate.now().minusDays(5);
        Goal goal1 = new Goal(testUserId, RecordType.HABIT, 10, startDate1);
        goalRepository.save(goal1);

        LocalDate startDate2 = LocalDate.now().minusDays(3);
        Goal goal2 = new Goal(testUserId, RecordType.EXERCISE, 20, startDate2);
        goalRepository.save(goal2);

        // 사용자 목표 설정
        testUser.updateGoalSettings(RecordType.EXERCISE, 20);
        userRepository.save(testUser);

        // when: 목표 강제 완료
        goalApplicationService.forceCompleteCurrentGoal(testUserId);

        // then: 모든 IN_PROGRESS 목표가 COMPLETED로 변경되어야 함
        List<Goal> inProgressGoals = goalRepository.findByUserIdAndStatus(testUserId, GoalStatus.IN_PROGRESS);
        assertThat(inProgressGoals).isEmpty();

        // 두 목표 모두 COMPLETED 상태
        Optional<Goal> completedGoal1 = goalRepository.findById(goal1.getId());
        Optional<Goal> completedGoal2 = goalRepository.findById(goal2.getId());

        assertThat(completedGoal1).isPresent();
        assertThat(completedGoal1.get().getStatus()).isEqualTo(GoalStatus.COMPLETED);

        assertThat(completedGoal2).isPresent();
        assertThat(completedGoal2.get().getStatus()).isEqualTo(GoalStatus.COMPLETED);

        // 사용자 목표 설정이 초기화되어야 함
        User updatedUser = userRepository.findById(testUserId).orElseThrow();
        assertThat(updatedUser.getMainRecordType()).isNull();
        assertThat(updatedUser.getGoalDays()).isNull();
    }

    @Test
    @DisplayName("정상 케이스: 중복 없이 목표 생성이 성공한다")
    void createGoal_WithoutDuplicates_Success() {
        // given: IN_PROGRESS 목표가 없는 상태

        // when: 새 목표 생성
        Goal createdGoal = goalApplicationService.createGoal(testUserId, RecordType.HABIT, 10);

        // then: 목표가 정상 생성되어야 함
        assertThat(createdGoal).isNotNull();
        assertThat(createdGoal.getRecordType()).isEqualTo(RecordType.HABIT);
        assertThat(createdGoal.getGoalDays()).isEqualTo(10);
        assertThat(createdGoal.getStatus()).isEqualTo(GoalStatus.IN_PROGRESS);

        // 사용자 정보도 동기화되어야 함
        User updatedUser = userRepository.findById(testUserId).orElseThrow();
        assertThat(updatedUser.getMainRecordType()).isEqualTo(RecordType.HABIT);
        assertThat(updatedUser.getGoalDays()).isEqualTo(10);
    }

    @Test
    @DisplayName("cleanupDuplicateInProgressGoals: 중복이 없으면 0을 반환한다")
    void cleanupDuplicateInProgressGoals_WithNoDuplicates_ReturnsZero() {
        // given: IN_PROGRESS 목표가 1개만 있음
        Goal goal = new Goal(testUserId, RecordType.HABIT, 10, LocalDate.now());
        goalRepository.save(goal);

        // when: 중복 정리 실행
        int cleanedCount = goalApplicationService.cleanupDuplicateInProgressGoals(testUserId);

        // then: 정리된 목표 개수는 0
        assertThat(cleanedCount).isEqualTo(0);

        // 기존 목표는 그대로 IN_PROGRESS 상태
        List<Goal> inProgressGoals = goalRepository.findByUserIdAndStatus(testUserId, GoalStatus.IN_PROGRESS);
        assertThat(inProgressGoals).hasSize(1);
    }

    @Test
    @DisplayName("cleanupDuplicateInProgressGoals: 중복이 있으면 오래된 목표를 COMPLETED로 변경한다")
    void cleanupDuplicateInProgressGoals_WithDuplicates_CompletesOldGoals() {
        // given: 3개의 IN_PROGRESS 목표를 생성
        Goal goal1 = new Goal(testUserId, RecordType.HABIT, 10, LocalDate.now().minusDays(10));
        goalRepository.save(goal1);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Goal goal2 = new Goal(testUserId, RecordType.EXERCISE, 20, LocalDate.now().minusDays(5));
        goalRepository.save(goal2);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Goal goal3 = new Goal(testUserId, RecordType.DAILY, 30, LocalDate.now());
        goalRepository.save(goal3);

        // when: 중복 정리 실행
        int cleanedCount = goalApplicationService.cleanupDuplicateInProgressGoals(testUserId);

        // then: 2개가 정리되어야 함
        assertThat(cleanedCount).isEqualTo(2);

        // 가장 최근 목표(goal3)만 IN_PROGRESS 상태
        List<Goal> inProgressGoals = goalRepository.findByUserIdAndStatus(testUserId, GoalStatus.IN_PROGRESS);
        assertThat(inProgressGoals).hasSize(1);
        assertThat(inProgressGoals.get(0).getId()).isEqualTo(goal3.getId());

        // 나머지는 COMPLETED 상태
        Optional<Goal> completedGoal1 = goalRepository.findById(goal1.getId());
        Optional<Goal> completedGoal2 = goalRepository.findById(goal2.getId());

        assertThat(completedGoal1.get().getStatus()).isEqualTo(GoalStatus.COMPLETED);
        assertThat(completedGoal2.get().getStatus()).isEqualTo(GoalStatus.COMPLETED);
    }
}
