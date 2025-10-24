package com.recordmanagement.habitlog.domain.user.application.service;

import com.recordmanagement.habitlog.domain.user.application.dto.FcmTokenUpdateCommand;
import com.recordmanagement.habitlog.domain.user.application.dto.UpdateProfileCommand;
import com.recordmanagement.habitlog.domain.user.application.dto.UserResponse;
import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
import com.recordmanagement.habitlog.domain.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 사용자 프로필 관리 서비스
 * 
 * SRP 적용: 사용자 프로필 정보 관리만을 담당
 * - 프로필 정보 업데이트
 * - FCM 토큰 관리
 * - 사용자 정보 조회
 * - 토큰 갱신용 사용자 정보 제공
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {

    private final UserRepository userRepository;

    /**
     * 사용자 프로필 업데이트
     * 사용자 이름과 생년월일 정보를 업데이트
     * 
     * @param command 프로필 업데이트 커맨드
     * @return 업데이트된 사용자 정보
     */
    public UserResponse updateProfile(UpdateProfileCommand command) {
        log.info("사용자 프로필 업데이트: userId={}", command.userId());

        UserId userId = UserId.of(command.userId());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserException.notFound(command.userId()));

        user.updateUserProfile(command.nickname(), command.birthDate());
        User updatedUser = userRepository.save(user);
        
        log.info("프로필 업데이트 완료: userId={}, nickname={}", 
                updatedUser.getId().getValue(), command.nickname());
        
        return UserResponse.from(updatedUser);
    }

    /**
     * FCM 토큰 업데이트
     * 푸시 알림을 위한 FCM 토큰 정보 갱신
     * 
     * @param command FCM 토큰 업데이트 커맨드
     * @return 업데이트된 사용자 정보
     */
    public UserResponse updateFcmToken(FcmTokenUpdateCommand command) {
        log.info("FCM 토큰 업데이트: userId={}", command.getUserId().getValue());

        User user = userRepository.findById(command.getUserId())
                .orElseThrow(() -> UserException.notFound(command.getUserId().getValue()));

        user.updateFcmToken(command.getFcmToken());
        User updatedUser = userRepository.save(user);
        
        log.info("FCM 토큰 업데이트 완료: userId={}", updatedUser.getId().getValue());
        
        return UserResponse.from(updatedUser);
    }

    /**
     * 사용자 ID로 사용자 조회
     * UserId VO로 변환 후 Repository 조회
     * 
     * @param userId 사용자 고유 ID 문자열
     * @return 사용자 정보 Optional
     */
    @Transactional(readOnly = true)
    public Optional<UserResponse> findById(String userId) {
        log.debug("사용자 조회: userId={}", userId);

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
        log.debug("토큰 갱신용 사용자 조회: userId={}", userId);

        return userRepository.findById(UserId.of(userId))
                .map(UserResponse::forRefreshToken);
    }

    /**
     * 사용자 소유권 검증
     * 요청한 사용자가 해당 리소스의 소유자인지 확인
     * 
     * @param requestUserId 요청자 사용자 ID
     * @param targetUserId 대상 사용자 ID
     * @throws UserException 소유권이 없는 경우
     */
    @Transactional(readOnly = true)
    public void validateUserOwnership(String requestUserId, String targetUserId) {
        if (!requestUserId.equals(targetUserId)) {
            log.warn("사용자 소유권 검증 실패: requestUserId={}, targetUserId={}", 
                    requestUserId, targetUserId);
            throw UserException.notFound(targetUserId);
        }
    }

    /**
     * 사용자 존재 여부 확인
     * 
     * @param userId 사용자 ID
     * @return 존재 여부
     */
    @Transactional(readOnly = true)
    public boolean existsById(String userId) {
        return userRepository.findById(UserId.of(userId)).isPresent();
    }
}