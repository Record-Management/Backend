package com.recordmanagement.habitlog.application.auth;

import com.recordmanagement.habitlog.application.auth.dto.SocialLoginCommand;
import com.recordmanagement.habitlog.application.auth.dto.SocialLoginResult;
import com.recordmanagement.habitlog.application.auth.dto.RefreshTokenCommand;
import com.recordmanagement.habitlog.application.auth.dto.RefreshTokenResult;
import com.recordmanagement.habitlog.application.auth.dto.LogoutCommand;
import com.recordmanagement.habitlog.application.auth.dto.AppleTransferSubCommand;
import com.recordmanagement.habitlog.application.user.UserApplicationService;
import com.recordmanagement.habitlog.application.user.dto.UserRegistrationCommand;
import com.recordmanagement.habitlog.application.user.dto.UserResponse;
import com.recordmanagement.habitlog.config.jwt.JwtTokenProvider;
import com.recordmanagement.habitlog.domain.auth.model.SocialUserInfo;
import com.recordmanagement.habitlog.domain.auth.service.SocialLoginService;
import com.recordmanagement.habitlog.domain.auth.service.RefreshTokenService;
import com.recordmanagement.habitlog.domain.user.model.SocialType;
import com.recordmanagement.habitlog.config.exception.CustomException;
import com.recordmanagement.habitlog.config.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 인증 애플리케이션 서비스
 * 인증 관련 비즈니스 로직을 담당하며,
 * 도메인 서비스와 애플리케이션 계층을 연결하는 중간 계층 역할 수행
 * 소셜 로그인, 토큰 갱신, 로그아웃, Apple 재로그인 등의 Use Case 처리
 * 트랜잭션 단위로 동작하며 예외 발생 시 전체 롤백 보장
 * 
 * identityToken 방식 처리:
 * - 카카오: accessToken으로 사용자 ID 추출
 * - Apple: identityToken(JWT)에서 sub 추출하여 안정적 식별
 * - 단순하고 일관된 socialId 기반 로그인 처리
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Slf4j
@Service
@Transactional
public class AuthApplicationService {

    private final SocialLoginService socialLoginService;
    private final UserApplicationService userApplicationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthApplicationService(SocialLoginService socialLoginService,
                                  UserApplicationService userApplicationService,
                                  JwtTokenProvider jwtTokenProvider,
                                  RefreshTokenService refreshTokenService) {
        this.socialLoginService = socialLoginService;
        this.userApplicationService = userApplicationService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * 소셜 로그인 처리 (단순화된 방식)
     * 소셜 플랫폼으로부터 사용자 정보를 조회하고,
     * socialId 기반으로 기존 사용자 조회 후 로그인 처리
     * identityToken 방식으로 Apple sub를 안정적으로 처리
     * JWT 액세스 토큰과 리프레시 토큰을 생성해 반환
     *
     * @param command 소셜 로그인 요청 정보 (소셜 타입, identityToken/accessToken)
     * @return 로그인 결과 (사용자 정보, 액세스 토큰, 리프레시 토큰, 신규 사용자 여부)
     */
    public SocialLoginResult socialLogin(SocialLoginCommand command) {
        SocialUserInfo socialUserInfo = socialLoginService.getUserInfo(command.getSocialType(), command.getAccessToken());

        // socialId로 기존 사용자 조회 (단순하고 안정적)
        Optional<UserResponse> existingUser = userApplicationService.findBySocialLogin(command.getSocialType(), socialUserInfo.getSocialId());

        UserResponse user;
        boolean isNewUser = false;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            
            // 탈퇴한 사용자인지 확인하고 복구 처리
            if (userApplicationService.isUserWithdrawn(user.getId())) {
                if (userApplicationService.canRestoreUser(user.getId())) {
                    // 7일 이내 탈퇴 사용자 복구
                    user = userApplicationService.restoreWithdrawnUser(user.getId());
                    log.info("탈퇴 사용자 복구: userId={}, socialType={}", user.getId(), command.getSocialType());
                } else {
                    // 7일 경과로 복구 불가능
                    throw new CustomException(ErrorCode.USER_PERMANENTLY_DELETED);
                }
            } else {
                // 일반 기존 사용자 로그인
                log.info("기존 사용자 로그인: userId={}, socialType={}", user.getId(), command.getSocialType());
            }
        } else {
            // 신규 사용자 가입
            user = createNewUser(socialUserInfo, command.getSocialType());
            isNewUser = true;
            log.info("신규 사용자 가입: userId={}, socialType={}", user.getId(), command.getSocialType());
        }

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        return new SocialLoginResult(user, accessToken, refreshToken, isNewUser);
    }

