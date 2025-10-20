package com.recordmanagement.habitlog.application.user;

import com.recordmanagement.habitlog.application.user.dto.UserRegistrationCommand;
import com.recordmanagement.habitlog.application.user.dto.UserResponse;
import com.recordmanagement.habitlog.application.user.dto.UserWithdrawalCommand;
import com.recordmanagement.habitlog.application.user.dto.OnboardingCompletionCommand;
import com.recordmanagement.habitlog.application.user.dto.FcmTokenUpdateCommand;
import com.recordmanagement.habitlog.config.exception.CustomException;
import com.recordmanagement.habitlog.config.exception.ErrorCode;
import com.recordmanagement.habitlog.domain.auth.repository.RefreshTokenRepository;
import com.recordmanagement.habitlog.domain.auth.service.SocialUnlinkService;
import com.recordmanagement.habitlog.domain.user.model.User;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import com.recordmanagement.habitlog.domain.user.model.SocialType;
import com.recordmanagement.habitlog.domain.user.repository.UserRepository;
import com.recordmanagement.habitlog.domain.user.service.UserDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 사용자 애플리케이션 서비스
 * 사용자 관련 Use Case 구현 및 도메인 서비스 조합
 * 사용자 등록, 소셜 로그인 사용자 조회, ID 기반 사용자 조회, 회원탈퇴 처리
 * 트랜잭션 경계 설정 및 데이터 일관성 보장
 *
 * @author 전우선
 * @since 2025.09.04
 * @version 2.1.0
 */
@Slf4j
@Service
@Transactional
public class UserApplicationService {

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final SocialUnlinkService socialUnlinkService;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserApplicationService(UserRepository userRepository, 
                                UserDomainService userDomainService,
                                SocialUnlinkService socialUnlinkService,
                                RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.userDomainService = userDomainService;
        this.socialUnlinkService = socialUnlinkService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * 신규 사용자 등록 처리
     * 도메인 서비스에 위임하여 User 엔터티 생성 후 저장
     * 저장된 도메인 모델을 UserResponse DTO로 변환하여 반환
     *
     * @param command 사용자 등록 커맨드 객체
     * @return 등록 완료된 사용자 정보 DTO
     */
    public UserResponse registerUser(UserRegistrationCommand command) {
        User user = userDomainService.createNewUser(
                command.getName(),
                command.getEmail(),
                command.getSocialType(),
                command.getSocialId()
        );

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    /**
     * 소셜 로그인용 사용자 등록
     * 기본 정보만 포함한 UserResponse 반환
     *
     * @param command 사용자 등록 커맨드 객체
     * @return 등록 완료된 사용자 정보 DTO (소셜로그인용)
     */
    public UserResponse registerUserForSocialLogin(UserRegistrationCommand command) {
        User user = userDomainService.createNewUser(
                command.getName(),
                command.getEmail(),
                command.getSocialType(),
                command.getSocialId()
        );

        User savedUser = userRepository.save(user);
        return UserResponse.forSocialLogin(savedUser);
    }

    /**
     * 소셜 로그인 사용자 조회
     * 소셜 타입과 소셜 ID 조합으로 사용자 검색
     * 조회 결과를 UserResponse DTO로 변환하여 반환
     * 읽기 전용 트랜잭션 적용
     *
     * @param socialType 소셜 로그인 플랫폼 타입
     * @param socialId 소셜 플랫폼 고유 사용자 ID
     * @return 사용자 정보 Optional
     */
    @Transactional(readOnly = true)
    public Optional<UserResponse> findBySocialLogin(SocialType socialType, String socialId) {
        return userRepository.findBySocialTypeAndSocialId(socialType, socialId)
                .map(UserResponse::forSocialLogin);
    }

    /**
     * 사용자 ID로 사용자 조회
     * UserId VO로 변환 후 Repository 조회
     * 조회 결과를 UserResponse DTO로 변환하여 반환
     * 읽기 전용 트랜잭션 적용
     *
     * @param userId 사용자 고유 ID 문자열
     * @return 사용자 정보 Optional
     */
    @Transactional(readOnly = true)
    public Optional<UserResponse> findById(String userId) {
        return userRepository.findById(UserId.of(userId))
                .map(UserResponse::from);
    }

    /**
     * 토큰 갱신용 사용자 조회
     * 프론트엔드 호환성을 위해 최소 필드만 포함
     *
     * @param userId 사용자 ID
     * @return 사용자 정보 Optional (토큰 갱신용)
     */
    @Transactional(readOnly = true)
    public Optional<UserResponse> findByIdForRefreshToken(String userId) {
        return userRepository.findById(UserId.of(userId))
                .map(UserResponse::forRefreshToken);
    }

    /**
     * 이메일과 소셜 타입으로 사용자 조회
     * Apple 재로그인 시 기존 사용자 찾기용
     *
     * @param email 사용자 이메일
     * @param socialType 소셜 타입
     * @return 사용자 정보 Optional
     */
    @Transactional(readOnly = true)
    public Optional<UserResponse> findByEmailAndSocialType(String email, SocialType socialType) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return userRepository.findByEmailAndSocialType(email.trim().toLowerCase(), socialType)
                .map(UserResponse::forSocialLogin);
    }

    /**
     * 기존 사용자의 socialId 업데이트
     * Apple 재로그인 시 새로운 sub로 업데이트
     *
     * @param userId 사용자 ID
     * @param newSocialId 새로운 소셜 ID
     * @return 업데이트된 사용자 정보
     */
    @Transactional
    public UserResponse updateUserSocialId(String userId, String newSocialId) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        log.info("사용자 socialId 업데이트: userId={}, oldSocialId={}, newSocialId={}", 
                userId, user.getSocialId(), newSocialId);
        
        User updatedUser = user.updateSocialId(newSocialId);
        User savedUser = userRepository.save(updatedUser);
        
        log.info("사용자 socialId 업데이트 완료: userId={}", userId);
        return UserResponse.forSocialLogin(savedUser);
    }

