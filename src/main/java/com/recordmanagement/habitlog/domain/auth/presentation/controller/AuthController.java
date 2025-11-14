package com.recordmanagement.habitlog.domain.auth.presentation.controller;

import com.recordmanagement.habitlog.domain.auth.application.service.AuthApplicationService;
import com.recordmanagement.habitlog.domain.auth.application.dto.LogoutCommand;
import com.recordmanagement.habitlog.domain.auth.application.dto.RefreshTokenCommand;
import com.recordmanagement.habitlog.domain.auth.application.dto.RefreshTokenResult;
import com.recordmanagement.habitlog.domain.auth.application.dto.SocialLoginCommand;
import com.recordmanagement.habitlog.domain.auth.application.dto.SocialLoginResult;
import com.recordmanagement.habitlog.domain.auth.application.dto.AppleTransferSubCommand;
import com.recordmanagement.habitlog.global.common.response.ApiResponse;
import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import com.recordmanagement.habitlog.domain.user.domain.model.SocialType;
import com.recordmanagement.habitlog.domain.auth.presentation.dto.LogoutRequest;
import com.recordmanagement.habitlog.domain.auth.presentation.dto.RefreshTokenRequest;
import com.recordmanagement.habitlog.domain.auth.presentation.dto.RefreshTokenResponse;
import com.recordmanagement.habitlog.domain.auth.presentation.dto.SocialLoginRequest;
import com.recordmanagement.habitlog.domain.auth.presentation.dto.SocialLoginResponse;
import com.recordmanagement.habitlog.domain.auth.presentation.dto.AppleTransferSubRequest;
import com.recordmanagement.habitlog.domain.user.application.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
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
 * - Apple 재로그인 지원 (이메일 기반 기존 사용자 복구)
 * - Apple Transfer Sub 처리 (실명 정보 변경 시 sub 업데이트)
 * 
 * 지원하는 소셜 플랫폼:
 * - KAKAO: 카카오 로그인
 * - APPLE: 애플 로그인 (Sign in with Apple)
 * 
 * 인증 플로우:
 * 1. 클라이언트에서 소셜 플랫폼 로그인
 * 2. 카카오: accessToken, Apple: identityToken을 API에 전송
 * 3. 토큰에서 사용자 식별정보(sub/id) 추출
 * 4. socialId로 기존 사용자 조회
 * 5. 신규 사용자면 회원가입, 기존 사용자면 로그인 처리
 * 6. JWT 액세스 토큰 + 리프레시 토큰 반환
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
 * Apple identityToken 방식:
 * - identityToken에서 sub를 직접 추출하여 안정적으로 사용자 식별
 * - 같은 Apple ID면 동일한 sub로 자동 계정 복구
 * - 앱 재설치, 재로그인 등 모든 시나리오에서 안정적 동작
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
    @Operation(
        summary = "소셜 로그인", 
        description = """
            소셜 플랫폼 토큰으로 로그인 처리 및 JWT 토큰 발급
            
            ### Apple 로그인 (identityToken 방식)
            - identityToken에서 sub를 추출하여 안정적으로 사용자 식별
            - 동일한 Apple ID는 항상 같은 sub로 매핑되어 계정 복구 자동화
            - 앱 재설치, 재로그인 등 모든 시나리오에서 안정적 동작
            
            ### 카카오 로그인
            - accessToken으로 카카오 사용자 ID 추출하여 처리
            
            ### 탈퇴 사용자 자동 복구
            - 7일 이내 탈퇴한 사용자가 재로그인 시 자동으로 계정 복구
            - 기존 데이터 모두 유지
            - 7일 경과한 탈퇴 사용자는 복구 불가능
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "로그인 성공",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        implementation = com.recordmanagement.habitlog.global.common.response.ApiResponse.class
                    ),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "statusCode": 200,
                          "code": "S200",
                          "message": "로그인이 성공적으로 처리되었습니다.",
                          "data": {
                            "user": {
                              "id": "user_123456",
                              "name": "홍길동",
                              "email": "hong@example.com",
                              "socialType": "KAKAO",
                              "createdAt": "2025-09-02T02:46:41.454753",
             
                              "onboardingCompleted": false
                            },
                            "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMTIzIiwiaWF0IjoxNjY5MjY0NjY0LCJleHAiOjE2NjkyNjgyNjR9.abcdefghijklmnopqrstuvwxyz",
                            "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMTIzIiwiaWF0IjoxNjY5MjY0NjY0LCJleHAiOjE2NjkyNjgyNjR9.abcdefghijklmnopqrstuvwxyz",
                            "isNewUser": false
                          }
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "영구 삭제된 사용자 (7일 경과)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패"
            )
        }
    )
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
    @Operation(
        summary = "액세스 토큰 갱신 (토큰 로테이션 적용)", 
        description = """
            유효한 리프레시 토큰으로 액세스 토큰 재발급
            
            ### 보안 강화: 토큰 로테이션
            - 새로운 액세스 토큰과 함께 새로운 리프레시 토큰도 발급됩니다
            - 기존 리프레시 토큰은 자동으로 무효화됩니다 (재사용 불가)
            - 토큰 탈취 공격을 방어하기 위한 보안 정책입니다
            
            ### 중요 사항
            - 응답으로 받은 새로운 리프레시 토큰을 반드시 저장해야 합니다
            - 기존 리프레시 토큰은 더 이상 사용할 수 없습니다
            - 클라이언트에서 토큰을 안전하게 업데이트 처리해야 합니다
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "토큰 갱신 성공 (토큰 로테이션 적용)",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "statusCode": 200,
                          "code": "S200",
                          "message": "요청이 성공적으로 처리되었습니다.",
                          "data": {
                            "accessToken": "eyJhbGciOiJIUzI1NiJ9.new_access_token...",
                            "refreshToken": "eyJhbGciOiJIUzI1NiJ9.new_refresh_token...",
                            "expiresIn": 3600,
                            "user": {
                              "id": "user_id",
                              "name": "이름",
                              "email": null,
                              "onboardingCompleted": false
                            }
                          }
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "리프레시 토큰이 유효하지 않거나 만료됨"
            )
        }
    )
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
    @Operation(
        summary = "로그아웃 (토큰 블랙리스트 적용)", 
        description = """
            리프레시 토큰을 폐기하여 로그아웃 처리합니다.
            
            ### 보안 강화: 토큰 블랙리스트
            - 로그아웃 시 액세스 토큰을 블랙리스트에 추가합니다
            - 블랙리스트된 토큰은 즉시 사용 불가능합니다
            - 토큰 탈취 시에도 안전하게 무효화됩니다
            
            ### 인증 방식 (필수)
            - **Authorization 헤더**: `Bearer {accessToken}` 형식으로 제공 필수
            - **요청 본문**: refreshToken 포함 필수
            - 액세스 토큰이 없으면 400 에러 반환
            
            ### 로그아웃 옵션
            - **단일 기기 로그아웃 (allDevices: false)**: 현재 기기만 로그아웃
            - **전체 기기 로그아웃 (allDevices: true)**: 모든 기기에서 강제 로그아웃
            
            ### 특징
            - 만료된 토큰도 로그아웃 처리 가능
            - 토큰 탈취 의심 시 allDevices=true 권장
            - 즉시 토큰 무효화로 보안 강화
            """,
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "로그아웃 성공",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "statusCode": 200,
                          "code": "S200",
                          "message": "로그아웃되었습니다."
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Authorization 헤더 누락",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "statusCode": 400,
                          "code": "E40023",
                          "message": "로그아웃 시 Authorization 헤더에 액세스 토큰이 필요합니다."
                        }
                        """
                    )
                )
            )
        }
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request,
            HttpServletRequest httpRequest) {

        log.info("로그아웃 요청: allDevices={}", request.isAllDevices());

        // Authorization 헤더에서 액세스 토큰 추출 (필수)
        String accessToken = extractAccessTokenFromHeader(httpRequest);
        if (accessToken == null || accessToken.trim().isEmpty()) {
            log.warn("로그아웃 요청에 Authorization 헤더가 누락됨");
            throw new CustomException(ErrorCode.MISSING_ACCESS_TOKEN_FOR_LOGOUT);
        }

        LogoutCommand command = new LogoutCommand(
                request.getRefreshToken(),
                request.isAllDevices(),
                accessToken
        );

        authApplicationService.logout(command);

        log.info("로그아웃 성공");

        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * Authorization 헤더에서 액세스 토큰 추출
     */
    private String extractAccessTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Apple Transfer Sub 처리 API
     * 
     * Apple에서 사용자 실명 정보 변경으로 새로운 sub가 발급된 경우 기존 계정과 연결
     * 
     * @param request Apple Transfer Sub 요청 DTO
     * @return 업데이트된 사용자 정보
     */
    @Operation(
        summary = "Apple Transfer Sub 처리",
        description = "Apple 사용자 실명 정보 변경으로 새로운 sub가 발급된 경우 기존 계정과 연결",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Transfer Sub 처리 성공",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "statusCode": 200,
                          "code": "S200",
                          "message": "Apple Transfer Sub 처리가 완료되었습니다.",
                          "data": {
                            "id": "user_123456",
                            "name": "사용자",
                            "email": "user@privaterelay.appleid.com",
                            "socialType": "APPLE",
                            "onboardingCompleted": true
                          }
                        }
                        """
                    )
                )
            )
        }
    )
    @PostMapping("/apple-transfer-sub")
    public ResponseEntity<ApiResponse<UserResponse>> processAppleTransferSub(
            @Valid @RequestBody AppleTransferSubRequest request) {

        log.info("Apple Transfer Sub 처리 요청: oldSub={}, newSub={}", 
                request.getOldSub(), request.getNewSub());

        AppleTransferSubCommand command = new AppleTransferSubCommand(
                request.getOldSub(),
                request.getNewSub(),
                request.getTransferSub()
        );

        UserResponse updatedUser = authApplicationService.processAppleTransferSub(command);

        log.info("Apple Transfer Sub 처리 성공: userId={}", updatedUser.getId());

        return ResponseEntity.ok(ApiResponse.success("Apple Transfer Sub 처리가 완료되었습니다.", updatedUser));
    }
}