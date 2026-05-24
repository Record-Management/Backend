package com.recordmanagement.habitlog.domain.record.application.service;

import com.recordmanagement.habitlog.domain.exercise.application.dto.CreateExerciseRecordCommand;
import com.recordmanagement.habitlog.domain.exercise.application.service.ExerciseRecordApplicationService;
import com.recordmanagement.habitlog.domain.exercise.domain.model.ExerciseType;
import com.recordmanagement.habitlog.domain.habit.application.dto.CreateHabitRecordCommand;
import com.recordmanagement.habitlog.domain.habit.application.service.HabitRecordApplicationService;
import com.recordmanagement.habitlog.domain.record.application.dto.CreateRecordCommand;
import com.recordmanagement.habitlog.domain.record.application.dto.CreationLimitsResponse;
import com.recordmanagement.habitlog.domain.schedule.application.dto.CreateScheduleCommand;
import com.recordmanagement.habitlog.domain.schedule.application.service.ScheduleRecordApplicationService;
import com.recordmanagement.habitlog.domain.schedule.domain.model.NotificationType;
import com.recordmanagement.habitlog.domain.schedule.domain.model.RepeatType;
import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleColor;
import com.recordmanagement.habitlog.domain.user.domain.model.Email;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.SocialType;
import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 기록/일정 생성 제한 조회 API 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CreationLimitsTest {

    @Autowired
    private RecordApplicationService recordApplicationService;

    @Autowired
    private ScheduleRecordApplicationService scheduleRecordApplicationService;

    @Autowired
    private ExerciseRecordApplicationService exerciseRecordApplicationService;

    @Autowired
    private HabitRecordApplicationService habitRecordApplicationService;

    @Autowired
    private UserRepository userRepository;

    private String testUserId;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = new User(
                "테스트유저",
                Email.of("creation-limits-test@example.com"),
                SocialType.KAKAO,
                "kakao-creation-limits-123"
        );
        testUser.completeOnboarding(
                "테스트닉네임",
                RecordType.DAILY,
                LocalDate.of(1990, 1, 1),
                30
        );

        User savedUser = userRepository.save(testUser);
        testUserId = savedUser.getId().getValue();
    }

    @Test
    @DisplayName("기록과 일정이 없는 경우 모두 생성 가능하다")
    void getCreationLimits_NoRecordsAndSchedules_BothTrue() {
        // when
        CreationLimitsResponse response = recordApplicationService.getCreationLimits(
                testUserId, LocalDate.now()
        );

        // then
        assertThat(response.isCanCreateRecord()).isTrue();
        assertThat(response.isCanCreateSchedule()).isTrue();
    }

    @Test
    @DisplayName("오늘 기록이 1개 있는 경우 기록 생성이 가능하다")
    void getCreationLimits_OneRecord_CanCreateRecord() {
        // given: 오늘 기록 1개 생성
        CreateRecordCommand recordCommand = new CreateRecordCommand(
                UserId.of(testUserId),
                RecordType.DAILY,
                "😊",
                "첫 번째 기록",
                null,
                LocalDate.now(),
                LocalTime.now()
        );
        recordApplicationService.createRecord(recordCommand);

        // when
        CreationLimitsResponse response = recordApplicationService.getCreationLimits(
                testUserId, LocalDate.now()
        );

        // then
        assertThat(response.isCanCreateRecord()).isTrue(); // 1개 있으므로 1개 더 가능
        assertThat(response.isCanCreateSchedule()).isTrue(); // 일정은 별도 제한
    }

    @Test
    @DisplayName("오늘 기록이 2개 있는 경우 기록 생성이 불가능하다")
    void getCreationLimits_TwoRecords_CannotCreateRecord() {
        // given: 오늘 기록 2개 생성 (DAILY 1개 + EXERCISE 1개)
        CreateRecordCommand recordCommand1 = new CreateRecordCommand(
                UserId.of(testUserId),
                RecordType.DAILY,
                "😊",
                "첫 번째 기록",
                null,
                LocalDate.now(),
                LocalTime.now()
        );
        recordApplicationService.createRecord(recordCommand1);

        CreateExerciseRecordCommand exerciseCommand = new CreateExerciseRecordCommand(
                UserId.of(testUserId),
                ExerciseType.RUNNING,
                300,
                60,
                null,
                null,
                "운동 기록",
                List.of(),
                LocalDate.now(),
                LocalTime.now()
        );
        exerciseRecordApplicationService.createExerciseRecord(exerciseCommand);

        // when
        CreationLimitsResponse response = recordApplicationService.getCreationLimits(
                testUserId, LocalDate.now()
        );

        // then
        assertThat(response.isCanCreateRecord()).isFalse(); // 2개 있으므로 더 생성 불가
        assertThat(response.isCanCreateSchedule()).isTrue(); // 일정은 별도 제한
    }

    @Test
    @DisplayName("오늘 일정이 1개 생성된 경우 일정 생성이 가능하다")
    void getCreationLimits_OneSchedule_CanCreateSchedule() {
        // given: 오늘 일정 1개 생성
        CreateScheduleCommand scheduleCommand = new CreateScheduleCommand(
                "첫 번째 일정",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(10),
                NotificationType.NONE, null, null, RepeatType.NONE, null,
                null, ScheduleColor.RED, null
        );
        scheduleRecordApplicationService.create(testUserId, scheduleCommand);

        // when
        CreationLimitsResponse response = recordApplicationService.getCreationLimits(
                testUserId, LocalDate.now()
        );

        // then
        assertThat(response.isCanCreateRecord()).isTrue(); // 기록은 별도 제한
        assertThat(response.isCanCreateSchedule()).isTrue(); // 1개 있으므로 1개 더 가능
    }

    @Test
    @DisplayName("오늘 일정이 2개 생성된 경우 일정 생성이 불가능하다")
    void getCreationLimits_TwoSchedules_CannotCreateSchedule() {
        // given: 오늘 일정 2개 생성
        CreateScheduleCommand scheduleCommand1 = new CreateScheduleCommand(
                "첫 번째 일정",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(10),
                NotificationType.NONE, null, null, RepeatType.NONE, null,
                null, ScheduleColor.RED, null
        );
        scheduleRecordApplicationService.create(testUserId, scheduleCommand1);

        CreateScheduleCommand scheduleCommand2 = new CreateScheduleCommand(
                "두 번째 일정",
                LocalDate.now().plusDays(20),
                LocalDate.now().plusDays(20),
                NotificationType.NONE, null, null, RepeatType.NONE, null,
                null, ScheduleColor.BLUE, null
        );
        scheduleRecordApplicationService.create(testUserId, scheduleCommand2);

        // when
        CreationLimitsResponse response = recordApplicationService.getCreationLimits(
                testUserId, LocalDate.now()
        );

        // then
        assertThat(response.isCanCreateRecord()).isTrue(); // 기록은 별도 제한
        assertThat(response.isCanCreateSchedule()).isFalse(); // 2개 있으므로 더 생성 불가
    }

    @Test
    @DisplayName("기록과 일정 제한은 독립적이다")
    void getCreationLimits_RecordAndScheduleLimitsAreIndependent() {
        // given: 기록 2개 + 일정 2개 생성
        CreateRecordCommand recordCommand1 = new CreateRecordCommand(
                UserId.of(testUserId),
                RecordType.DAILY,
                "😊",
                "첫 번째 기록",
                null,
                LocalDate.now(),
                LocalTime.now()
        );
        recordApplicationService.createRecord(recordCommand1);

        CreateExerciseRecordCommand exerciseCommand = new CreateExerciseRecordCommand(
                UserId.of(testUserId),
                ExerciseType.RUNNING,
                300,
                60,
                null,
                null,
                "운동 기록",
                List.of(),
                LocalDate.now(),
                LocalTime.now()
        );
        exerciseRecordApplicationService.createExerciseRecord(exerciseCommand);

        CreateScheduleCommand scheduleCommand1 = new CreateScheduleCommand(
                "첫 번째 일정",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(10),
                NotificationType.NONE, null, null, RepeatType.NONE, null,
                null, ScheduleColor.RED, null
        );
        scheduleRecordApplicationService.create(testUserId, scheduleCommand1);

        CreateScheduleCommand scheduleCommand2 = new CreateScheduleCommand(
                "두 번째 일정",
                LocalDate.now().plusDays(20),
                LocalDate.now().plusDays(20),
                NotificationType.NONE, null, null, RepeatType.NONE, null,
                null, ScheduleColor.BLUE, null
        );
        scheduleRecordApplicationService.create(testUserId, scheduleCommand2);

        // when
        CreationLimitsResponse response = recordApplicationService.getCreationLimits(
                testUserId, LocalDate.now()
        );

        // then: 각각 독립적으로 제한 적용
        assertThat(response.isCanCreateRecord()).isFalse(); // 기록 2개 (DAILY 1 + EXERCISE 1)
        assertThat(response.isCanCreateSchedule()).isFalse(); // 일정 2개
    }

    @Test
    @DisplayName("일정 제한은 startDate가 아닌 createdAt 기준이다")
    void getCreationLimits_ScheduleLimitBasedOnCreatedAt() {
        // given: 미래 날짜를 startDate로 하는 일정 2개 생성 (오늘 생성)
        CreateScheduleCommand scheduleCommand1 = new CreateScheduleCommand(
                "한 달 뒤 일정",
                LocalDate.now().plusMonths(1),
                LocalDate.now().plusMonths(1),
                NotificationType.NONE, null, null, RepeatType.NONE, null,
                null, ScheduleColor.RED, null
        );
        scheduleRecordApplicationService.create(testUserId, scheduleCommand1);

        CreateScheduleCommand scheduleCommand2 = new CreateScheduleCommand(
                "두 달 뒤 일정",
                LocalDate.now().plusMonths(2),
                LocalDate.now().plusMonths(2),
                NotificationType.NONE, null, null, RepeatType.NONE, null,
                null, ScheduleColor.BLUE, null
        );
        scheduleRecordApplicationService.create(testUserId, scheduleCommand2);

        // when: 오늘 날짜로 조회
        CreationLimitsResponse response = recordApplicationService.getCreationLimits(
                testUserId, LocalDate.now()
        );

        // then: startDate는 미래이지만 오늘 생성했으므로 제한 적용
        assertThat(response.isCanCreateSchedule()).isFalse();
    }

    @Test
    @DisplayName("특정 과거 날짜 기준으로 조회할 수 있다")
    void getCreationLimits_CanQueryPastDate() {
        // when: 과거 날짜로 조회
        LocalDate pastDate = LocalDate.now().minusDays(10);
        CreationLimitsResponse response = recordApplicationService.getCreationLimits(
                testUserId, pastDate
        );

        // then: 과거 날짜에는 기록이 없으므로 모두 생성 가능
        assertThat(response.isCanCreateRecord()).isTrue();
        assertThat(response.isCanCreateSchedule()).isTrue();
    }
}
