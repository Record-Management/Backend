package com.recordmanagement.habitlog.application.auth;

import com.recordmanagement.habitlog.application.auth.dto.SocialLoginCommand;
import com.recordmanagement.habitlog.application.auth.dto.SocialLoginResult;
import com.recordmanagement.habitlog.application.auth.dto.RefreshTokenCommand;
import com.recordmanagement.habitlog.application.auth.dto.RefreshTokenResult;
import com.recordmanagement.habitlog.application.auth.dto.LogoutCommand;
import com.recordmanagement.habitlog.application.user.UserApplicationService;
import com.recordmanagement.habitlog.application.user.dto.UserRegistrationCommand;
import com.recordmanagement.habitlog.application.user.dto.UserResponse;
import com.recordmanagement.habitlog.config.jwt.JwtTokenProvider;
import com.recordmanagement.habitlog.domain.auth.model.SocialUserInfo;
import com.recordmanagement.habitlog.domain.auth.service.SocialLoginService;
import com.recordmanagement.habitlog.domain.auth.service.RefreshTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 인증 애플리케이션 서비스
 * 인증 관련 비즈니스 로직을 담당하며,
 * 도메인 서비스와 애플리케이션 계층을 연결하는 중간 계층 역할 수행
 * 소셜 로그인, 토큰 갱신, 로그아웃 등의 Use Case 처리
 * 트랜잭션 단위로 동작하며 예외 발생 시 전체 롤백 보장
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
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
     * 소셜 로그인 처리
     * 소셜 플랫폼으로부터 사용자 정보를 조회하고,
     * 기존 사용자라면 바로 로그인, 신규 사용자면 회원가입 후 로그인 처리
     * JWT 액세스 토큰과 리프레시 토큰을 생성해 반환
     *
     * @param command 소셜 로그인 요청 정보 (소셜 타입, 액세스 토큰)
     * @return 로그인 결과 (사용자 정보, 액세스 토큰, 리프레시 토큰, 신규 사용자 여부)
     */
    public SocialLoginResult socialLogin(SocialLoginCommand command) {
        SocialUserInfo socialUserInfo = socialLoginService.getUserInfo(command.getSocialType(), command.getAccessToken());

        Optional<UserResponse> existingUser = userApplicationService.findBySocialLogin(command.getSocialType(), socialUserInfo.getSocialId());

        UserResponse user;
        boolean isNewUser = false;

        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            UserRegistrationCommand registrationCommand = new UserRegistrationCommand(
                    socialUserInfo.getName(),
                    socialUserInfo.getEmail(),
                    command.getSocialType(),
                    socialUserInfo.getSocialId()
            );
            user = userApplicationService.registerUserForSocialLogin(registrationCommand);
            isNewUser = true;
        }

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        return new SocialLoginResult(user, accessToken, refreshToken, isNewUser);
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
        
        // 사용자 정보 조회
        UserResponse user = userApplicationService.findById(userId)
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
}
