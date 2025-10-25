package com.recordmanagement.habitlog.domain.user.application.service;

import com.recordmanagement.habitlog.domain.user.application.dto.FcmTokenUpdateCommand;
import com.recordmanagement.habitlog.domain.user.application.dto.OnboardingCompletionCommand;
import com.recordmanagement.habitlog.domain.user.application.dto.UpdateProfileCommand;
import com.recordmanagement.habitlog.domain.user.application.dto.UserRegistrationCommand;
import com.recordmanagement.habitlog.domain.user.application.dto.UserResponse;
import com.recordmanagement.habitlog.domain.user.application.dto.UserWithdrawalCommand;
import com.recordmanagement.habitlog.domain.user.domain.model.SocialType;
import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
import com.recordmanagement.habitlog.domain.user.exception.UserException;
import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        return userRegistrationService.completeOnboarding(command);
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
        return UserResponse.from(restoredUser);
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