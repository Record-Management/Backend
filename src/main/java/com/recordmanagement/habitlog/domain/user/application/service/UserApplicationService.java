package com.recordmanagement.habitlog.domain.user.application.service;

import com.recordmanagement.habitlog.domain.user.application.dto.FcmTokenUpdateCommand;
import com.recordmanagement.habitlog.domain.user.application.dto.GoalResetCommand;
import com.recordmanagement.habitlog.domain.user.application.dto.OnboardingCompletionCommand;
import com.recordmanagement.habitlog.domain.user.application.dto.UpdateProfileCommand;
import com.recordmanagement.habitlog.domain.user.application.dto.UserRegistrationCommand;
import com.recordmanagement.habitlog.domain.user.application.dto.UserResponse;
import com.recordmanagement.habitlog.domain.user.application.dto.UserWithdrawalCommand;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.domain.user.domain.model.SocialType;
import com.recordmanagement.habitlog.domain.goal.domain.model.TreeStage;
import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
import com.recordmanagement.habitlog.domain.user.exception.UserException;
import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 사용자 애플리케이션 서비스 (Facade 패턴 적용)
 * 
 * SRP 개선: 여러 전문화된 서비스들을 조합하는 Facade 역할
 * - UserRegistrationService: 사용자 등록 및 온보딩
 * - UserProfileService: 프로필 및 FCM 토큰 관리  
 * - UserLifecycleService: 회원탈퇴 및 복구
 * 
 * 기존 API 호환성을 유지하면서 내부적으로는 분리된 서비스들에 위임
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 3.0.0 (SRP 적용)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserApplicationService {

    // SRP 적용: 전문화된 서비스들에 위임
    private final UserRegistrationService userRegistrationService;
    private final UserProfileService userProfileService;
    private final UserLifecycleService userLifecycleService;
    
    // 직접 사용하는 의존성 (Apple Transfer Sub 등 특수 기능용)
    private final UserRepository userRepository;
    private final ApplicationContext applicationContext;

    // ============ 사용자 등록 관련 (UserRegistrationService에 위임) ============

    /**
     * 신규 사용자 등록 처리
     */
    public UserResponse registerUser(UserRegistrationCommand command) {
        return userRegistrationService.registerUser(command);
    }

    /**
     * 소셜 로그인용 사용자 등록
     */
    public UserResponse registerUserForSocialLogin(UserRegistrationCommand command) {
        return userRegistrationService.registerUserForSocialLogin(command);
    }

    /**
     * 온보딩 완료 처리
     */
    public UserResponse completeOnboarding(OnboardingCompletionCommand command) {
        log.info("온보딩 완료 처리: userId=[{}], mainRecordType=[{}], goalDays=[{}]", 
                command.userId(), command.mainRecordType(), command.goalDays());

        // UserRegistrationService에 온보딩 완료 위임
        UserResponse result = userRegistrationService.completeOnboarding(command);
        
        // 메인 기록 타입이 HABIT로 설정되어도 자동 생성하지 않음 - 일상/운동과 동일한 UX 제공
        if (command.mainRecordType() == RecordType.HABIT) {
            log.info("온보딩 완료로 메인 기록 타입이 HABIT로 설정됨. 사용자 실제 행동 시에만 기록 생성: userId=[{}]", 
                    command.userId());
        }
        
        // 온보딩 완료 시 새로운 목표 생성
        try {
            var goalApplicationService = applicationContext.getBean("goalApplicationService", 
                com.recordmanagement.habitlog.domain.goal.application.service.GoalApplicationService.class);
            goalApplicationService.createGoal(
                UserId.of(command.userId()),
                command.mainRecordType(),
                command.goalDays()
            );
            log.info("온보딩 완료로 새로운 목표 생성 완료: userId=[{}], recordType=[{}], goalDays=[{}]", 
                    command.userId(), command.mainRecordType(), command.goalDays());
        } catch (Exception e) {
            log.error("온보딩 완료 후 목표 생성 중 오류 발생: userId=[{}], error={}", 
                    command.userId(), e.getMessage(), e);
            // 목표 생성 실패가 온보딩 완료 자체를 실패시키지 않도록 함
        }
        
        return result;
    }

    /**
     * 온보딩 재설정 처리
     */
    public UserResponse resetOnboarding(OnboardingCompletionCommand command) {
        log.info("온보딩 재설정 처리: userId=[{}], mainRecordType=[{}], goalDays=[{}]", 
                command.userId(), command.mainRecordType(), command.goalDays());

        // 기존 사용자 정보 조회하여 기존 메인 기록 타입 확인
        User existingUser = userRepository.findById(UserId.of(command.userId()))
                .orElseThrow(() -> UserException.notFound(command.userId()));
        RecordType previousMainRecordType = existingUser.getMainRecordType();
        
        // UserRegistrationService에 온보딩 재설정 위임
        UserResponse result = userRegistrationService.resetOnboarding(command);
        
        // 메인 기록 타입이 HABIT로 변경되어도 자동 생성하지 않음 - 일상/운동과 동일한 UX 제공
        if (command.mainRecordType() == RecordType.HABIT && previousMainRecordType != RecordType.HABIT) {
            log.info("온보딩 재설정으로 메인 기록 타입이 HABIT로 변경됨. 사용자 실제 행동 시에만 기록 생성: userId=[{}]", 
                    command.userId());
        }
        
        return result;
    }

    /**
     * 목표 재설정 처리 (간소화 버전)
     * 기록 타입과 목표 일수만 변경
     */
    public UserResponse resetGoal(GoalResetCommand command) {
        log.info("목표 재설정 처리: userId=[{}], mainRecordType=[{}], goalDays=[{}]", 
                command.userId(), command.mainRecordType(), command.goalDays());

        User user = userRepository.findById(UserId.of(command.userId()))
                .orElseThrow(() -> UserException.notFound(command.userId()));

        // 기존 메인 기록 타입 저장
        RecordType previousMainRecordType = user.getMainRecordType();
        
        user.resetGoal(command.mainRecordType(), command.goalDays());
        User updatedUser = userRepository.save(user);
        
        // 메인 기록 타입이 HABIT로 변경되어도 자동 생성하지 않음 - 일상/운동과 동일한 UX 제공
        if (command.mainRecordType() == RecordType.HABIT && previousMainRecordType != RecordType.HABIT) {
            log.info("목표 재설정으로 메인 기록 타입이 HABIT로 변경됨. 사용자 실제 행동 시에만 기록 생성: userId=[{}]", 
                    updatedUser.getId().getValue());
        }
        
        log.info("목표 재설정 완료: userId=[{}]", updatedUser.getId().getValue());
        // 목표 재설정 시 목표가 새로 설정되므로 currentTreeStage = STAGE_1 (초기 단계)
        return UserResponse.from(updatedUser, TreeStage.STAGE_1);
    }

    /**
     * 소셜 로그인 사용자 조회
     */
    @Transactional(readOnly = true)
    public Optional<UserResponse> findBySocialLogin(SocialType socialType, String socialId) {
        return userRegistrationService.findBySocialLogin(socialType, socialId);
    }

    /**
     * 이메일과 소셜 타입으로 사용자 조회
     */
    @Transactional(readOnly = true)
    public Optional<UserResponse> findByEmailAndSocialType(String email, SocialType socialType) {
        return userRegistrationService.findByEmailAndSocialType(email, socialType);
    }

    // ============ 사용자 프로필 관리 (UserProfileService에 위임) ============

    /**
     * 사용자 ID로 사용자 조회
     */
    @Transactional(readOnly = true)
    public Optional<UserResponse> findById(String userId) {
        return userProfileService.findById(userId);
    }

    /**
     * 토큰 갱신용 사용자 조회
     */
    @Transactional(readOnly = true)
    public Optional<UserResponse> findByIdForRefreshToken(String userId) {
        return userProfileService.findByIdForRefreshToken(userId);
    }

    /**
     * 사용자 프로필 업데이트
     */
    public UserResponse updateProfile(UpdateProfileCommand command) {
        return userProfileService.updateProfile(command);
    }

    /**
     * FCM 토큰 업데이트
     */
    public UserResponse updateFcmToken(FcmTokenUpdateCommand command) {
        return userProfileService.updateFcmToken(command);
    }

    /**
     * FCM 토큰 삭제
     */
    public void deleteFcmToken(String userId) {
        log.info("FCM 토큰 삭제: userId={}", userId);
        
        // FCM 토큰을 null로 설정하여 삭제
        FcmTokenUpdateCommand command = new FcmTokenUpdateCommand(UserId.of(userId), null);
        userProfileService.updateFcmToken(command);
        
        log.info("FCM 토큰 삭제 완료: userId={}", userId);
    }


    /**
     * 사용자 소유권 검증
     */
    @Transactional(readOnly = true)
    public void validateUserOwnership(String requestUserId, String targetUserId) {
        userProfileService.validateUserOwnership(requestUserId, targetUserId);
    }

    // ============ 사용자 생명주기 관리 (UserLifecycleService에 위임) ============

    /**
     * 회원탈퇴 처리
     */
    public UserResponse withdrawUser(UserWithdrawalCommand command) {
        return userLifecycleService.withdrawUser(command);
    }

    /**
     * 탈퇴 사용자 복구 처리
     */
    public Optional<UserResponse> restoreWithdrawnUser(SocialType socialType, String socialId) {
        return userLifecycleService.restoreWithdrawnUser(socialType, socialId);
    }

    /**
     * 사용자의 모든 리프레시 토큰 삭제
     */
    public void revokeAllTokens(String userId) {
        userLifecycleService.revokeAllTokens(userId);
    }

    // ============ 특수 기능 (직접 처리) ============

    /**
     * 기존 사용자의 socialId 업데이트
     * Apple Transfer Sub 처리용
     */
    public UserResponse updateUserSocialId(String userId, String newSocialId) {
        log.info("사용자 socialId 업데이트: userId={}, newSocialId={}", 
                userId, maskSocialId(newSocialId));

        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> UserException.notFound(userId));
        
        user.updateSocialId(newSocialId);
        User savedUser = userRepository.save(user);
        
        log.info("사용자 socialId 업데이트 완료: userId={}", userId);
        return UserResponse.forSocialLogin(savedUser);
    }

    /**
     * 탈퇴 사용자 상태 확인
     */
    @Transactional(readOnly = true)
    public boolean isUserWithdrawn(String userId) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> UserException.notFound(userId));
        return user.isWithdrawn();
    }

    /**
     * 탈퇴 사용자 복구 가능 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean canRestoreUser(String userId) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> UserException.notFound(userId));
        return user.canBeRestored();
    }

    /**
     * 탈퇴 사용자 복구 처리 (userId 기반)
     */
    public UserResponse restoreWithdrawnUser(String userId) {
        log.info("탈퇴 사용자 복구: userId={}", userId);

        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> UserException.notFound(userId));
        
        if (!user.isWithdrawn()) {
            throw new CustomException(ErrorCode.USER_NOT_WITHDRAWN);
        }

        if (!user.canBeRestored()) {
            throw new CustomException(ErrorCode.USER_PERMANENTLY_DELETED);
        }

        user.restoreFromWithdrawal();
        User restoredUser = userRepository.save(user);
        
        log.info("탈퇴 사용자 복구 완료: userId={}", userId);
        // 탈퇴 복구 시에는 아직 목표가 없으므로 currentTreeStage = null
        return UserResponse.from(restoredUser, null);
    }

    /**
     * 소셜 ID 마스킹 (로깅용)
     */
    private String maskSocialId(String socialId) {
        if (socialId == null || socialId.length() <= 6) {
            return "****";
        }
        return socialId.substring(0, 3) + "****" + socialId.substring(socialId.length() - 3);
    }
}