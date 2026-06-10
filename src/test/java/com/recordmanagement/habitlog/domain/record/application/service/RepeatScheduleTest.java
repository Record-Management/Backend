package com.recordmanagement.habitlog.domain.record.application.service;

import com.recordmanagement.habitlog.domain.record.application.dto.CalendarRecordResponse;
import com.recordmanagement.habitlog.domain.record.application.dto.CalendarResponse;
import com.recordmanagement.habitlog.domain.record.application.dto.ScheduleSummary;
import com.recordmanagement.habitlog.domain.schedule.application.dto.CreateScheduleCommand;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 반복 일정 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RepeatScheduleTest {

    @Autowired
    private RecordApplicationService recordApplicationService;

    @Autowired
    private ScheduleRecordApplicationService scheduleRecordApplicationService;

    @Autowired
    private UserRepository userRepository;

    private String testUserId;

    @BeforeEach
    void setUp() {
        User testUser = new User(
                "테스트유저",
                Email.of("repeat-schedule-test@example.com"),
                SocialType.KAKAO,
                "kakao-repeat-123"
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
    @DisplayName("매일(DAY) 반복 일정이 repeatEndsOn까지만 표시된다")
    void repeatDaily_WithRepeatEndsOn_ShowsUntilEndDate() {
        // given: 6월 4일부터 매일 반복, 6월 7일까지
        CreateScheduleCommand command = new CreateScheduleCommand(
                "매일 미팅",
                LocalDate.of(2026, 6, 4),
                LocalDate.of(2026, 6, 4),
                NotificationType.NONE, null, null,
                RepeatType.DAY,
                LocalDate.of(2026, 6, 7), // 7일까지만
                null,
                ScheduleColor.BLUE,
                null
        );
        scheduleRecordApplicationService.create(testUserId, command);

        // when: 6월 캘린더 조회
        CalendarResponse response = recordApplicationService.getCalendar(testUserId, 2026, 6, null);

        // then: 4, 5, 6, 7일에만 표시되고 8일부터는 없음
        assertThat(findScheduleOnDate(response, LocalDate.of(2026, 6, 3))).isNull();
        assertThat(findScheduleOnDate(response, LocalDate.of(2026, 6, 4))).isNotNull();
        assertThat(findScheduleOnDate(response, LocalDate.of(2026, 6, 5))).isNotNull();
        assertThat(findScheduleOnDate(response, LocalDate.of(2026, 6, 6))).isNotNull();
        assertThat(findScheduleOnDate(response, LocalDate.of(2026, 6, 7))).isNotNull();
        assertThat(findScheduleOnDate(response, LocalDate.of(2026, 6, 8))).isNull();
    }

    @Test
    @DisplayName("매주(WEEK) 반복 일정이 매주 같은 요일에만 표시된다")
    void repeatWeekly_ShowsOnSameDayOfWeek() {
        // given: 6월 4일(수요일)부터 매주 반복, 6월 25일까지
        CreateScheduleCommand command = new CreateScheduleCommand(
                "주간 회의",
                LocalDate.of(2026, 6, 4), // 수요일
                LocalDate.of(2026, 6, 4),
                NotificationType.NONE, null, null,
                RepeatType.WEEK,
                LocalDate.of(2026, 6, 25),
                null,
                ScheduleColor.GREEN,
                null
        );
        scheduleRecordApplicationService.create(testUserId, command);

        // when: 6월 캘린더 조회
        CalendarResponse response = recordApplicationService.getCalendar(testUserId, 2026, 6, null);

        // then: 수요일(4, 11, 18, 25)에만 표시됨
        assertThat(findScheduleOnDate(response, LocalDate.of(2026, 6, 4))).isNotNull(); // 수요일
        assertThat(findScheduleOnDate(response, LocalDate.of(2026, 6, 5))).isNull(); // 목요일
        assertThat(findScheduleOnDate(response, LocalDate.of(2026, 6, 11))).isNotNull(); // 수요일
        assertThat(findScheduleOnDate(response, LocalDate.of(2026, 6, 12))).isNull(); // 목요일
        assertThat(findScheduleOnDate(response, LocalDate.of(2026, 6, 18))).isNotNull(); // 수요일
        assertThat(findScheduleOnDate(response, LocalDate.of(2026, 6, 25))).isNotNull(); // 수요일
        assertThat(findScheduleOnDate(response, LocalDate.of(2026, 7, 2))).isNull(); // 다음달 수요일 (종료일 지남)
    }

    @Test
    @DisplayName("매월(MONTH) 반복 일정이 매월 같은 날에 표시된다")
    void repeatMonthly_ShowsOnSameDayOfMonth() {
        // given: 5월 15일부터 매월 반복, 8월 15일까지
        CreateScheduleCommand command = new CreateScheduleCommand(
                "월간 점검",
                LocalDate.of(2026, 5, 15),
                LocalDate.of(2026, 5, 15),
                NotificationType.NONE, null, null,
                RepeatType.MONTH,
                LocalDate.of(2026, 8, 15),
                null,
                ScheduleColor.ORANGE,
                null
        );
        scheduleRecordApplicationService.create(testUserId, command);

        // when: 5월, 6월, 7월, 8월 캘린더 조회
        CalendarResponse may = recordApplicationService.getCalendar(testUserId, 2026, 5, null);
        CalendarResponse june = recordApplicationService.getCalendar(testUserId, 2026, 6, null);
        CalendarResponse july = recordApplicationService.getCalendar(testUserId, 2026, 7, null);
        CalendarResponse august = recordApplicationService.getCalendar(testUserId, 2026, 8, null);

        // then: 각 달 15일에만 표시됨
        assertThat(findScheduleOnDate(may, LocalDate.of(2026, 5, 15))).isNotNull();
        assertThat(findScheduleOnDate(may, LocalDate.of(2026, 5, 16))).isNull();

        assertThat(findScheduleOnDate(june, LocalDate.of(2026, 6, 15))).isNotNull();
        assertThat(findScheduleOnDate(june, LocalDate.of(2026, 6, 16))).isNull();

        assertThat(findScheduleOnDate(july, LocalDate.of(2026, 7, 15))).isNotNull();

        assertThat(findScheduleOnDate(august, LocalDate.of(2026, 8, 15))).isNotNull();
        assertThat(findScheduleOnDate(august, LocalDate.of(2026, 8, 16))).isNull();
    }

    @Test
    @DisplayName("매년(YEAR) 반복 일정이 매년 같은 날에 표시된다")
    void repeatYearly_ShowsOnSameDayOfYear() {
        // given: 2026년 6월 10일부터 매년 반복, 2028년 6월 10일까지
        CreateScheduleCommand command = new CreateScheduleCommand(
                "연례 행사",
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 10),
                NotificationType.NONE, null, null,
                RepeatType.YEAR,
                LocalDate.of(2028, 6, 10),
                null,
                ScheduleColor.PINK,
                null
        );
        scheduleRecordApplicationService.create(testUserId, command);

        // when: 2026, 2027, 2028년 6월 캘린더 조회
        CalendarResponse y2026 = recordApplicationService.getCalendar(testUserId, 2026, 6, null);
        CalendarResponse y2027 = recordApplicationService.getCalendar(testUserId, 2027, 6, null);
        CalendarResponse y2028 = recordApplicationService.getCalendar(testUserId, 2028, 6, null);

        // then: 각 년도 6월 10일에 표시됨
        assertThat(findScheduleOnDate(y2026, LocalDate.of(2026, 6, 10))).isNotNull();
        assertThat(findScheduleOnDate(y2027, LocalDate.of(2027, 6, 10))).isNotNull();
        assertThat(findScheduleOnDate(y2028, LocalDate.of(2028, 6, 10))).isNotNull();
    }

    @Test
    @DisplayName("반복 없음(NONE)은 startDate ~ endDate 범위만 표시된다")
    void noRepeat_ShowsOnlyInDateRange() {
        // given: 6월 10일 ~ 6월 12일 일정 (반복 없음)
        CreateScheduleCommand command = new CreateScheduleCommand(
                "3일 여행",
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 12),
                NotificationType.NONE, null, null,
                RepeatType.NONE,
                null, // repeatEndsOn 없음
                null,
                ScheduleColor.YELLOW,
                null
        );
        scheduleRecordApplicationService.create(testUserId, command);

        // when: 6월 캘린더 조회
        CalendarResponse response = recordApplicationService.getCalendar(testUserId, 2026, 6, null);

        // then: 10, 11, 12일에만 표시됨
        assertThat(findScheduleOnDate(response, LocalDate.of(2026, 6, 9))).isNull();
        assertThat(findScheduleOnDate(response, LocalDate.of(2026, 6, 10))).isNotNull();
        assertThat(findScheduleOnDate(response, LocalDate.of(2026, 6, 11))).isNotNull();
        assertThat(findScheduleOnDate(response, LocalDate.of(2026, 6, 12))).isNotNull();
        assertThat(findScheduleOnDate(response, LocalDate.of(2026, 6, 13))).isNull();
    }

    private ScheduleSummary findScheduleOnDate(CalendarResponse response, LocalDate date) {
        return response.monthlyRecords().stream()
                .filter(r -> r.date().equals(date))
                .findFirst()
                .map(CalendarRecordResponse::schedules)
                .orElse(null);
    }
}
