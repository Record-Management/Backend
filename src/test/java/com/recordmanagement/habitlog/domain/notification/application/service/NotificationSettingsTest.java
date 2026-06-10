package com.recordmanagement.habitlog.domain.notification.application.service;

import com.recordmanagement.habitlog.domain.notification.application.dto.NotificationSettingsCommand;
import com.recordmanagement.habitlog.domain.notification.application.dto.NotificationSettingsResponse;
import com.recordmanagement.habitlog.domain.notification.domain.repository.NotificationSettingsRepository;
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
 * 알림 설정 통합 테스트
 *
 * 테스트 대상:
 * - 알림 설정 조회 (기본값 확인)
 * - 알림 설정 업데이트 (개별 필드)
 * - 일정 알림 on/off 기능
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificationSettingsTest {

    @Autowired
    private NotificationApplicationService notificationApplicationService;

    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;

    @Autowired
    private UserRepository userRepository;

    private UserId testUserId;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = new User(
                "알림테스트유저",
                Email.of("notification-test@example.com"),
                SocialType.KAKAO,
                "kakao-notification-test-123"
        );

        User savedUser = userRepository.save(testUser);
        testUserId = savedUser.getId();
    }

    @Test
    @DisplayName("알림 설정 조회 시 기본값으로 모든 알림이 활성화되어야 한다")
    void getNotificationSettings_DefaultValues() {
        // when
        NotificationSettingsResponse response = notificationApplicationService.getNotificationSettings(testUserId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(testUserId.getValue());
        assertThat(response.isDailyRecordNotificationEnabled()).isTrue();
        assertThat(response.isExerciseNotificationEnabled()).isTrue();
        assertThat(response.isHabitNotificationEnabled()).isTrue();
        assertThat(response.isGoalSettingNotificationEnabled()).isTrue();
        assertThat(response.isScheduleNotificationEnabled()).isTrue(); // 일정 알림 기본값 확인
    }

    @Test
    @DisplayName("일정 알림만 비활성화할 수 있다")
    void updateNotificationSettings_DisableScheduleNotification() {
        // given
        NotificationSettingsCommand command = new NotificationSettingsCommand(
                testUserId,
                null, // dailyRecordNotificationEnabled
                null, // exerciseNotificationEnabled
                null, // habitNotificationEnabled
                null, // goalSettingNotificationEnabled
                false // scheduleNotificationEnabled만 변경
        );

        // when
        NotificationSettingsResponse response = notificationApplicationService.updateNotificationSettings(command);

        // then
        assertThat(response.isDailyRecordNotificationEnabled()).isTrue(); // 변경 안됨
        assertThat(response.isExerciseNotificationEnabled()).isTrue(); // 변경 안됨
        assertThat(response.isHabitNotificationEnabled()).isTrue(); // 변경 안됨
        assertThat(response.isGoalSettingNotificationEnabled()).isTrue(); // 변경 안됨
        assertThat(response.isScheduleNotificationEnabled()).isFalse(); // 변경됨
    }

    @Test
    @DisplayName("일정 알림만 활성화할 수 있다")
    void updateNotificationSettings_EnableScheduleNotification() {
        // given - 먼저 비활성화
        NotificationSettingsCommand disableCommand = new NotificationSettingsCommand(
                testUserId,
                null,
                null,
                null,
                null,
                false
        );
        notificationApplicationService.updateNotificationSettings(disableCommand);

        // when - 다시 활성화
        NotificationSettingsCommand enableCommand = new NotificationSettingsCommand(
                testUserId,
                null,
                null,
                null,
                null,
                true
        );
        NotificationSettingsResponse response = notificationApplicationService.updateNotificationSettings(enableCommand);

        // then
        assertThat(response.isScheduleNotificationEnabled()).isTrue();
    }

    @Test
    @DisplayName("모든 알림을 한 번에 업데이트할 수 있다")
    void updateNotificationSettings_AllFields() {
        // given
        NotificationSettingsCommand command = new NotificationSettingsCommand(
                testUserId,
                false, // dailyRecordNotificationEnabled
                false, // exerciseNotificationEnabled
                true,  // habitNotificationEnabled
                false, // goalSettingNotificationEnabled
                true   // scheduleNotificationEnabled
        );

        // when
        NotificationSettingsResponse response = notificationApplicationService.updateNotificationSettings(command);

        // then
        assertThat(response.isDailyRecordNotificationEnabled()).isFalse();
        assertThat(response.isExerciseNotificationEnabled()).isFalse();
        assertThat(response.isHabitNotificationEnabled()).isTrue();
        assertThat(response.isGoalSettingNotificationEnabled()).isFalse();
        assertThat(response.isScheduleNotificationEnabled()).isTrue();
    }

    @Test
    @DisplayName("일정 알림이 활성화되어 있는지 확인할 수 있다")
    void isScheduleNotificationEnabled_WhenEnabled() {
        // given
        NotificationSettingsCommand command = new NotificationSettingsCommand(
                testUserId,
                null,
                null,
                null,
                null,
                true
        );
        notificationApplicationService.updateNotificationSettings(command);

        // when
        NotificationSettingsResponse response = notificationApplicationService.getNotificationSettings(testUserId);

        // then
        assertThat(response.isScheduleNotificationEnabled()).isTrue();
    }

    @Test
    @DisplayName("일정 알림이 비활성화되어 있는지 확인할 수 있다")
    void isScheduleNotificationEnabled_WhenDisabled() {
        // given
        NotificationSettingsCommand command = new NotificationSettingsCommand(
                testUserId,
                null,
                null,
                null,
                null,
                false
        );
        notificationApplicationService.updateNotificationSettings(command);

        // when
        NotificationSettingsResponse response = notificationApplicationService.getNotificationSettings(testUserId);

        // then
        assertThat(response.isScheduleNotificationEnabled()).isFalse();
    }
}
