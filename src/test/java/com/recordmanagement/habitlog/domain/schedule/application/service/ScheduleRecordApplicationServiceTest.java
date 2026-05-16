package com.recordmanagement.habitlog.domain.schedule.application.service;

import com.recordmanagement.habitlog.domain.schedule.application.dto.CreateScheduleCommand;
import com.recordmanagement.habitlog.domain.schedule.application.dto.ScheduleResponse;
import com.recordmanagement.habitlog.domain.schedule.application.dto.UpdateScheduleCommand;
import com.recordmanagement.habitlog.domain.schedule.domain.model.NotificationType;
import com.recordmanagement.habitlog.domain.schedule.domain.model.RepeatType;
import com.recordmanagement.habitlog.domain.schedule.domain.model.ScheduleColor;
import com.recordmanagement.habitlog.domain.schedule.domain.repository.ScheduleRecordRepository;
import com.recordmanagement.habitlog.domain.user.domain.model.Email;
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

import static org.assertj.core.api.Assertions.*;

/**
 * 일정 기록 Application Service 통합 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ScheduleRecordApplicationServiceTest {

    @Autowired
    private ScheduleRecordApplicationService scheduleRecordApplicationService;

    @Autowired
    private ScheduleRecordRepository scheduleRecordRepository;

    @Autowired
    private UserRepository userRepository;

    private String testUserId;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = new User(
                "테스트유저",
                Email.of("schedule-test@example.com"),
                SocialType.KAKAO,
                "kakao-schedule-test-123"
        );

        User savedUser = userRepository.save(testUser);
        testUserId = savedUser.getId().getValue();
    }

    @Test
    @DisplayName("일정을 생성할 수 있다")
    void createSchedule_Success() {
        // given
        CreateScheduleCommand command = new CreateScheduleCommand(
                "매장 점검",
                LocalDate.of(2026, 3, 21),
                LocalDate.of(2026, 3, 21),
                NotificationType.ONE_DAY_BEFORE,
                null,
                RepeatType.NONE,
                null,
                "도쿄점",
                ScheduleColor.ORANGE,
                "오픈 전 냉장고 점검"
        );

        // when
        ScheduleResponse response = scheduleRecordApplicationService.create(testUserId, command);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getScheduleRecordId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("매장 점검");
        assertThat(response.getStartDate()).isEqualTo(LocalDate.of(2026, 3, 21));
        assertThat(response.getEndDate()).isEqualTo(LocalDate.of(2026, 3, 21));
        assertThat(response.getNotificationType()).isEqualTo(NotificationType.ONE_DAY_BEFORE);
        assertThat(response.getRepeatType()).isEqualTo(RepeatType.NONE);
        assertThat(response.getLocation()).isEqualTo("도쿄점");
        assertThat(response.getColor()).isEqualTo(ScheduleColor.ORANGE);
        assertThat(response.getMemo()).isEqualTo("오픈 전 냉장고 점검");
    }

    @Test
    @DisplayName("CUSTOM 알림 타입으로 일정을 생성할 수 있다")
    void createSchedule_WithCustomNotification_Success() {
        // given
        CreateScheduleCommand command = new CreateScheduleCommand(
                "회의",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 10),
                NotificationType.CUSTOM,
                9, // startDate 당일 오전 9시에 알림
                RepeatType.NONE,
                null,
                "회의실 A",
                ScheduleColor.BLUE,
                "중요 회의"
        );

        // when
        ScheduleResponse response = scheduleRecordApplicationService.create(testUserId, command);

        // then
        assertThat(response.getNotificationType()).isEqualTo(NotificationType.CUSTOM);
        assertThat(response.getNotificationCustomHours()).isEqualTo(9);
    }

    @Test
    @DisplayName("시작일이 종료일보다 늦으면 예외가 발생한다")
    void createSchedule_WithInvalidDateRange_ThrowsException() {
        // given
        CreateScheduleCommand command = new CreateScheduleCommand(
                "잘못된 일정",
                LocalDate.of(2026, 3, 25),
                LocalDate.of(2026, 3, 20), // 종료일이 시작일보다 빠름
                NotificationType.NONE,
                null,
                RepeatType.NONE,
                null,
                null,
                ScheduleColor.RED,
                null
        );

        // when & then
        assertThatThrownBy(() -> scheduleRecordApplicationService.create(testUserId, command))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("CUSTOM 알림 시 customHours가 없으면 예외가 발생한다")
    void createSchedule_WithCustomNotificationWithoutValues_ThrowsException() {
        // given
        CreateScheduleCommand command = new CreateScheduleCommand(
                "회의",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 10),
                NotificationType.CUSTOM,
                null, // customHours 없음
                RepeatType.NONE,
                null,
                null,
                ScheduleColor.BLUE,
                null
        );

        // when & then
        assertThatThrownBy(() -> scheduleRecordApplicationService.create(testUserId, command))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("일정을 수정할 수 있다")
    void updateSchedule_Success() {
        // given: 일정 생성
        CreateScheduleCommand createCommand = new CreateScheduleCommand(
                "원래 제목",
                LocalDate.of(2026, 3, 21),
                LocalDate.of(2026, 3, 21),
                NotificationType.NONE,
                null,
                RepeatType.NONE,
                null,
                "원래 위치",
                ScheduleColor.RED,
                "원래 메모"
        );
        ScheduleResponse created = scheduleRecordApplicationService.create(testUserId, createCommand);

        // when: 일정 수정
        UpdateScheduleCommand updateCommand = new UpdateScheduleCommand(
                "수정된 제목",
                LocalDate.of(2026, 3, 22),
                LocalDate.of(2026, 3, 23),
                NotificationType.ONE_DAY_BEFORE,
                null,
                RepeatType.DAY,
                LocalDate.of(2026, 4, 1),
                "수정된 위치",
                ScheduleColor.BLUE,
                "수정된 메모"
        );
        ScheduleResponse updated = scheduleRecordApplicationService.update(
                testUserId, created.getScheduleRecordId(), updateCommand
        );

        // then
        assertThat(updated.getScheduleRecordId()).isEqualTo(created.getScheduleRecordId());
        assertThat(updated.getTitle()).isEqualTo("수정된 제목");
        assertThat(updated.getStartDate()).isEqualTo(LocalDate.of(2026, 3, 22));
        assertThat(updated.getEndDate()).isEqualTo(LocalDate.of(2026, 3, 23));
        assertThat(updated.getNotificationType()).isEqualTo(NotificationType.ONE_DAY_BEFORE);
        assertThat(updated.getRepeatType()).isEqualTo(RepeatType.DAY);
        assertThat(updated.getRepeatEndsOn()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(updated.getLocation()).isEqualTo("수정된 위치");
        assertThat(updated.getColor()).isEqualTo(ScheduleColor.BLUE);
        assertThat(updated.getMemo()).isEqualTo("수정된 메모");
    }

    @Test
    @DisplayName("일정을 삭제할 수 있다")
    void deleteSchedule_Success() {
        // given
        CreateScheduleCommand command = new CreateScheduleCommand(
                "삭제할 일정",
                LocalDate.of(2026, 3, 21),
                LocalDate.of(2026, 3, 21),
                NotificationType.NONE,
                null,
                RepeatType.NONE,
                null,
                null,
                ScheduleColor.RED,
                null
        );
        ScheduleResponse created = scheduleRecordApplicationService.create(testUserId, command);

        // when
        scheduleRecordApplicationService.delete(testUserId, created.getScheduleRecordId());

        // then
        assertThatThrownBy(() ->
                scheduleRecordApplicationService.findById(testUserId, created.getScheduleRecordId()))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("일정을 단건 조회할 수 있다")
    void findScheduleById_Success() {
        // given
        CreateScheduleCommand command = new CreateScheduleCommand(
                "조회할 일정",
                LocalDate.of(2026, 3, 21),
                LocalDate.of(2026, 3, 21),
                NotificationType.NONE,
                null,
                RepeatType.NONE,
                null,
                null,
                ScheduleColor.GREEN,
                null
        );
        ScheduleResponse created = scheduleRecordApplicationService.create(testUserId, command);

        // when
        ScheduleResponse found = scheduleRecordApplicationService.findById(
                testUserId, created.getScheduleRecordId()
        );

        // then
        assertThat(found).isNotNull();
        assertThat(found.getScheduleRecordId()).isEqualTo(created.getScheduleRecordId());
        assertThat(found.getTitle()).isEqualTo("조회할 일정");
    }

    @Test
    @DisplayName("날짜 범위로 일정을 조회할 수 있다")
    void findSchedulesByDateRange_Success() {
        // given: 여러 일정 생성
        scheduleRecordApplicationService.create(testUserId, new CreateScheduleCommand(
                "3월 21일 일정",
                LocalDate.of(2026, 3, 21),
                LocalDate.of(2026, 3, 21),
                NotificationType.NONE, null, RepeatType.NONE, null,
                null, ScheduleColor.RED, null
        ));

        scheduleRecordApplicationService.create(testUserId, new CreateScheduleCommand(
                "3월 22-25일 일정",
                LocalDate.of(2026, 3, 22),
                LocalDate.of(2026, 3, 25),
                NotificationType.NONE, null, RepeatType.NONE, null,
                null, ScheduleColor.BLUE, null
        ));

        scheduleRecordApplicationService.create(testUserId, new CreateScheduleCommand(
                "4월 1일 일정",
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 1),
                NotificationType.NONE, null, RepeatType.NONE, null,
                null, ScheduleColor.GREEN, null
        ));

        // when: 3월 전체 조회
        List<ScheduleResponse> schedules = scheduleRecordApplicationService.findByDateRange(
                testUserId,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        );

        // then: 3월에 걸친 일정 2개만 조회됨
        assertThat(schedules).hasSize(2);
        assertThat(schedules).extracting(ScheduleResponse::getTitle)
                .containsExactlyInAnyOrder("3월 21일 일정", "3월 22-25일 일정");
    }

    @Test
    @DisplayName("여러 날에 걸친 일정이 범위에 포함되면 조회된다")
    void findSchedulesByDateRange_WithMultiDaySchedule_Success() {
        // given: 3월 20일 ~ 25일 일정 생성
        scheduleRecordApplicationService.create(testUserId, new CreateScheduleCommand(
                "긴 일정",
                LocalDate.of(2026, 3, 20),
                LocalDate.of(2026, 3, 25),
                NotificationType.NONE, null, RepeatType.NONE, null,
                null, ScheduleColor.PINK, null
        ));

        // when: 3월 22일~24일만 조회
        List<ScheduleResponse> schedules = scheduleRecordApplicationService.findByDateRange(
                testUserId,
                LocalDate.of(2026, 3, 22),
                LocalDate.of(2026, 3, 24)
        );

        // then: 범위에 걸쳐있으므로 조회됨
        assertThat(schedules).hasSize(1);
        assertThat(schedules.get(0).getTitle()).isEqualTo("긴 일정");
        assertThat(schedules.get(0).getStartDate()).isEqualTo(LocalDate.of(2026, 3, 20));
        assertThat(schedules.get(0).getEndDate()).isEqualTo(LocalDate.of(2026, 3, 25));
    }

    @Test
    @DisplayName("다른 사용자의 일정은 조회할 수 없다")
    void findScheduleById_WithDifferentUser_ThrowsException() {
        // given: 첫 번째 사용자의 일정 생성
        CreateScheduleCommand command = new CreateScheduleCommand(
                "내 일정",
                LocalDate.of(2026, 3, 21),
                LocalDate.of(2026, 3, 21),
                NotificationType.NONE, null, RepeatType.NONE, null,
                null, ScheduleColor.RED, null
        );
        ScheduleResponse created = scheduleRecordApplicationService.create(testUserId, command);

        // 두 번째 사용자 생성
        User anotherUser = new User(
                "다른유저",
                Email.of("another@example.com"),
                SocialType.KAKAO,
                "kakao-another-123"
        );
        User savedAnotherUser = userRepository.save(anotherUser);

        // when & then: 다른 사용자로 조회 시도
        assertThatThrownBy(() ->
                scheduleRecordApplicationService.findById(
                        savedAnotherUser.getId().getValue(),
                        created.getScheduleRecordId()
                )
        ).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("알림 없음에서 알림 생성으로 수정할 수 있다")
    void updateSchedule_FromNoNotificationToWithNotification_Success() {
        // given: 알림 없는 일정 생성
        CreateScheduleCommand createCommand = new CreateScheduleCommand(
                "알림 없는 일정",
                LocalDate.of(2026, 5, 20),
                LocalDate.of(2026, 5, 20),
                NotificationType.NONE,
                null,
                RepeatType.NONE,
                null,
                null,
                ScheduleColor.GREEN,
                null
        );
        ScheduleResponse created = scheduleRecordApplicationService.create(testUserId, createCommand);

        // when: CUSTOM 알림 추가
        UpdateScheduleCommand updateCommand = new UpdateScheduleCommand(
                "알림 추가된 일정",
                LocalDate.of(2026, 5, 20),
                LocalDate.of(2026, 5, 20),
                NotificationType.CUSTOM,
                14, // 오후 2시에 알림
                RepeatType.NONE,
                null,
                null,
                ScheduleColor.GREEN,
                null
        );
        ScheduleResponse updated = scheduleRecordApplicationService.update(
                testUserId, created.getScheduleRecordId(), updateCommand
        );

        // then: 알림이 올바르게 추가됨
        assertThat(updated.getNotificationType()).isEqualTo(NotificationType.CUSTOM);
        assertThat(updated.getNotificationCustomHours()).isEqualTo(14);
        assertThat(updated.getTitle()).isEqualTo("알림 추가된 일정");
    }

    @Test
    @DisplayName("알림 있음에서 알림 삭제로 수정할 수 있다")
    void updateSchedule_FromWithNotificationToNoNotification_Success() {
        // given: ONE_DAY_BEFORE 알림이 있는 일정 생성
        CreateScheduleCommand createCommand = new CreateScheduleCommand(
                "알림 있는 일정",
                LocalDate.of(2026, 5, 25),
                LocalDate.of(2026, 5, 25),
                NotificationType.ONE_DAY_BEFORE,
                null,
                RepeatType.NONE,
                null,
                "회의실 B",
                ScheduleColor.BLUE,
                "중요 회의"
        );
        ScheduleResponse created = scheduleRecordApplicationService.create(testUserId, createCommand);

        // when: 알림 삭제 (NONE으로 변경)
        UpdateScheduleCommand updateCommand = new UpdateScheduleCommand(
                "알림 삭제된 일정",
                LocalDate.of(2026, 5, 25),
                LocalDate.of(2026, 5, 25),
                NotificationType.NONE,
                null,
                RepeatType.NONE,
                null,
                "회의실 B",
                ScheduleColor.BLUE,
                "중요 회의"
        );
        ScheduleResponse updated = scheduleRecordApplicationService.update(
                testUserId, created.getScheduleRecordId(), updateCommand
        );

        // then: 알림이 올바르게 삭제됨
        assertThat(updated.getNotificationType()).isEqualTo(NotificationType.NONE);
        assertThat(updated.getNotificationCustomHours()).isNull();
        assertThat(updated.getTitle()).isEqualTo("알림 삭제된 일정");
    }

    @Test
    @DisplayName("알림 타입을 변경할 수 있다 (ONE_DAY_BEFORE → CUSTOM)")
    void updateSchedule_ChangeNotificationType_Success() {
        // given: ONE_DAY_BEFORE 알림이 있는 일정 생성
        CreateScheduleCommand createCommand = new CreateScheduleCommand(
                "알림 타입 변경 테스트",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 1),
                NotificationType.ONE_DAY_BEFORE,
                null,
                RepeatType.NONE,
                null,
                null,
                ScheduleColor.PINK,
                null
        );
        ScheduleResponse created = scheduleRecordApplicationService.create(testUserId, createCommand);

        // when: CUSTOM 알림으로 변경
        UpdateScheduleCommand updateCommand = new UpdateScheduleCommand(
                "알림 타입 변경 테스트",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 1),
                NotificationType.CUSTOM,
                7, // 오전 7시에 알림
                RepeatType.NONE,
                null,
                null,
                ScheduleColor.PINK,
                null
        );
        ScheduleResponse updated = scheduleRecordApplicationService.update(
                testUserId, created.getScheduleRecordId(), updateCommand
        );

        // then: 알림 타입이 올바르게 변경됨
        assertThat(updated.getNotificationType()).isEqualTo(NotificationType.CUSTOM);
        assertThat(updated.getNotificationCustomHours()).isEqualTo(7);
    }
}
