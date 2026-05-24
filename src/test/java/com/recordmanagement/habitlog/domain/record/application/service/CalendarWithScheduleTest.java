package com.recordmanagement.habitlog.domain.record.application.service;

import com.recordmanagement.habitlog.domain.record.application.dto.CalendarResponse;
import com.recordmanagement.habitlog.domain.record.application.dto.CalendarRecordResponse;
import com.recordmanagement.habitlog.domain.record.application.dto.ScheduleSummary;
import com.recordmanagement.habitlog.domain.schedule.application.dto.CreateScheduleCommand;
import com.recordmanagement.habitlog.domain.schedule.application.dto.ScheduleResponse;
import com.recordmanagement.habitlog.domain.schedule.application.dto.UpdateScheduleCommand;
import com.recordmanagement.habitlog.domain.schedule.application.service.ScheduleRecordApplicationService;
import com.recordmanagement.habitlog.domain.schedule.domain.model.NotificationType;
import com.recordmanagement.habitlog.domain.schedule.domain.model.RepeatType;
import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleColor;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 캘린더 API에서 일정(Schedule)이 제대로 조회되는지 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CalendarWithScheduleTest {

    @Autowired
    private RecordApplicationService recordApplicationService;

    @Autowired
    private ScheduleRecordApplicationService scheduleRecordApplicationService;

    @Autowired
    private UserRepository userRepository;

    private String testUserId;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = new User(
                "테스트유저",
                Email.of("calendar-schedule-test@example.com"),
                SocialType.KAKAO,
                "kakao-calendar-schedule-123"
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
    @DisplayName("캘린더 조회 시 일정이 없는 날짜는 schedules가 null이다")
    void getCalendar_WithNoSchedule_ReturnsNullSchedules() {
        // when
        CalendarResponse response = recordApplicationService.getCalendar(
                testUserId, 2026, 5, null
        );

        // then
        assertThat(response.monthlyRecords()).isNotEmpty();

        // 일정이 없는 날짜는 schedules가 null
        CalendarRecordResponse firstDay = response.monthlyRecords().get(0);
        assertThat(firstDay.schedules()).isNull();
    }

    @Test
    @DisplayName("캘린더 조회 시 일정이 있는 날짜는 schedules 정보가 표시된다")
    void getCalendar_WithSchedule_ReturnsScheduleSummary() {
        // given: 5월 15일에 일정 생성
        CreateScheduleCommand scheduleCommand = new CreateScheduleCommand(
                "팀 회의",
                LocalDate.of(2026, 5, 15),
                LocalDate.of(2026, 5, 15),
                NotificationType.ONE_DAY_BEFORE,
                null,
                null,
                RepeatType.NONE,
                null,
                "회의실 A",
                ScheduleColor.BLUE,
                "중요 회의"
        );
        scheduleRecordApplicationService.create(testUserId, scheduleCommand);

        // when: 5월 캘린더 조회
        CalendarResponse response = recordApplicationService.getCalendar(
                testUserId, 2026, 5, null
        );

        // then: 5월 15일의 schedules 정보 확인
        CalendarRecordResponse may15 = response.monthlyRecords().stream()
                .filter(r -> r.date().equals(LocalDate.of(2026, 5, 15)))
                .findFirst()
                .orElseThrow();

        assertThat(may15.schedules()).isNotNull();
        assertThat(may15.schedules().getTitle()).isEqualTo("팀 회의");
        assertThat(may15.schedules().getSize()).isEqualTo(1);
        assertThat(may15.schedules().getColor()).isEqualTo(ScheduleColor.BLUE);
    }

    @Test
    @DisplayName("같은 날짜에 여러 일정이 있으면 size가 정확히 반영된다")
    void getCalendar_WithMultipleSchedules_ReturnCorrectSize() {
        // given: 5월 20일에 일정 3개 생성
        LocalDate targetDate = LocalDate.of(2026, 5, 20);

        scheduleRecordApplicationService.create(testUserId, new CreateScheduleCommand(
                "아침 운동",
                targetDate, targetDate,
                NotificationType.NONE, null, null,
                RepeatType.NONE, null, null,
                ScheduleColor.GREEN, null
        ));

        scheduleRecordApplicationService.create(testUserId, new CreateScheduleCommand(
                "점심 약속",
                targetDate, targetDate,
                NotificationType.NONE, null, null,
                RepeatType.NONE, null, null,
                ScheduleColor.PINK, null
        ));

        scheduleRecordApplicationService.create(testUserId, new CreateScheduleCommand(
                "저녁 미팅",
                targetDate, targetDate,
                NotificationType.NONE, null, null,
                RepeatType.NONE, null, null,
                ScheduleColor.ORANGE, null
        ));

        // when
        CalendarResponse response = recordApplicationService.getCalendar(
                testUserId, 2026, 5, null
        );

        // then: 5월 20일의 schedules size가 3
        CalendarRecordResponse may20 = response.monthlyRecords().stream()
                .filter(r -> r.date().equals(targetDate))
                .findFirst()
                .orElseThrow();

        assertThat(may20.schedules()).isNotNull();
        assertThat(may20.schedules().getSize()).isEqualTo(3);
        assertThat(may20.schedules().getTitle()).isEqualTo("아침 운동"); // 첫 번째 일정이 대표
        assertThat(may20.schedules().getColor()).isEqualTo(ScheduleColor.GREEN);
    }

    @Test
    @DisplayName("여러 날에 걸친 일정은 각 날짜마다 표시된다")
    void getCalendar_WithMultiDaySchedule_ShowsOnEachDay() {
        // given: 5월 10일 ~ 5월 12일 일정 생성 (3일간)
        CreateScheduleCommand scheduleCommand = new CreateScheduleCommand(
                "제주도 여행",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 12),
                NotificationType.NONE, null, null,
                RepeatType.NONE, null,
                "제주도",
                ScheduleColor.YELLOW,
                "가족 여행"
        );
        scheduleRecordApplicationService.create(testUserId, scheduleCommand);

        // when
        CalendarResponse response = recordApplicationService.getCalendar(
                testUserId, 2026, 5, null
        );

        // then: 5월 10, 11, 12일 모두 일정이 표시됨
        List<LocalDate> scheduleDates = List.of(
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 11),
                LocalDate.of(2026, 5, 12)
        );

        for (LocalDate date : scheduleDates) {
            CalendarRecordResponse dayRecord = response.monthlyRecords().stream()
                    .filter(r -> r.date().equals(date))
                    .findFirst()
                    .orElseThrow();

            assertThat(dayRecord.schedules()).isNotNull();
            assertThat(dayRecord.schedules().getTitle()).isEqualTo("제주도 여행");
            assertThat(dayRecord.schedules().getSize()).isEqualTo(1);
            assertThat(dayRecord.schedules().getColor()).isEqualTo(ScheduleColor.YELLOW);
        }

        // 5월 13일은 일정이 없음
        CalendarRecordResponse may13 = response.monthlyRecords().stream()
                .filter(r -> r.date().equals(LocalDate.of(2026, 5, 13)))
                .findFirst()
                .orElseThrow();
        assertThat(may13.schedules()).isNull();
    }

    @Test
    @DisplayName("type 필터링(DAILY, EXERCISE 등)과 무관하게 일정은 항상 조회된다")
    void getCalendar_WithTypeFilter_AlwaysShowsSchedules() {
        // given: 5월 25일에 일정 생성
        LocalDate targetDate = LocalDate.of(2026, 5, 25);
        CreateScheduleCommand scheduleCommand = new CreateScheduleCommand(
                "병원 예약",
                targetDate, targetDate,
                NotificationType.CUSTOM, 14, 30,
                RepeatType.NONE, null, null,
                ScheduleColor.RED,
                "정기 검진"
        );
        scheduleRecordApplicationService.create(testUserId, scheduleCommand);

        // when: DAILY 타입으로 필터링해서 조회
        CalendarResponse dailyFilterResponse = recordApplicationService.getCalendar(
                testUserId, 2026, 5, RecordType.DAILY
        );

        // then: DAILY 필터링에도 일정은 표시됨
        CalendarRecordResponse may25Daily = dailyFilterResponse.monthlyRecords().stream()
                .filter(r -> r.date().equals(targetDate))
                .findFirst()
                .orElseThrow();

        assertThat(may25Daily.schedules()).isNotNull();
        assertThat(may25Daily.schedules().getTitle()).isEqualTo("병원 예약");
        assertThat(may25Daily.schedules().getSize()).isEqualTo(1);

        // when: EXERCISE 타입으로 필터링해서 조회
        CalendarResponse exerciseFilterResponse = recordApplicationService.getCalendar(
                testUserId, 2026, 5, RecordType.EXERCISE
        );

        // then: EXERCISE 필터링에도 일정은 표시됨
        CalendarRecordResponse may25Exercise = exerciseFilterResponse.monthlyRecords().stream()
                .filter(r -> r.date().equals(targetDate))
                .findFirst()
                .orElseThrow();

        assertThat(may25Exercise.schedules()).isNotNull();
        assertThat(may25Exercise.schedules().getTitle()).isEqualTo("병원 예약");
        assertThat(may25Exercise.schedules().getSize()).isEqualTo(1);

        // when: HABIT 타입으로 필터링해서 조회
        CalendarResponse habitFilterResponse = recordApplicationService.getCalendar(
                testUserId, 2026, 5, RecordType.HABIT
        );

        // then: HABIT 필터링에도 일정은 표시됨
        CalendarRecordResponse may25Habit = habitFilterResponse.monthlyRecords().stream()
                .filter(r -> r.date().equals(targetDate))
                .findFirst()
                .orElseThrow();

        assertThat(may25Habit.schedules()).isNotNull();
        assertThat(may25Habit.schedules().getTitle()).isEqualTo("병원 예약");
        assertThat(may25Habit.schedules().getSize()).isEqualTo(1);
    }

    @Test
    @DisplayName("월 범위를 넘어가는 일정은 해당 월 범위 내에서만 표시된다")
    void getCalendar_WithCrossMonthSchedule_ShowsOnlyInRequestedMonth() {
        // given: 4월 28일 ~ 5월 3일 일정 생성 (4월 말 ~ 5월 초)
        CreateScheduleCommand scheduleCommand = new CreateScheduleCommand(
                "연휴 여행",
                LocalDate.of(2026, 4, 28),
                LocalDate.of(2026, 5, 3),
                NotificationType.NONE, null, null,
                RepeatType.NONE, null, null,
                ScheduleColor.INDIGO,
                "황금연휴"
        );
        scheduleRecordApplicationService.create(testUserId, scheduleCommand);

        // when: 5월 캘린더 조회
        CalendarResponse mayResponse = recordApplicationService.getCalendar(
                testUserId, 2026, 5, null
        );

        // then: 5월 1일, 2일, 3일에만 일정이 표시됨 (4월 28, 29, 30일은 표시 안 됨)
        List<LocalDate> mayDates = List.of(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 2),
                LocalDate.of(2026, 5, 3)
        );

        for (LocalDate date : mayDates) {
            CalendarRecordResponse dayRecord = mayResponse.monthlyRecords().stream()
                    .filter(r -> r.date().equals(date))
                    .findFirst()
                    .orElseThrow();

            assertThat(dayRecord.schedules()).isNotNull();
            assertThat(dayRecord.schedules().getTitle()).isEqualTo("연휴 여행");
        }

        // 5월 4일은 일정이 없음
        CalendarRecordResponse may4 = mayResponse.monthlyRecords().stream()
                .filter(r -> r.date().equals(LocalDate.of(2026, 5, 4)))
                .findFirst()
                .orElseThrow();
        assertThat(may4.schedules()).isNull();
    }

    @Test
    @DisplayName("일정 생성 후 캘린더 캐시가 갱신된다")
    void createSchedule_CacheEviction_ReturnsNewSchedule() {
        // given: 5월 캘린더 조회 (캐시 생성)
        LocalDate targetDate = LocalDate.of(2026, 5, 28);
        CalendarResponse beforeResponse = recordApplicationService.getCalendar(
                testUserId, 2026, 5, null
        );

        // 처음에는 5월 28일에 일정이 없음
        CalendarRecordResponse may28Before = beforeResponse.monthlyRecords().stream()
                .filter(r -> r.date().equals(targetDate))
                .findFirst()
                .orElseThrow();
        assertThat(may28Before.schedules()).isNull();

        // when: 5월 28일에 일정 생성
        CreateScheduleCommand scheduleCommand = new CreateScheduleCommand(
                "새로운 일정",
                targetDate, targetDate,
                NotificationType.NONE, null, null,
                RepeatType.NONE, null, null,
                ScheduleColor.INDIGO,
                "캐시 테스트"
        );
        scheduleRecordApplicationService.create(testUserId, scheduleCommand);

        // then: 캘린더 다시 조회 시 새 일정이 보임 (캐시가 갱신됨)
        CalendarResponse afterResponse = recordApplicationService.getCalendar(
                testUserId, 2026, 5, null
        );

        CalendarRecordResponse may28After = afterResponse.monthlyRecords().stream()
                .filter(r -> r.date().equals(targetDate))
                .findFirst()
                .orElseThrow();

        assertThat(may28After.schedules()).isNotNull();
        assertThat(may28After.schedules().getTitle()).isEqualTo("새로운 일정");
        assertThat(may28After.schedules().getSize()).isEqualTo(1);
        assertThat(may28After.schedules().getColor()).isEqualTo(ScheduleColor.INDIGO);
    }

    @Test
    @DisplayName("일정 수정 후 캘린더 캐시가 갱신된다")
    void updateSchedule_CacheEviction_ReturnsUpdatedSchedule() {
        // given: 일정 생성
        LocalDate targetDate = LocalDate.of(2026, 5, 30);
        CreateScheduleCommand createCommand = new CreateScheduleCommand(
                "원래 제목",
                targetDate, targetDate,
                NotificationType.NONE, null, null,
                RepeatType.NONE, null, null,
                ScheduleColor.RED,
                null
        );
        ScheduleResponse created = scheduleRecordApplicationService.create(testUserId, createCommand);

        // 캘린더 조회 (캐시 생성)
        CalendarResponse beforeResponse = recordApplicationService.getCalendar(
                testUserId, 2026, 5, null
        );

        CalendarRecordResponse may30Before = beforeResponse.monthlyRecords().stream()
                .filter(r -> r.date().equals(targetDate))
                .findFirst()
                .orElseThrow();
        assertThat(may30Before.schedules().getTitle()).isEqualTo("원래 제목");

        // when: 일정 수정
        UpdateScheduleCommand updateCommand = new UpdateScheduleCommand(
                "수정된 제목",
                targetDate, targetDate,
                NotificationType.ONE_DAY_BEFORE, null, null,
                RepeatType.NONE, null, null,
                ScheduleColor.BLUE,
                "수정됨"
        );
        scheduleRecordApplicationService.update(testUserId, created.getScheduleRecordId(), updateCommand);

        // then: 캘린더 다시 조회 시 수정된 내용이 보임
        CalendarResponse afterResponse = recordApplicationService.getCalendar(
                testUserId, 2026, 5, null
        );

        CalendarRecordResponse may30After = afterResponse.monthlyRecords().stream()
                .filter(r -> r.date().equals(targetDate))
                .findFirst()
                .orElseThrow();

        assertThat(may30After.schedules()).isNotNull();
        assertThat(may30After.schedules().getTitle()).isEqualTo("수정된 제목");
        assertThat(may30After.schedules().getColor()).isEqualTo(ScheduleColor.BLUE);
    }

    @Test
    @DisplayName("일정 삭제 후 캘린더 캐시가 갱신된다")
    void deleteSchedule_CacheEviction_ScheduleDisappears() {
        // given: 일정 생성
        LocalDate targetDate = LocalDate.of(2026, 5, 31);
        CreateScheduleCommand createCommand = new CreateScheduleCommand(
                "삭제할 일정",
                targetDate, targetDate,
                NotificationType.NONE, null, null,
                RepeatType.NONE, null, null,
                ScheduleColor.GREEN,
                null
        );
        ScheduleResponse created = scheduleRecordApplicationService.create(testUserId, createCommand);

        // 캘린더 조회 (캐시 생성)
        CalendarResponse beforeResponse = recordApplicationService.getCalendar(
                testUserId, 2026, 5, null
        );

        CalendarRecordResponse may31Before = beforeResponse.monthlyRecords().stream()
                .filter(r -> r.date().equals(targetDate))
                .findFirst()
                .orElseThrow();
        assertThat(may31Before.schedules()).isNotNull();
        assertThat(may31Before.schedules().getTitle()).isEqualTo("삭제할 일정");

        // when: 일정 삭제
        scheduleRecordApplicationService.delete(testUserId, created.getScheduleRecordId());

        // then: 캘린더 다시 조회 시 일정이 사라짐
        CalendarResponse afterResponse = recordApplicationService.getCalendar(
                testUserId, 2026, 5, null
        );

        CalendarRecordResponse may31After = afterResponse.monthlyRecords().stream()
                .filter(r -> r.date().equals(targetDate))
                .findFirst()
                .orElseThrow();

        assertThat(may31After.schedules()).isNull();
    }
}
