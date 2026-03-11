package com.recordmanagement.habitlog.global.config.scheduler;

import com.recordmanagement.habitlog.domain.habit.domain.model.HabitRecord;
import com.recordmanagement.habitlog.domain.habit.domain.model.HabitType;
import com.recordmanagement.habitlog.domain.habit.domain.repository.HabitRecordRepository;
import com.recordmanagement.habitlog.domain.user.domain.model.Email;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.SocialType;
import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * 습관 기록 자동 생성 스케줄러 테스트
 *
 * 주요 테스트 시나리오:
 * 1. 습관 타입 사용자의 오늘 날짜 메인 습관 기록 자동 생성
 * 2. 이미 메인 습관 기록이 있으면 건너뛰기
 * 3. 습관 기간 외의 사용자는 건너뛰기
 * 4. 어제 메인 기록과 동일한 habitType으로 생성
 * 5. 첫 습관 기록은 WATER_DRINKING으로 생성
 * 6. 배치 처리 정상 동작 (여러 사용자 동시 처리)
 * 7. 운동/일상 타입 사용자는 건너뛰기
 * 8. 습관 시작일이 없는 사용자는 건너뛰기
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HabitRecordAutoGenerationSchedulerTest {

    @Autowired
    private HabitRecordAutoGenerationScheduler scheduler;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HabitRecordRepository habitRecordRepository;

    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
    }

    @Test
    @DisplayName("습관 타입 사용자의 첫 메인 습관 기록을 WATER_DRINKING으로 자동 생성한다")
    void generateDailyMainHabitRecords_FirstHabit_CreatesWaterDrinking() {
        // given: 습관 타입 사용자 생성 (습관 기간 내)
        User user = createHabitUser("첫습관유저", "first@test.com", today, 30);
        userRepository.save(user);

        // when: 스케줄러 실행
        scheduler.generateDailyMainHabitRecords();

        // then: 오늘 날짜에 WATER_DRINKING 메인 습관 기록이 생성되어야 함
        List<HabitRecord> records = habitRecordRepository.findByUserIdAndRecordDate(user.getId(), today);

        assertThat(records).hasSize(1);
        assertThat(records.get(0).getHabitType()).isEqualTo(HabitType.WATER_DRINKING);
        assertThat(records.get(0).isMainRecord()).isTrue();
        assertThat(records.get(0).getMemo()).isNull(); // 캘린더에 표시되도록 memo=null
        assertThat(records.get(0).isCompleted()).isFalse();
    }

    @Test
    @DisplayName("어제 메인 기록과 동일한 habitType으로 오늘 메인 습관 기록을 자동 생성한다")
    void generateDailyMainHabitRecords_WithYesterdayRecord_UsesSameHabitType() {
        // given: 습관 타입 사용자 생성
        User user = createHabitUser("어제습관유저", "yesterday@test.com", today.minusDays(1), 30);
        userRepository.save(user);

        // 어제 READING 메인 습관 기록 생성
        HabitRecord yesterdayRecord = HabitRecord.create(
                user.getId(),
                HabitType.READING,
                false,
                null,
                null,
                today.minusDays(1)
        );
        yesterdayRecord = yesterdayRecord.updateMainRecordStatus(true);
        habitRecordRepository.save(yesterdayRecord);

        // when: 스케줄러 실행
        scheduler.generateDailyMainHabitRecords();

        // then: 오늘 날짜에 READING 메인 습관 기록이 생성되어야 함
        List<HabitRecord> todayRecords = habitRecordRepository.findByUserIdAndRecordDate(user.getId(), today);

        assertThat(todayRecords).hasSize(1);
        assertThat(todayRecords.get(0).getHabitType()).isEqualTo(HabitType.READING);
        assertThat(todayRecords.get(0).isMainRecord()).isTrue();
        assertThat(todayRecords.get(0).getMemo()).isNull();
    }

    @Test
    @DisplayName("이미 오늘 메인 습관 기록이 있으면 건너뛴다")
    void generateDailyMainHabitRecords_WithExistingTodayRecord_Skips() {
        // given: 습관 타입 사용자 생성
        User user = createHabitUser("이미있음유저", "existing@test.com", today, 30);
        userRepository.save(user);

        // 오늘 메인 습관 기록 이미 존재
        HabitRecord existingRecord = HabitRecord.create(
                user.getId(),
                HabitType.STRETCHING,
                false,
                null,
                "이미 작성한 기록",
                today
        );
        existingRecord = existingRecord.updateMainRecordStatus(true);
        habitRecordRepository.save(existingRecord);

        // when: 스케줄러 실행
        scheduler.generateDailyMainHabitRecords();

        // then: 여전히 1개의 메인 습관 기록만 있어야 함 (중복 생성 안됨)
        List<HabitRecord> records = habitRecordRepository.findByUserIdAndRecordDate(user.getId(), today);

        assertThat(records).hasSize(1);
        assertThat(records.get(0).getHabitType()).isEqualTo(HabitType.STRETCHING);
        assertThat(records.get(0).getMemo()).isEqualTo("이미 작성한 기록"); // 기존 기록 유지
    }

    @Test
    @DisplayName("습관 기간이 지난 사용자는 건너뛴다")
    void generateDailyMainHabitRecords_OutOfHabitPeriod_Skips() {
        // given: 습관 기간이 지난 사용자 (시작일로부터 10일 목표, 오늘은 15일째)
        LocalDate startDate = today.minusDays(15);
        User user = createHabitUser("기간지남유저", "expired@test.com", startDate, 10);
        userRepository.save(user);

        // when: 스케줄러 실행
        scheduler.generateDailyMainHabitRecords();

        // then: 오늘 날짜에 습관 기록이 생성되지 않아야 함
        List<HabitRecord> records = habitRecordRepository.findByUserIdAndRecordDate(user.getId(), today);

        assertThat(records).isEmpty();
    }

    @Test
    @DisplayName("운동/일상 타입 사용자는 건너뛴다")
    void generateDailyMainHabitRecords_NonHabitTypeUser_Skips() {
        // given: 운동 타입 사용자
        User exerciseUser = new User(
                "운동유저",
                Email.of("exercise@test.com"),
                SocialType.KAKAO,
                "kakao-exercise-123"
        );
        exerciseUser.updateGoalSettings(RecordType.EXERCISE, 30);
        userRepository.save(exerciseUser);

        // 일상 타입 사용자
        User dailyUser = new User(
                "일상유저",
                Email.of("daily@test.com"),
                SocialType.KAKAO,
                "kakao-daily-123"
        );
        dailyUser.updateGoalSettings(RecordType.DAILY, 30);
        userRepository.save(dailyUser);

        // when: 스케줄러 실행
        scheduler.generateDailyMainHabitRecords();

        // then: 두 사용자 모두 습관 기록이 생성되지 않아야 함
        List<HabitRecord> exerciseRecords = habitRecordRepository.findByUserIdAndRecordDate(exerciseUser.getId(), today);
        List<HabitRecord> dailyRecords = habitRecordRepository.findByUserIdAndRecordDate(dailyUser.getId(), today);

        assertThat(exerciseRecords).isEmpty();
        assertThat(dailyRecords).isEmpty();
    }

    @Test
    @DisplayName("배치 처리로 여러 사용자의 메인 습관 기록을 생성한다")
    void generateDailyMainHabitRecords_BatchProcessing_CreatesMultipleRecords() {
        // given: 습관 타입 사용자 5명 생성
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            User user = createHabitUser("배치유저" + i, "batch" + i + "@test.com", today, 30);
            userRepository.save(user);
            users.add(user);
        }

        // when: 스케줄러 실행
        scheduler.generateDailyMainHabitRecords();

        // then: 모든 사용자에게 오늘 날짜 메인 습관 기록이 생성되어야 함
        for (User user : users) {
            List<HabitRecord> records = habitRecordRepository.findByUserIdAndRecordDate(user.getId(), today);

            assertThat(records).hasSize(1);
            assertThat(records.get(0).isMainRecord()).isTrue();
            assertThat(records.get(0).getMemo()).isNull();
        }
    }

    @Test
    @DisplayName("습관 시작일이 설정되지 않은 사용자는 건너뛴다")
    void generateDailyMainHabitRecords_NoHabitStartDate_Skips() {
        // given: 습관 타입이지만 habitStartDate가 null인 사용자
        User user = new User(
                "시작일없음유저",
                Email.of("nostart@test.com"),
                SocialType.KAKAO,
                "kakao-nostart-123"
        );
        user.updateGoalSettings(RecordType.HABIT, 30);

        // 리플렉션을 사용하여 habitStartDate를 null로 강제 설정
        try {
            var field = User.class.getDeclaredField("habitStartDate");
            field.setAccessible(true);
            field.set(user, null);
        } catch (Exception e) {
            throw new RuntimeException("habitStartDate null 설정 실패", e);
        }

        userRepository.save(user);

        // when: 스케줄러 실행
        scheduler.generateDailyMainHabitRecords();

        // then: 습관 기록이 생성되지 않아야 함
        List<HabitRecord> records = habitRecordRepository.findByUserIdAndRecordDate(user.getId(), today);

        assertThat(records).isEmpty();
    }

    /**
     * 테스트용 습관 타입 사용자 생성 헬퍼 메서드
     */
    private User createHabitUser(String name, String email, LocalDate habitStartDate, int goalDays) {
        User user = new User(
                name,
                Email.of(email),
                SocialType.KAKAO,
                "kakao-" + email
        );
        user.updateGoalSettings(RecordType.HABIT, goalDays);

        // 리플렉션을 사용하여 habitStartDate 설정
        try {
            var field = User.class.getDeclaredField("habitStartDate");
            field.setAccessible(true);
            field.set(user, habitStartDate);
        } catch (Exception e) {
            throw new RuntimeException("habitStartDate 설정 실패", e);
        }

        return user;
    }
}
