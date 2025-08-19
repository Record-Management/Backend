package com.recordmanagement.habitlog.api.auth;

import com.recordmanagement.habitlog.application.auth.AuthApplicationService;
import com.recordmanagement.habitlog.application.auth.dto.LogoutCommand;
import com.recordmanagement.habitlog.application.auth.dto.RefreshTokenCommand;
import com.recordmanagement.habitlog.application.auth.dto.RefreshTokenResult;
import com.recordmanagement.habitlog.application.auth.dto.SocialLoginCommand;
import com.recordmanagement.habitlog.application.auth.dto.SocialLoginResult;
import com.recordmanagement.habitlog.common.response.ApiResponse;
import com.recordmanagement.habitlog.domain.user.model.SocialType;
import com.recordmanagement.habitlog.api.auth.dto.LogoutRequest;
import com.recordmanagement.habitlog.api.auth.dto.RefreshTokenRequest;
import com.recordmanagement.habitlog.api.auth.dto.RefreshTokenResponse;
import com.recordmanagement.habitlog.api.auth.dto.SocialLoginRequest;
import com.recordmanagement.habitlog.api.auth.dto.SocialLoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * 인증 관련 API 컨트롤러
 * 
 * 소셜 로그인 기반의 사용자 인증 시스템을 제공합니다.
 * 카카오, 애플 등의 소셜 플랫폼을 통한 로그인 및 JWT 토큰 관리를 담당합니다.
 * 
 * 주요 기능:
 * - 소셜 로그인 (카카오, 애플)
 * - JWT 액세스 토큰 발급
 * - 리프레시 토큰을 통한 토큰 갱신
 * - 로그아웃 처리
 * 
 * 지원하는 소셜 플랫폼:
 * - KAKAO: 카카오 로그인
 * - APPLE: 애플 로그인 (Sign in with Apple)
 * 
 * 인증 플로우:
 * 1. 클라이언트에서 소셜 플랫폼 로그인
 * 2. 소셜 액세스 토큰을 이 API에 전송
 * 3. 소셜 플랫폼에서 사용자 정보 조회
 * 4. 신규 사용자면 회원가입, 기존 사용자면 로그인 처리
 * 5. JWT 액세스 토큰 + 리프레시 토큰 반환
 * 
 * JWT 토큰 정책:
 * - 액세스 토큰 만료시간: 1시간
 * - 리프레시 토큰 만료시간: 14일
 * - HS256 알고리즘 사용
 * - Bearer 토큰 방식
 * 
 * 신규 사용자 처리:
 * - 소셜 플랫폼에서 받은 정보로 자동 회원가입
 * - 온보딩 미완료 상태로 초기화
 * - UserController에서 온보딩 완료 처리 필요
 * 
 * 보안 고려사항:
 * - 소셜 토큰 검증을 통한 사용자 신원 확인
 * - 리프레시 토큰 로테이션으로 보안 강화
 * - 로그아웃 시 토큰 블랙리스트 처리
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

        if (result.isNewUser()) {
            return ResponseEntity.status(201).body(ApiResponse.created("새 사용자로 가입되었습니다.", response));
        } else {
            return ResponseEntity.ok(ApiResponse.success(response));
        }
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