    /**
     * 신규 사용자 생성
     */
    private UserResponse createNewUser(SocialUserInfo socialUserInfo, SocialType socialType) {
        UserRegistrationCommand registrationCommand = new UserRegistrationCommand(
                socialUserInfo.getName(),
                socialUserInfo.getEmail(),
                socialType,
                socialUserInfo.getSocialId()
        );
        return userApplicationService.registerUserForSocialLogin(registrationCommand);
    }

    private String generateAccessToken(UserResponse user) {
        return jwtTokenProvider.generateAccessToken(user.getId());
    }

    private String generateRefreshToken(UserResponse user) {
        return refreshTokenService.createRefreshToken(user.getId());
    }

    /**
     * 액세스 토큰 갱신 처리
     * 리프레시 토큰 검증 후 새로운 액세스 토큰 발급
     * 토큰 만료 시간도 함께 반환
     *
     * @param command 리프레시 토큰 갱신 요청
     * @return 갱신 결과 (새 액세스 토큰, 만료 시간 초 단위)
     */
    public RefreshTokenResult refreshAccessToken(RefreshTokenCommand command) {
        String refreshTokenValue = command.refreshToken();
        
        // 토큰에서 사용자 ID 추출
        String userId = jwtTokenProvider.getUserIdAsStringFromToken(refreshTokenValue);
        
        // 사용자 정보 조회 (토큰 갱신용 최소 필드)
        UserResponse user = userApplicationService.findByIdForRefreshToken(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 새 액세스 토큰 발급
        String newAccessToken = refreshTokenService.refreshAccessToken(refreshTokenValue);
        Long expiresIn = 3600L; // 1시간
        
        return RefreshTokenResult.of(newAccessToken, expiresIn, user);
    }

    /**
     * 로그아웃 처리
     * 단일 디바이스 또는 전체 디바이스 로그아웃 지원
     * 전체 로그아웃 시 JWT 토큰에서 사용자 ID 추출해 모든 토큰 무효화
     * 단일 로그아웃 시 해당 토큰만 무효화
     * 토큰 파싱 실패 시에도 무조건 해당 토큰 삭제 시도
     *
     * @param command 로그아웃 요청 정보 (리프레시 토큰, 전체 로그아웃 여부)
     */
    public void logout(LogoutCommand command) {
        if (command.allDevices()) {
            try {
                String userId = jwtTokenProvider.getUserIdAsStringFromToken(command.refreshToken());
                refreshTokenService.invalidateAllRefreshTokens(userId);
            } catch (Exception e) {
                if (command.refreshToken() != null && !command.refreshToken().trim().isEmpty()) {
                    refreshTokenService.invalidateRefreshToken(command.refreshToken());
                }
            }
        } else {
            if (command.refreshToken() != null && !command.refreshToken().trim().isEmpty()) {
                refreshTokenService.invalidateRefreshToken(command.refreshToken());
            }
        }
    }

    /**
     * Apple Transfer Sub 처리
     * Apple에서 사용자 실명 정보 변경으로 새로운 sub가 발급된 경우 기존 계정과 연결
     * 
     * @param command Apple Transfer Sub 처리 커맨드
     * @return 업데이트된 사용자 정보
     */
    public UserResponse processAppleTransferSub(AppleTransferSubCommand command) {
        log.info("Apple Transfer Sub 처리 시작: oldSub={}, newSub={}", 
                command.oldSub(), command.newSub());
        
        // 1. 기존 사용자 조회
        Optional<UserResponse> existingUser = userApplicationService.findBySocialLogin(
                SocialType.APPLE, command.oldSub()
        );
        
        if (existingUser.isEmpty()) {
            log.error("Apple Transfer Sub 처리 실패 - 기존 사용자를 찾을 수 없음: oldSub={}", command.oldSub());
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 2. 새 sub로 이미 가입된 사용자가 있는지 확인
        Optional<UserResponse> conflictUser = userApplicationService.findBySocialLogin(
                SocialType.APPLE, command.newSub()
        );
        
        if (conflictUser.isPresent()) {
            log.error("Apple Transfer Sub 처리 실패 - 새 sub로 이미 가입된 사용자 존재: newSub={}", command.newSub());
            throw new CustomException(ErrorCode.SOCIAL_ID_ALREADY_EXISTS);
        }
        
        // 3. socialId 업데이트
        UserResponse updatedUser = userApplicationService.updateUserSocialId(
                existingUser.get().getId(), 
                command.newSub()
        );
        
        log.info("Apple Transfer Sub 처리 완료: userId={}, oldSub={}, newSub={}", 
                updatedUser.getId(), command.oldSub(), command.newSub());
        
        return updatedUser;
    }
}
