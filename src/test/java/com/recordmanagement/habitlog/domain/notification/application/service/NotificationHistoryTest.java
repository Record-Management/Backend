package com.recordmanagement.habitlog.domain.notification.application.service;

import com.recordmanagement.habitlog.domain.notification.application.dto.NotificationHistoryResponse;
import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationHistory;
import com.recordmanagement.habitlog.domain.notification.domain.model.NotificationType;
import com.recordmanagement.habitlog.domain.notification.domain.repository.NotificationHistoryRepository;
import com.recordmanagement.habitlog.domain.user.domain.model.Email;
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

import static org.assertj.core.api.Assertions.*;

/**
 * 알림 히스토리 통합 테스트
 *
 * 테스트 대상:
 * - SCHEDULE_REMINDER 타입 처리
 * - 일정 알림 message가 일정명으로 표시되는지 확인
 * - 다른 알림 타입은 기존처럼 고정 메시지 사용
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationHistoryTest {

    @Autowired
    private NotificationHistoryRepository notificationHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    private UserId testUserId;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = new User(
                "히스토리테스트유저",
                Email.of("history-test@example.com"),
                SocialType.KAKAO,
                "kakao-history-test-456"
        );

        User savedUser = userRepository.save(testUser);
        testUserId = savedUser.getId();
    }

    @Test
    @DisplayName("일정 알림 히스토리는 message에 일정명이 표시되어야 한다")
    void scheduleNotificationHistory_ShowsScheduleNameInMessage() {
        // given
        String scheduleName = "한강 러닝가기";
        NotificationHistory scheduleNotification = new NotificationHistory(
                testUserId,
                NotificationType.SCHEDULE_REMINDER,
                "일정 기록",
                scheduleName // 일정명을 message로 저장
        );
        NotificationHistory saved = notificationHistoryRepository.save(scheduleNotification);

        // when
        NotificationHistoryResponse response = NotificationHistoryResponse.from(saved);

        // then
        assertThat(response.getType()).isEqualTo("SCHEDULE_REMINDER");
        assertThat(response.getTitle()).isEqualTo("일정 기록");
        assertThat(response.getMessage()).isEqualTo(scheduleName); // 일정명이 그대로 표시됨
        assertThat(response.isRead()).isFalse();
    }

    @Test
    @DisplayName("하루 기록 알림은 고정 메시지를 사용해야 한다")
    void dailyRecordNotificationHistory_ShowsFixedMessage() {
        // given
        NotificationHistory dailyNotification = new NotificationHistory(
                testUserId,
                NotificationType.DAILY_RECORD_REMINDER,
                "하루 기록",
                "어떤 메시지든 상관없음" // 이 값은 무시되고 고정 메시지가 사용됨
        );
        NotificationHistory saved = notificationHistoryRepository.save(dailyNotification);

        // when
        NotificationHistoryResponse response = NotificationHistoryResponse.from(saved);

        // then
        assertThat(response.getType()).isEqualTo("DAILY_RECORD_REMINDER");
        assertThat(response.getTitle()).isEqualTo("하루 기록");
        assertThat(response.getMessage()).isEqualTo("아직 '하루 기록'을 작성하지 않았어요. 하루의 작은 순간이 쌓이면 큰 변화가 돼요.");
    }

    @Test
    @DisplayName("운동 기록 알림은 고정 메시지를 사용해야 한다")
    void exerciseNotificationHistory_ShowsFixedMessage() {
        // given
        NotificationHistory exerciseNotification = new NotificationHistory(
                testUserId,
                NotificationType.EXERCISE_REMINDER,
                "운동 기록",
                "어떤 메시지든 상관없음"
        );
        NotificationHistory saved = notificationHistoryRepository.save(exerciseNotification);

        // when
        NotificationHistoryResponse response = NotificationHistoryResponse.from(saved);

        // then
        assertThat(response.getType()).isEqualTo("EXERCISE_REMINDER");
        assertThat(response.getTitle()).isEqualTo("운동 기록");
        assertThat(response.getMessage()).isEqualTo("아직 '운동 기록'을 작성하지 않았어요. 기록이 쌓일수록 습관이 되고, 어느새 운동이 자연스러워질 거예요.");
    }

    @Test
    @DisplayName("습관 기록 알림은 고정 메시지를 사용해야 한다")
    void habitNotificationHistory_ShowsFixedMessage() {
        // given
        NotificationHistory habitNotification = new NotificationHistory(
                testUserId,
                NotificationType.HABIT_REMINDER,
                "습관 기록",
                "어떤 메시지든 상관없음"
        );
        NotificationHistory saved = notificationHistoryRepository.save(habitNotification);

        // when
        NotificationHistoryResponse response = NotificationHistoryResponse.from(saved);

        // then
        assertThat(response.getType()).isEqualTo("HABIT_REMINDER");
        assertThat(response.getTitle()).isEqualTo("습관 기록");
        assertThat(response.getMessage()).isEqualTo("아직 '습관 기록'을 작성하지 않았어요. 꾸준히 쌓이는 하루가 큰 변화를 만들 수 있어요.");
    }

    @Test
    @DisplayName("목표 설정 알림은 고정 메시지를 사용해야 한다")
    void goalSettingNotificationHistory_ShowsFixedMessage() {
        // given
        NotificationHistory goalNotification = new NotificationHistory(
                testUserId,
                NotificationType.GOAL_SETTING_REMINDER,
                "목표 설정",
                "어떤 메시지든 상관없음"
        );
        NotificationHistory saved = notificationHistoryRepository.save(goalNotification);

        // when
        NotificationHistoryResponse response = NotificationHistoryResponse.from(saved);

        // then
        assertThat(response.getType()).isEqualTo("GOAL_SETTING_REMINDER");
        assertThat(response.getTitle()).isEqualTo("목표 설정");
        assertThat(response.getMessage()).isEqualTo("아직 목표를 설정하지 않으셨어요! 지금부터 새로운 목표를 만들어볼까요?");
    }

    @Test
    @DisplayName("여러 타입의 알림 히스토리를 올바르게 변환해야 한다")
    void multipleNotificationTypes_ConvertedCorrectly() {
        // given
        NotificationHistory scheduleNotif = new NotificationHistory(
                testUserId,
                NotificationType.SCHEDULE_REMINDER,
                "일정 기록",
                "요가 수업 참석"
        );
        NotificationHistory dailyNotif = new NotificationHistory(
                testUserId,
                NotificationType.DAILY_RECORD_REMINDER,
                "하루 기록",
                "테스트 메시지"
        );
        NotificationHistory habitNotif = new NotificationHistory(
                testUserId,
                NotificationType.HABIT_REMINDER,
                "습관 기록",
                "테스트 메시지"
        );

        notificationHistoryRepository.save(scheduleNotif);
        notificationHistoryRepository.save(dailyNotif);
        notificationHistoryRepository.save(habitNotif);

        // when
        NotificationHistoryResponse scheduleResponse = NotificationHistoryResponse.from(scheduleNotif);
        NotificationHistoryResponse dailyResponse = NotificationHistoryResponse.from(dailyNotif);
        NotificationHistoryResponse habitResponse = NotificationHistoryResponse.from(habitNotif);

        // then
        // 일정 알림은 일정명 사용
        assertThat(scheduleResponse.getMessage()).isEqualTo("요가 수업 참석");

        // 다른 알림들은 고정 메시지 사용
        assertThat(dailyResponse.getMessage()).contains("하루 기록");
        assertThat(habitResponse.getMessage()).contains("습관 기록");
    }

    @Test
    @DisplayName("일정 알림의 다양한 일정명을 올바르게 표시해야 한다")
    void scheduleNotification_VariousScheduleNames() {
        // given
        String[] scheduleNames = {
                "한강 러닝가기",
                "회의 참석",
                "생일 파티",
                "병원 예약",
                "프로젝트 마감"
        };

        // when & then
        for (String scheduleName : scheduleNames) {
            NotificationHistory notification = new NotificationHistory(
                    testUserId,
                    NotificationType.SCHEDULE_REMINDER,
                    "일정 기록",
                    scheduleName
            );
            NotificationHistory saved = notificationHistoryRepository.save(notification);
            NotificationHistoryResponse response = NotificationHistoryResponse.from(saved);

            assertThat(response.getMessage()).isEqualTo(scheduleName);
            assertThat(response.getTitle()).isEqualTo("일정 기록");
            assertThat(response.getType()).isEqualTo("SCHEDULE_REMINDER");
        }
    }
}
