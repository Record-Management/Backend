package com.recordmanagement.habitlog.domain.user.infrastructure.entity;

import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.user.domain.model.Email;
import com.recordmanagement.habitlog.domain.user.domain.model.SocialType;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * 사용자 JPA 엔터티
 *
 * - 사용자 정보를 DB에 저장하기 위한 JPA 엔터티
 * - 도메인 모델과 DB 테이블 간 매핑 역할 수행
 * - ORM을 통한 데이터 영속성 관리 담당
 *
 * 테이블 컬럼 매핑:
 * - id: 사용자 고유 식별자 (PK)
 * - name: 사용자 이름/닉네임
 * - email: 이메일 주소
 * - social_type: 소셜 로그인 타입 (Enum 저장)
 * - social_id: 소셜 플랫폼 사용자 ID
 * - created_at: 생성 시간
 * - updated_at: 수정 시간
 *
 * 도메인 ↔ 엔터티 간 변환 지원
 *
 * @since 1.0.0
 * @version 1.1.0
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)  // JPA 사용을 위한 기본 생성자
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type", nullable = false)
    private SocialType socialType;

    @Column(name = "social_id", nullable = false)
    private String socialId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted;

    @Column(name = "nickname", length = 6)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "main_record_type")
    private RecordType mainRecordType;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "goal_days")
    private Integer goalDays;

    @Column(name = "notification_enabled")
    private Boolean notificationEnabled;

    @Column(name = "fcm_token", length = 1000)
    private String fcmToken;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deletion_scheduled_at")
    private LocalDateTime deletionScheduledAt;

    @Column(name = "withdrawal_reason", length = 500)
    private String withdrawalReason;

    /**
     * 도메인 모델(User)을 엔터티로 변환
     *
     * @param user 도메인 사용자 객체
     * @return 변환된 UserEntity 객체
     */
    public static UserEntity from(User user) {
        return UserEntity.builder()
                .id(user.getId().getValue())
                .name(user.getName())
                .email(user.getEmail() != null ? user.getEmail().getValue() : null)
                .socialType(user.getSocialType())
                .socialId(user.getSocialId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .onboardingCompleted(user.isOnboardingCompleted())
                .nickname(user.getNickname())
                .mainRecordType(user.getMainRecordType())
                .birthDate(user.getBirthDate())
                .goalDays(user.getGoalDays())
                .notificationEnabled(user.getNotificationEnabled())
                .fcmToken(null)  // FCM 토큰은 보안상 응답에 포함하지 않음
                .deletedAt(user.getDeletedAt())
                .deletionScheduledAt(user.getDeletionScheduledAt())
                .withdrawalReason(user.getWithdrawalReason())
                .build();
    }

    /**
     * JPA 엔터티를 도메인 모델(User)로 변환
     *
     * - 기본 생성자 호출 후 리플렉션으로 필드 주입
     * - 도메인 모델의 불변성 및 비즈니스 규칙 유지
     *
     * @return 변환된 도메인 User 객체
     */
    public User toDomain() {
        User user = new User(name, Email.of(email), socialType, socialId);
        setDomainFields(user);
        return user;
    }

    /**
     * 리플렉션을 사용하여 도메인 객체의 필드를 설정
     *
     * @param user 도메인 User 객체
     */
    private void setDomainFields(User user) {
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, UserId.of(this.id));

            var createdAtField = User.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(user, this.createdAt);

            var updatedAtField = User.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(user, this.updatedAt);

            var onboardingCompletedField = User.class.getDeclaredField("onboardingCompleted");
            onboardingCompletedField.setAccessible(true);
            onboardingCompletedField.set(user, this.onboardingCompleted);

            var nicknameField = User.class.getDeclaredField("nickname");
            nicknameField.setAccessible(true);
            nicknameField.set(user, this.nickname);

            var mainRecordTypeField = User.class.getDeclaredField("mainRecordType");
            mainRecordTypeField.setAccessible(true);
            mainRecordTypeField.set(user, this.mainRecordType);

            var birthDateField = User.class.getDeclaredField("birthDate");
            birthDateField.setAccessible(true);
            birthDateField.set(user, this.birthDate);

            var goalDaysField = User.class.getDeclaredField("goalDays");
            goalDaysField.setAccessible(true);
            goalDaysField.set(user, this.goalDays);

            var notificationEnabledField = User.class.getDeclaredField("notificationEnabled");
            notificationEnabledField.setAccessible(true);
            notificationEnabledField.set(user, this.notificationEnabled);

            // TODO: Firebase 설정 후 주석 해제
            // var fcmTokenField = User.class.getDeclaredField("fcmToken");
            // fcmTokenField.setAccessible(true);
            // fcmTokenField.set(user, this.fcmToken);

            var deletedAtField = User.class.getDeclaredField("deletedAt");
            deletedAtField.setAccessible(true);
            deletedAtField.set(user, this.deletedAt);

            var deletionScheduledAtField = User.class.getDeclaredField("deletionScheduledAt");
            deletionScheduledAtField.setAccessible(true);
            deletionScheduledAtField.set(user, this.deletionScheduledAt);

            var withdrawalReasonField = User.class.getDeclaredField("withdrawalReason");
            withdrawalReasonField.setAccessible(true);
            withdrawalReasonField.set(user, this.withdrawalReason);

        } catch (Exception e) {
            throw new RuntimeException("도메인 필드 설정 실패", e);
        }
    }
}
