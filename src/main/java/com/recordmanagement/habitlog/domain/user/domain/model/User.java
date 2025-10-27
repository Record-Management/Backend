package com.recordmanagement.habitlog.domain.user.domain.model;

import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 사용자 도메인 핵심 엔터티 (Aggregate Root)
 *
 * - 사용자 식별, 기본 정보 및 소셜 로그인 관련 데이터 보유
 * - 도메인 내 사용자 관련 비즈니스 로직 캡슐화
 * - JPA 엔티티로 활용 가능하며 생성 및 변경 시점 관리
 *
 * 주요 필드:
 * - id: 사용자 고유 식별자 (UserId 타입, 생성 시 자동 할당)
 * - name: 사용자 이름 또는 닉네임
 * - email: Email 값 객체로 이메일 형식 검증 포함
 * - socialType: 로그인 소셜 플랫폼 타입 (카카오, 애플 등)
 * - socialId: 소셜 플랫폼 내 고유 사용자 식별자
 * - createdAt: 계정 생성 시간
 * - updatedAt: 마지막 프로필 수정 시간
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA 기본 생성자 용도, 직접 호출 금지
@Schema(description = "사용자 도메인 핵심 엔터티")
public class User {

    @Schema(description = "사용자 고유 식별자", example = "user-1234-uuid")
    private UserId id;

    @Schema(description = "사용자 이름 또는 닉네임", example = "홍길동")
    private String name;

    @Schema(description = "사용자 이메일 정보")
    private Email email;

    @Schema(description = "사용자 로그인 소셜 플랫폼 타입", example = "kakao")
    private SocialType socialType;

    @Schema(description = "소셜 플랫폼 고유 사용자 식별자", example = "1234567890")
    private String socialId;

    @Schema(description = "계정 생성 시간", example = "2025-07-30T12:34:56")
    private LocalDateTime createdAt;

