package com.recordmanagement.habitlog.domain.user.application.service;

import com.recordmanagement.habitlog.domain.auth.domain.repository.RefreshTokenRepository;
import com.recordmanagement.habitlog.domain.auth.domain.service.SocialUnlinkService;
import com.recordmanagement.habitlog.domain.user.application.dto.UserResponse;
import com.recordmanagement.habitlog.domain.user.application.dto.UserWithdrawalCommand;
import com.recordmanagement.habitlog.domain.user.domain.event.UserWithdrawnEvent;
import com.recordmanagement.habitlog.domain.user.domain.model.SocialType;
import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
import com.recordmanagement.habitlog.domain.user.exception.UserException;
import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 사용자 생명주기 관리 서비스
 * 
 * SRP 적용: 사용자의 생명주기 관리만을 담당
 * - 사용자 회원탈퇴 처리
 * - 탈퇴 사용자 복구
 * - 소셜 연동 해제
 * - 관련 토큰 정리
 * - 탈퇴 이벤트 발행
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserLifecycleService {

    private final UserRepository userRepository;
    private final SocialUnlinkService socialUnlinkService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 사용자 회원탈퇴 처리
     * 
     * 탈퇴 과정:
     * 1. 사용자 존재 확인
     * 2. 소셜 플랫폼 연동 해제
     * 3. 리프레시 토큰 모두 삭제
     * 4. 사용자 소프트 삭제 처리
     * 5. 탈퇴 이벤트 발행 (다른 도메인에서 관련 데이터 정리)
     * 
     * @param command 회원탈퇴 커맨드
     * @return 탈퇴 처리된 사용자 정보
     */
    public UserResponse withdrawUser(UserWithdrawalCommand command) {
        log.info("회원탈퇴 처리 시작: userId={}", command.getUserId());

        UserId userId = UserId.of(command.getUserId());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserException.notFound(command.getUserId()));

        // 이미 탈퇴한 사용자인지 확인
        if (user.isWithdrawn()) {
            log.warn("이미 탈퇴한 사용자: userId={}", command.getUserId());
            throw new CustomException(ErrorCode.USER_ALREADY_WITHDRAWN);
        }

        try {
            // 1. 소셜 플랫폼 연동 해제 시도
            socialUnlinkService.unlinkSocialConnection(user.getSocialType(), user.getSocialId());
            log.info("소셜 연동 해제 완료: userId={}, socialType={}", 
                    user.getId().getValue(), user.getSocialType());
        } catch (Exception e) {
            // 소셜 연동 해제 실패는 탈퇴를 막지 않음 (이미 해제되었거나 API 오류일 수 있음)
            log.warn("소셜 연동 해제 실패 (진행 계속): userId={}, error={}", 
                    user.getId().getValue(), e.getMessage());
        }

        // 2. 모든 리프레시 토큰 삭제
        try {
            refreshTokenRepository.deleteByUserId(userId.getValue());
            log.info("리프레시 토큰 삭제 완료: userId={}", user.getId().getValue());
        } catch (Exception e) {
            log.error("리프레시 토큰 삭제 실패: userId={}, error={}", 
                    user.getId().getValue(), e.getMessage());
            // 토큰 삭제 실패 시 탈퇴 중단
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 3. 사용자 소프트 삭제
        user.markAsWithdrawn(command.getReason());
        User withdrawnUser = userRepository.save(user);

        // 4. 탈퇴 이벤트 발행 (다른 도메인의 관련 데이터 정리를 위함)
        UserWithdrawnEvent event = new UserWithdrawnEvent(userId, command.getReason());
        eventPublisher.publishEvent(event);
        
        log.info("회원탈퇴 처리 완료: userId={}, withdrawnAt={}", 
                withdrawnUser.getId().getValue(), withdrawnUser.getDeletedAt());

        return UserResponse.from(withdrawnUser);
    }

    /**
     * 탈퇴 사용자 복구 처리
     * 탈퇴한 지 7일 이내의 사용자만 복구 가능
     * 
     * @param socialType 소셜 타입
     * @param socialId 소셜 ID
     * @return 복구된 사용자 정보 Optional
     */
    public Optional<UserResponse> restoreWithdrawnUser(SocialType socialType, String socialId) {
        log.info("탈퇴 사용자 복구 시도: socialType={}, socialId={}", 
                socialType, maskSocialId(socialId));

        Optional<User> withdrawnUserOpt = userRepository.findBySocialTypeAndSocialId(socialType, socialId);
        
        if (withdrawnUserOpt.isEmpty()) {
            log.debug("해당 소셜 계정의 사용자 없음: socialType={}, socialId={}", 
                    socialType, maskSocialId(socialId));
            return Optional.empty();
        }

        User user = withdrawnUserOpt.get();
        
        // 탈퇴하지 않은 사용자인 경우
        if (!user.isWithdrawn()) {
            log.debug("탈퇴하지 않은 사용자: userId={}", user.getId().getValue());
            return Optional.of(UserResponse.forSocialLogin(user));
        }

        // 탈퇴한 지 7일이 경과한 경우 복구 불가
        if (user.getDeletedAt() != null && 
            user.getDeletedAt().isBefore(LocalDateTime.now().minusDays(7))) {
            log.warn("복구 기간 만료 (7일 경과): userId={}, withdrawnAt={}", 
                    user.getId().getValue(), user.getDeletedAt());
            throw new CustomException(ErrorCode.USER_PERMANENTLY_DELETED);
        }

        // 사용자 복구
        user.restoreFromWithdrawal();
        User restoredUser = userRepository.save(user);
        
        log.info("탈퇴 사용자 복구 완료: userId={}", restoredUser.getId().getValue());
        
        return Optional.of(UserResponse.forSocialLogin(restoredUser));
    }

    /**
     * 사용자의 모든 리프레시 토큰 삭제
     * 보안상 필요할 때 호출 (예: 계정 보안 위험 감지 시)
     * 
     * @param userId 사용자 ID
     */
    public void revokeAllTokens(String userId) {
        log.info("모든 리프레시 토큰 삭제: userId={}", userId);

        UserId userIdVO = UserId.of(userId);
        
        // 사용자 존재 확인
        if (userRepository.findById(userIdVO).isEmpty()) {
            throw UserException.notFound(userId);
        }

        refreshTokenRepository.deleteByUserId(userIdVO.getValue());
        
        log.info("모든 리프레시 토큰 삭제 완료: userId={}", userId);
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