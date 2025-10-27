package com.recordmanagement.habitlog.domain.user.application.service;

import com.recordmanagement.habitlog.domain.user.application.dto.OnboardingCompletionCommand;
import com.recordmanagement.habitlog.domain.user.application.dto.UserRegistrationCommand;
import com.recordmanagement.habitlog.domain.user.application.dto.UserResponse;
import com.recordmanagement.habitlog.domain.user.domain.model.SocialType;
import com.recordmanagement.habitlog.domain.user.domain.model.User;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.domain.user.domain.repository.UserRepository;
import com.recordmanagement.habitlog.domain.user.domain.service.UserDomainService;
import com.recordmanagement.habitlog.domain.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 사용자 등록 및 온보딩 서비스
 * 
 * SRP 적용: 사용자 등록과 온보딩 완료만을 담당
 * - 신규 사용자 등록
 * - 소셜 로그인용 사용자 등록
 * - 온보딩 완료 처리
 * - 사용자 조회 (등록과 관련된 조회만)
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;

    /**
     * 신규 사용자 등록 처리
     * 도메인 서비스에 위임하여 User 엔터티 생성 후 저장
     * 
     * @param command 사용자 등록 커맨드 객체
     * @return 등록 완료된 사용자 정보 DTO
     */
    public UserResponse registerUser(UserRegistrationCommand command) {
        log.info("사용자 등록 요청: socialType={}, socialId={}", 
                command.getSocialType(), maskSocialId(command.getSocialId()));

        User user = userDomainService.createNewUser(
                command.getName(),
                command.getEmail(),
                command.getSocialType(),
                command.getSocialId()
        );

        User savedUser = userRepository.save(user);
        
        log.info("사용자 등록 완료: userId={}", savedUser.getId().getValue());
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
        log.info("소셜 로그인용 사용자 등록: socialType={}, socialId={}", 
                command.getSocialType(), maskSocialId(command.getSocialId()));

        User user = userDomainService.createNewUser(
                command.getName(),
                command.getEmail(),
                command.getSocialType(),
                command.getSocialId()
        );

        User savedUser = userRepository.save(user);
        
        log.info("소셜 로그인용 사용자 등록 완료: userId={}", savedUser.getId().getValue());
        return UserResponse.forSocialLogin(savedUser);
    }

    /**
     * 온보딩 완료 처리
     * 사용자의 온보딩 상태를 완료로 변경하고 메인 기록 타입 설정
     * 
     * @param command 온보딩 완료 커맨드
     * @return 업데이트된 사용자 정보
     */
    public UserResponse completeOnboarding(OnboardingCompletionCommand command) {
        log.info("온보딩 완료 처리: userId={}, mainRecordType={}", 
                command.userId(), command.mainRecordType());

        UserId userId = UserId.of(command.userId());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserException.notFound(command.userId()));

        user.completeOnboarding(
                command.nickname(),
                command.mainRecordType(), 
                command.birthDate(),
                command.goalDays()
        );
        User updatedUser = userRepository.save(user);
        
        log.info("온보딩 완료: userId={}", updatedUser.getId().getValue());
        return UserResponse.from(updatedUser);
    }

    /**
     * 소셜 로그인 사용자 조회
     * 소셜 타입과 소셜 ID 조합으로 사용자 검색
     * 
     * @param socialType 소셜 로그인 플랫폼 타입
     * @param socialId 소셜 플랫폼 고유 사용자 ID
     * @return 사용자 정보 Optional
     */
    @Transactional(readOnly = true)
    public Optional<UserResponse> findBySocialLogin(SocialType socialType, String socialId) {
        log.debug("소셜 로그인 사용자 조회: socialType={}, socialId={}", 
                socialType, maskSocialId(socialId));

        return userRepository.findBySocialTypeAndSocialId(socialType, socialId)
                .map(UserResponse::forSocialLogin);
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
        
        log.debug("이메일 기반 사용자 조회: email={}, socialType={}", 
                maskEmail(email), socialType);

        return userRepository.findByEmailAndSocialType(email.trim().toLowerCase(), socialType)
                .map(UserResponse::forSocialLogin);
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

    /**
     * 이메일 마스킹 (로깅용)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "****";
        }
        String[] parts = email.split("@");
        if (parts[0].length() <= 2) {
            return "****@" + parts[1];
        }
        return parts[0].substring(0, 2) + "****@" + parts[1];
    }
}