    @Schema(description = "마지막 프로필 수정 시간", example = "2025-07-31T09:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "온보딩 완료 여부", example = "false")
    private boolean onboardingCompleted;

    @Schema(description = "사용자 닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "메인 기록 타입")
    private RecordType mainRecordType;

    @Schema(description = "생년월일", example = "1998-06-02")
    private LocalDate birthDate;

    @Schema(description = "목표 일수", example = "20")
    private Integer goalDays;

    @Schema(description = "FCM 토큰 (푸시 알림용)", example = "dGhpcyBpcyBhIGZha2UgZmNtIHRva2VuLi4u")
    private String fcmToken;

    @Schema(description = "회원 탈퇴 요청 시간 (soft delete)", example = "2025-07-31T09:00:00")
    private LocalDateTime deletedAt;

    @Schema(description = "실제 삭제 예정 시간 (탈퇴일 + 7일)", example = "2025-08-07T09:00:00")
    private LocalDateTime deletionScheduledAt;

    @Schema(description = "회원 탈퇴 사유", example = "서비스 불만족")
    private String withdrawalReason;


    /**
     * 신규 사용자 생성 생성자
     *
     * - UserId는 도메인 내에서 생성 (UUID 등)
     * - createdAt과 updatedAt은 생성 시 현재 시간으로 자동 세팅
     *
     * @param name 사용자 이름
     * @param email 이메일 값 객체
     * @param socialType 소셜 로그인 플랫폼 타입
     * @param socialId 소셜 플랫폼 내 고유 사용자 ID
     */
    public User(String name, Email email, SocialType socialType, String socialId) {
        this.id = UserId.generate();
        this.name = name;
        this.email = email;
        this.socialType = socialType;
        this.socialId = socialId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.onboardingCompleted = false;
    }

    /**
     * 사용자 프로필 정보 수정
     *
     * - 이름과 이메일 변경 가능
     * - 변경 시 updatedAt 자동 갱신
     *
     * @param name 변경할 이름
     * @param email 변경할 이메일 (Email VO)
     */
    public void updateProfile(String name, Email email) {
        this.name = name;
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 온보딩 완료 처리
     *
     * - 온보딩 완료 상태를 true로 변경
     * - 모든 온보딩 데이터 한번에 설정
     * - updatedAt 자동 갱신
     * - 이미 온보딩이 완료된 경우 예외 발생
     */
    public void completeOnboarding(String nickname, RecordType mainRecordType, LocalDate birthDate, Integer goalDays) {
        if (this.onboardingCompleted) {
            throw new CustomException(ErrorCode.USER_ONBOARDING_ALREADY_COMPLETED);
        }
        
        this.nickname = nickname;  // name이 아닌 nickname 필드에 저장
        this.mainRecordType = mainRecordType;
        this.birthDate = birthDate;
        this.goalDays = goalDays;
        this.onboardingCompleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 소셜 ID 업데이트
     * Apple 재로그인 시 새로운 sub로 업데이트하기 위함
     * 기존 User 객체를 복사하되 socialId만 변경하여 새 객체 생성
     *
     * @param newSocialId 새로운 소셜 ID
     * @return 업데이트된 User 객체 (불변성 유지)
     */
    public User updateSocialId(String newSocialId) {
        User newUser = new User(this.name, this.email, this.socialType, newSocialId);
        
        // 기존 필드들을 리플렉션으로 복사
        try {
            setFieldValue(newUser, "id", this.id);
            setFieldValue(newUser, "nickname", this.nickname);
            setFieldValue(newUser, "mainRecordType", this.mainRecordType);
            setFieldValue(newUser, "birthDate", this.birthDate);
            setFieldValue(newUser, "goalDays", this.goalDays);
            setFieldValue(newUser, "fcmToken", this.fcmToken);
            setFieldValue(newUser, "onboardingCompleted", this.onboardingCompleted);
            setFieldValue(newUser, "createdAt", this.createdAt);
            setFieldValue(newUser, "updatedAt", LocalDateTime.now());  // 업데이트 시간 갱신
            setFieldValue(newUser, "deletedAt", this.deletedAt);
            setFieldValue(newUser, "deletionScheduledAt", this.deletionScheduledAt);
            setFieldValue(newUser, "withdrawalReason", this.withdrawalReason);
        } catch (Exception e) {
            throw new RuntimeException("socialId 업데이트 중 필드 복사 실패", e);
        }
        
        return newUser;
    }

    /**
     * 리플렉션을 사용하여 필드 값 설정
     */
    private void setFieldValue(Object target, String fieldName, Object value) throws Exception {
        var field = User.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * FCM 토큰 업데이트
     *
     * - 푸시 알림을 위한 FCM 토큰 설정
     * - updatedAt 자동 갱신
     *
     * @param fcmToken FCM 토큰
     */
    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 사용자 프로필 업데이트 (닉네임, 생년월일)
     *
     * - 닉네임과 생년월일 선택적 변경 가능
     * - null이 아닌 값만 업데이트
     * - 변경 시 updatedAt 자동 갱신
     *
     * @param nickname 변경할 닉네임 (null이면 변경하지 않음)
     * @param birthDate 변경할 생년월일 (null이면 변경하지 않음)
     */
    public void updateUserProfile(String nickname, LocalDate birthDate) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (birthDate != null) {
            this.birthDate = birthDate;
        }
        this.updatedAt = LocalDateTime.now();
    }


    /**
     * 회원 탈퇴 처리 (soft delete)
     * 
     * - 즉시 삭제하지 않고 탈퇴 표시만 함
     * - 7일 후 자동 삭제 예정 시간 설정
     * - 탈퇴 사유 저장
     * 
     * @param reason 탈퇴 사유
     */
    public void markAsWithdrawn(String reason) {
        LocalDateTime now = LocalDateTime.now();
        this.deletedAt = now;
        this.deletionScheduledAt = now.plusDays(7);
        this.withdrawalReason = reason;
        this.updatedAt = now;
    }

    /**
     * 회원 탈퇴 복구 (재가입)
     * 
     * - 7일 이내 재가입 시 탈퇴 상태 해제
     * - 탈퇴 관련 필드 초기화
     */
    public void restoreFromWithdrawal() {
        if (!isWithdrawn()) {
            throw new CustomException(ErrorCode.USER_NOT_WITHDRAWN);
        }
        
        if (isPermanentlyDeleted()) {
            throw new CustomException(ErrorCode.USER_PERMANENTLY_DELETED);
        }
        
        this.deletedAt = null;
        this.deletionScheduledAt = null;
        this.withdrawalReason = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 탈퇴 상태 확인
     * 
     * @return true 탈퇴한 사용자, false 활성 사용자
     */
    public boolean isWithdrawn() {
        return this.deletedAt != null;
    }

    /**
     * 영구 삭제 대상 확인
     * 
     * @return true 7일 경과로 영구 삭제 대상, false 복구 가능
     */
    public boolean isPermanentlyDeleted() {
        return this.deletionScheduledAt != null && 
               LocalDateTime.now().isAfter(this.deletionScheduledAt);
    }

    /**
     * 복구 가능 여부 확인
     * 
     * @return true 복구 가능, false 복구 불가능
     */
    public boolean canBeRestored() {
        return isWithdrawn() && !isPermanentlyDeleted();
    }

    /**
     * 동일 사용자 여부 확인
     *
     * - 비교 기준은 UserId 값
     *
     * @param userId 비교할 사용자 ID
     * @return true 동일 사용자, false 다른 사용자
     */
    public boolean isSameUser(UserId userId) {
        return this.id.equals(userId);
    }
}