    /**
     * 탈퇴 사용자 상태 확인
     * 
     * @param userId 사용자 ID
     * @return true 탈퇴한 사용자, false 활성 사용자
     */
    @Transactional(readOnly = true)
    public boolean isUserWithdrawn(String userId) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return user.isWithdrawn();
    }

    /**
     * 탈퇴 사용자 복구 가능 여부 확인
     * 
     * @param userId 사용자 ID
     * @return true 복구 가능, false 복구 불가능
     */
    @Transactional(readOnly = true)
    public boolean canRestoreUser(String userId) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return user.canBeRestored();
    }

    /**
     * 탈퇴 사용자 복구 처리
     * 
     * @param userId 사용자 ID
     * @return 복구된 사용자 정보
     */
    @Transactional
    public UserResponse restoreWithdrawnUser(String userId) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        user.restoreFromWithdrawal();
        User restoredUser = userRepository.save(user);
        
        log.info("사용자 탈퇴 복구 완료: userId={}", userId);
        return UserResponse.from(restoredUser);
    }

    /**
     * 회원탈퇴 처리 (soft delete)
     * 즉시 삭제하지 않고 7일 후 자동 삭제 예약
     * 
     * @param command 회원탈퇴 커맨드
     */
    public void withdrawUser(UserWithdrawalCommand command) {
        log.info("회원탈퇴 처리 시작 (soft delete)");
        
        // 1. 사용자 조회
        User user = userRepository.findById(UserId.of(command.getUserId()))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 2. 이미 탈퇴한 사용자인지 확인
        if (user.isWithdrawn()) {
            throw new CustomException(ErrorCode.USER_NOT_WITHDRAWN);
        }
        
        try {
            // 3. 소셜 플랫폼 연결 해제 (실패해도 계속 진행)
            socialUnlinkService.unlinkSocialConnection(
                    user.getSocialType(), 
                    user.getSocialId()
            );
        } catch (Exception e) {
            log.warn("소셜 연결 해제 실패, 계속 진행: error={}", e.getMessage());
        }
        
        try {
            // 4. soft delete 처리 (7일 후 자동 삭제 예약)
            user.markAsWithdrawn(command.getReason());
            userRepository.save(user);
            
            // 5. 관련 토큰 삭제 (즉시 로그아웃 처리)
            refreshTokenRepository.deleteByUserId(user.getId().getValue());
            
            log.info("회원탈퇴 완료 (soft delete): reason={}, deletionScheduledAt={}", 
                    command.getReason(), user.getDeletionScheduledAt());
                    
        } catch (Exception e) {
            log.error("회원탈퇴 실패: error={}", e.getMessage());
            throw new CustomException(ErrorCode.USER_WITHDRAWAL_FAILED);
        }
    }

    /**
     * 사용자 관련 데이터 삭제
     * RefreshToken 등 사용자와 연관된 모든 데이터를 정리
     * 
     * @param user 삭제할 사용자
     */
    private void deleteUserRelatedData(User user) {
        // RefreshToken 삭제
        refreshTokenRepository.deleteByUserId(user.getId().getValue());
        
        // TODO: 추후 추가 데이터 삭제
        // - 사용자 습관 데이터
        // - 사용자 설정 데이터
        // - 기타 연관 데이터
        
        log.info("사용자 관련 데이터 삭제 완료");
    }

    /**
     * 온보딩 완료 처리
     * 사용자의 온보딩 완료 상태를 업데이트
     * 
     * @param command 온보딩 완료 커맨드
     * @return 완료된 사용자 정보 DTO
     */
    public UserResponse completeOnboarding(OnboardingCompletionCommand command) {
        log.info("온보딩 완료 처리 시작");
        
        User user = userRepository.findById(UserId.of(command.userId()))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        log.info("Command에서 받은 닉네임: [{}]", command.nickname());
        
        try {
            user.completeOnboarding(
                command.nickname(),
                command.mainRecordType(), 
                command.birthDate(),
                command.goalDays(),
                command.notificationEnabled()
            );
            
            log.info("온보딩 완료 후 User의 닉네임: [{}]", user.getNickname());
        } catch (IllegalStateException e) {
            log.warn("온보딩 중복 완료 시도 발생");
            throw new CustomException(ErrorCode.USER_ONBOARDING_ALREADY_COMPLETED);
        }
        
        User savedUser = userRepository.save(user);
        
        log.info("온보딩 완료 처리 완료");
        
        return UserResponse.from(savedUser);
    }

    /**
     * FCM 토큰 업데이트
     * 사용자의 FCM 토큰을 업데이트하여 푸시 알림 발송을 위한 토큰 저장
     * 
     * @param command FCM 토큰 업데이트 커맨드
     */
    public void updateFcmToken(FcmTokenUpdateCommand command) {
        log.info("FCM 토큰 업데이트 시작 - 사용자 ID: {}", command.getUserId().getValue());
        
        User user = userRepository.findById(command.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        user.updateFcmToken(command.getFcmToken());
        
        userRepository.save(user);
        
        log.info("FCM 토큰 업데이트 완료 - 사용자 ID: {}", command.getUserId().getValue());
    }

}


