package com.recordmanagement.habitlog.presentation.auth;

import com.recordmanagement.habitlog.application.auth.AuthApplicationService;
import com.recordmanagement.habitlog.application.auth.dto.*;
import com.recordmanagement.habitlog.common.response.ApiResponse;
import com.recordmanagement.habitlog.domain.user.model.SocialType;
import com.recordmanagement.habitlog.presentation.auth.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 인증 관련 API 컨트롤러
 * 
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    /**
     * 소셜 로그인 API
     *
     * @param request 소셜 로그인 요청 DTO
     * @return 로그인 결과 및 토큰 정보
     */
    @Operation(summary = "소셜 로그인", description = "소셜 플랫폼 액세스 토큰으로 로그인 처리 및 토큰 발급")
    @PostMapping("/social-login")
    public ResponseEntity<ApiResponse<SocialLoginResponse>> socialLogin(
            @Valid @RequestBody SocialLoginRequest request) {

        log.info("소셜 로그인 요청: socialType={}", request.getSocialType());

        SocialLoginCommand command = new SocialLoginCommand(
                SocialType.fromValue(request.getSocialType()),
                request.getAccessToken()
        );

        SocialLoginResult result = authApplicationService.socialLogin(command);

        SocialLoginResponse response = SocialLoginResponse.from(result);

        log.info("소셜 로그인 성공: userId={}, isNewUser={}",
                result.getUser().getId(), result.isNewUser());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 액세스 토큰 갱신 API
     *
     * @param request 리프레시 토큰 요청 DTO
     * @return 새로운 액세스 토큰 응답 DTO
     */
    @Operation(summary = "액세스 토큰 갱신", description = "유효한 리프레시 토큰으로 액세스 토큰 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.info("액세스 토큰 갱신 요청");

        RefreshTokenCommand command = new RefreshTokenCommand(request.getRefreshToken());
        RefreshTokenResult result = authApplicationService.refreshAccessToken(command);

        RefreshTokenResponse response = RefreshTokenResponse.from(result);

        log.info("액세스 토큰 갱신 성공");

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 로그아웃 API
     *
     * @param request 로그아웃 요청 DTO (토큰, 전체 기기 로그아웃 여부)
     * @return 빈 성공 응답
     */
    @Operation(summary = "로그아웃", description = "리프레시 토큰 폐기 및 로그아웃 처리")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request) {

        log.info("로그아웃 요청: allDevices={}", request.isAllDevices());

        LogoutCommand command = new LogoutCommand(
                request.getRefreshToken(),
                request.isAllDevices()
        );

        authApplicationService.logout(command);

        log.info("로그아웃 성공");

        return ResponseEntity.ok(ApiResponse.success());
    }
}