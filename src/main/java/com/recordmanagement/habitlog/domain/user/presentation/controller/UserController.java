package com.recordmanagement.habitlog.domain.user.presentation.controller;

import com.recordmanagement.habitlog.domain.user.application.service.UserApplicationService;
import com.recordmanagement.habitlog.domain.user.application.dto.UserWithdrawalCommand;
import com.recordmanagement.habitlog.domain.user.application.dto.OnboardingCompletionCommand;
import com.recordmanagement.habitlog.domain.user.application.dto.FcmTokenUpdateCommand;
import com.recordmanagement.habitlog.domain.user.application.dto.GoalResetCommand;
import com.recordmanagement.habitlog.domain.user.application.dto.UpdateProfileCommand;
import com.recordmanagement.habitlog.domain.user.application.dto.UserResponse;
import com.recordmanagement.habitlog.global.common.response.ApiResponse;
import com.recordmanagement.habitlog.domain.user.presentation.dto.UserWithdrawalRequest;
import com.recordmanagement.habitlog.domain.user.presentation.dto.OnboardingCompletionRequest;
import com.recordmanagement.habitlog.domain.user.presentation.dto.FcmTokenUpdateRequest;
import com.recordmanagement.habitlog.domain.user.presentation.dto.UpdateProfileRequest;
import com.recordmanagement.habitlog.domain.user.presentation.dto.GoalResetRequest;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 사용자 관리 관련 API 컨트롤러
 * 
 * 사용자 계정 관리 및 온보딩 프로세스를 담당하는 REST API를 제공합니다.
 * 회원가입, 온보딩, 회원탈퇴 등 사용자 라이프사이클 전반을 관리합니다.
 * 
 * 주요 기능:
 * - 사용자 온보딩 완료 처리
 * - 온보딩 상태 조회
 * - 회원 탈퇴 처리
 * - 사용자 프로필 관리
 * 
 * 온보딩 프로세스:
 * 1. 소셜 로그인 완료 (AuthController)
 * 2. 메인 기록 타입 선택 (일상/운동/습관/일정 중 선택)
 * 3. 온보딩 완료 처리 (이 컨트롤러)
 * 4. 메인 화면 진입
 * 
 * 지원하는 메인 기록 타입:
 * - DAILY: 일상 기록 (기분, 제목, 내용, 이미지) - 구현됨
 * - EXERCISE: 운동 기록 (운동 종류, 칼로리, 시간 등) - 구현됨
 * - HABIT: 습관 기록 - 추후 구현 예정
 * - SCHEDULE: 일정 기록 - 추후 구현 예정
 * 
 * 회원 탈퇴 처리:
 * - Soft delete 방식 (7일 보관 정책)
 * - 즉시: RefreshToken만 삭제, 계정 비활성화
 * - 7일 후: 스케줄러가 모든 관련 데이터 완전 삭제
 * 
 * 인증 요구사항:
 * - 모든 API는 JWT Bearer 토큰 인증 필요
 * - 사용자는 본인 계정만 관리 가능
 * 
 * @author 전우선
 * @since 2025.09.04
 * @version 2.1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    private final UserApplicationService userApplicationService;

    public UserController(UserApplicationService userApplicationService) {
        this.userApplicationService = userApplicationService;
    }

    /**
     * 회원탈퇴 API (Soft Delete)
     * 7일 보관 정책에 따라 즉시 삭제하지 않고 탈퇴 표시 후 7일 후 자동 삭제
     *
     * @param request 회원탈퇴 요청 DTO
     * @param authentication 인증된 사용자 정보
     * @return 성공 응답
     */
    @Operation(
        summary = "회원탈퇴", 
        description = """
            회원탈퇴를 처리합니다. (Soft Delete 방식)
            
            ### 탈퇴 정책
            - 즉시 삭제되지 않고 7일간 보관됩니다
            - 7일 이내 재가입 시 자동으로 복구됩니다
            - 7일 경과 후 자동으로 영구 삭제됩니다
            
            ### 처리 과정
            1. 소셜 플랫폼 연결 해제 시도
            2. 사용자 계정 탈퇴 표시 (deletedAt 설정)
            3. 7일 후 삭제 예약 (deletionScheduledAt 설정)
            4. 즉시 로그아웃 처리 (RefreshToken 삭제)
            5. 사용자 관련 데이터는 7일 보관 후 스케줄러에서 완전 삭제
            
            ### 재가입 복구
            - 7일 이내 동일 소셜 계정으로 로그인 시 자동 복구
            - 기존 데이터 모두 유지
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "204",
                description = "탈퇴 성공 (응답 본문 없음)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "이미 탈퇴한 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패 (토큰 없음/만료/잘못됨)",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "error": "토큰이 만료되었거나 유효하지 않습니다."
                        }
                        """
                    )
                )
            )
        }
    )
    @DeleteMapping("/withdrawal")
    public ResponseEntity<ApiResponse<Void>> withdrawUser(
            @Valid @RequestBody UserWithdrawalRequest request,
            Authentication authentication) {

        log.info("회원탈퇴 요청 수신");

        UserWithdrawalCommand command = new UserWithdrawalCommand(
                authentication.getName(), // JWT에서 추출된 사용자 ID
                request.getReason()
        );

        userApplicationService.withdrawUser(command);

        log.info("회원탈퇴 처리 완료");

        return ResponseEntity.noContent().build();

    }

    /**
     * 내 정보 조회 API
     * 현재 로그인한 사용자의 모든 정보를 반환 (mainRecordType 포함)
     *
     * @param authentication 인증된 사용자 정보
     * @return 사용자 상세 정보
     */
    @Operation(
        summary = "내 정보 조회",
        description = """
            현재 로그인한 사용자의 상세 정보를 조회합니다.
            
            ### 포함 정보
            - 기본 프로필 (이름, 이메일, 닉네임)
            - 메인 기록 타입 (DAILY/HABIT/EXERCISE/SCHEDULE)
            - 온보딩 정보 (생년월일, 목표일수, 알림 설정)
            - 계정 상태 (온보딩 완료 여부, 생성 시간)
            
            ### 사용 시나리오
            - 앱 시작 시 사용자 정보 로드
            - 프로필 화면 표시
            - 설정 화면 초기값 설정
            - 메인 기록 타입 확인
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "내 정보 조회 성공",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "statusCode": 200,
                          "code": "SUCCESS",
                          "message": "요청이 성공적으로 처리되었습니다.",
                          "data": {
                            "id": "550e8400-e29b-41d4-a716-446655440000",
                            "name": "카카오닉네임",
                            "nickname": "홍길동",
                            "email": "user@example.com",
                            "socialType": "KAKAO",
                            "mainRecordType": "EXERCISE",
                            "birthDate": "1998-06-02",
                            "goalDays": 20,
                            "onboardingCompleted": true,
                            "createdAt": "2025-09-02T02:46:41.454753"
                          }
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패 (토큰 없음/만료/잘못됨)",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "error": "토큰이 만료되었거나 유효하지 않습니다."
                        }
                        """
                    )
                )
            )
        }
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo(Authentication authentication) {
        
        log.info("내 정보 조회 요청 수신");
        
        String userId = authentication.getName();
        UserResponse user = userApplicationService.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        log.info("내 정보 조회 완료: userId={}", userId);
        
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 온보딩 완료 API
     * 사용자가 온보딩 과정을 완료했을 때 호출
     *
     * @param request 온보딩 완료 요청 DTO
     * @param userDetails 인증된 사용자 정보
     * @return 성공 응답
     */
    @Operation(
        summary = "온보딩 완료", 
        description = """
            사용자의 온보딩 과정 완료를 처리합니다.
            
            ### 🎯 온보딩 완료 시 자동 처리
            1. **목표 자동 생성**: 선택한 기록 타입과 목표일수로 새로운 목표 자동 생성
            2. **나무 성장 시작**: 4단계 나무 성장 시스템 활성화 (STAGE_1부터 시작)
            3. **진행률 추적**: 기록 생성 시마다 자동으로 목표 진행률 업데이트
            
            ### HABIT 타입 추가 처리
            - 메인 기록 타입을 HABIT로 설정해도 자동 생성하지 않음 (일상/운동과 동일한 UX)
            - habitStartDate가 현재 날짜로 설정되고 habitPeriodInfo가 포함됨
            - 사용자 실제 행동 시에만 기록 생성되어 캘린더 표시
            
            ### 목표 시스템 연동
            - `/api/goals/current`에서 생성된 목표 정보 확인 가능
            - 기록 생성 시 자동으로 목표 진행률 업데이트
            - 목표 완료 시 새로운 목표 생성 가능
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "온보딩 완료 성공",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "statusCode": 200,
                          "code": "SUCCESS",
                          "message": "요청이 성공적으로 처리되었습니다.",
                          "data": {
                            "id": "550e8400-e29b-41d4-a716-446655440000",
                            "name": "카카오닉네임",
                            "nickname": "홍길동",
                            "email": null,
                            "socialType": "KAKAO",
                            "mainRecordType": "EXERCISE",
                            "birthDate": "1998-06-02",
                            "goalDays": 20,
                            "onboardingCompleted": true,
                            "createdAt": "2025-09-02T02:46:41.454753"
                          }
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패 (토큰 없음/만료/잘못됨)",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "error": "토큰이 만료되었거나 유효하지 않습니다."
                        }
                        """
                    )
                )
            )
        }
    )
    @PostMapping("/onboarding/complete")
    public ResponseEntity<ApiResponse<UserResponse>> completeOnboarding(
            @Valid @RequestBody OnboardingCompletionRequest request,
            Authentication authentication) {

        log.info("온보딩 완료 요청 수신");
        log.info("요청 데이터: nickname=[{}], mainRecordType=[{}], birthDate=[{}], goalDays=[{}]", 
                request.getNickname(), request.getMainRecordType(), request.getBirthDate(), 
                request.getGoalDays());

        String userId = authentication.getName();
        
        OnboardingCompletionCommand command = new OnboardingCompletionCommand(
                userId,
                request.getNickname(),
                request.getMainRecordType(),
                request.getBirthDate(),
                request.getGoalDays()
        );

        UserResponse user = userApplicationService.completeOnboarding(command);

        log.info("온보딩 완료 처리 완료");

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 온보딩 재설정 API
     * 기존 사용자가 목표 재설정을 위해 온보딩을 다시 진행할 때 호출
     *
     * @param request 온보딩 완료 요청 DTO
     * @param authentication 인증된 사용자 정보
     * @return 업데이트된 사용자 정보
     */
    @Operation(
        summary = "온보딩 재설정", 
        description = """
            기존 사용자의 목표 재설정을 위한 온보딩 처리를 합니다.
            
            ### ⚠️ 주의사항
            - **기존 진행중인 목표가 있으면 실행되지 않습니다**
            - 목표 완료 또는 포기 후에만 재설정 가능
            
            ### 기존 온보딩과의 차이점
            - 이미 온보딩이 완료된 사용자도 재설정 가능
            - 목표 변경, 메인 기록 타입 변경 등에 사용
            
            ### 🎯 재설정 시 자동 처리
            1. **새로운 목표 생성**: 변경된 기록 타입과 목표일수로 새로운 목표 생성
            2. **나무 성장 초기화**: 새로운 4단계 나무 성장 시작
            3. **진행률 추적 재시작**: 새로운 목표에 대한 진행률 추적
            
            ### HABIT 타입으로 변경시 추가 처리
            - 메인 기록 타입을 HABIT로 변경해도 자동 생성하지 않음 (일상/운동과 동일한 UX)
            - habitStartDate가 현재 날짜로 설정되고 habitPeriodInfo가 업데이트됨
            - 사용자 실제 행동 시에만 기록 생성되어 캘린더 표시
            
            ### 사용 시나리오
            - 목표 완료 후 새로운 기록 타입으로 변경하고 싶을 때
            - 목표일수를 변경하고 싶을 때
            - 개인정보 업데이트가 필요할 때
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "온보딩 재설정 성공",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "statusCode": 200,
                          "code": "SUCCESS",
                          "message": "요청이 성공적으로 처리되었습니다.",
                          "data": {
                            "id": "550e8400-e29b-41d4-a716-446655440000",
                            "name": "카카오닉네임",
                            "nickname": "홍길동",
                            "email": null,
                            "socialType": "KAKAO",
                            "mainRecordType": "DAILY",
                            "birthDate": "1998-06-02",
                            "goalDays": 30,
                            "onboardingCompleted": true,
                            "createdAt": "2025-09-02T02:46:41.454753"
                          }
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패 (토큰 없음/만료/잘못됨)",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "error": "토큰이 만료되었거나 유효하지 않습니다."
                        }
                        """
                    )
                )
            )
        }
    )
    @PostMapping("/onboarding/reset")
    public ResponseEntity<ApiResponse<UserResponse>> resetOnboarding(
            @Valid @RequestBody OnboardingCompletionRequest request,
            Authentication authentication) {

        log.info("온보딩 재설정 요청 수신");
        log.info("요청 데이터: nickname=[{}], mainRecordType=[{}], birthDate=[{}], goalDays=[{}]", 
                request.getNickname(), request.getMainRecordType(), request.getBirthDate(), 
                request.getGoalDays());

        String userId = authentication.getName();
        
        OnboardingCompletionCommand command = new OnboardingCompletionCommand(
                userId,
                request.getNickname(),
                request.getMainRecordType(),
                request.getBirthDate(),
                request.getGoalDays()
        );

        UserResponse user = userApplicationService.resetOnboarding(command);

        log.info("온보딩 재설정 처리 완료");

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @Operation(
        summary = "FCM 토큰 업데이트",
        description = """
            푸시 알림을 위한 FCM 토큰을 업데이트합니다.
            
            ### 자동 알림 시스템 연동
            - 매일 오후 7시 자동 알림 수신을 위해 필수
            - 토큰 등록 후 즉시 알림 수신 가능
            - 알림 설정과 함께 사용하여 개인화된 알림 제어
            
            ### 사용 시나리오
            - 앱 설치 후 최초 푸시 알림 설정
            - 토큰 갱신 시 업데이트
            - 알림 수신 재활성화
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "FCM 토큰 업데이트 성공",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "성공 응답",
                        value = """
                        {
                          "statusCode": 200,
                          "code": "U200",
                          "message": "FCM 토큰이 성공적으로 업데이트되었습니다",
                          "data": null
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패 (토큰 없음/만료/잘못됨)",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "error": "토큰이 만료되었거나 유효하지 않습니다."
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음"
            )
        }
    )
    @PutMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(
            @Valid @RequestBody FcmTokenUpdateRequest request,
            Authentication authentication) {

        log.info("FCM 토큰 업데이트 요청 수신");

        String userId = authentication.getName();
        
        FcmTokenUpdateCommand command = new FcmTokenUpdateCommand(
                UserId.of(userId),
                request.getFcmToken()
        );

        userApplicationService.updateFcmToken(command);

        log.info("FCM 토큰 업데이트 완료");

        return ResponseEntity.ok(ApiResponse.success("FCM 토큰이 성공적으로 업데이트되었습니다", null));
    }

    /**
     * 프로필 업데이트 API
     * 사용자의 닉네임과 생년월일을 수정합니다.
     *
     * @param request 프로필 업데이트 요청 DTO
     * @param authentication 인증된 사용자 정보
     * @return 업데이트된 사용자 정보
     */
    @Operation(
        summary = "프로필 업데이트",
        description = """
            사용자의 닉네임과 생년월일을 선택적으로 수정합니다.
            
            ### 수정 가능한 정보
            - 닉네임 (선택적, 1-6글자, 한글/영문/숫자만)
            - 생년월일 (선택적, YYYY-MM-DD 형식)
            
            ### 요청 예시
            - 닉네임만 수정: {"nickname": "홍길동"}
            - 생년월일만 수정: {"birthDate": "1990-01-01"}
            - 둘 다 수정: {"nickname": "홍길동", "birthDate": "1990-01-01"}
            
            ### 사용 시나리오
            - 설정 화면에서 개별 프로필 정보 수정
            - 닉네임만 변경하기
            - 생년월일만 수정하기
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "프로필 업데이트 성공",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "statusCode": 200,
                          "code": "SUCCESS",
                          "message": "요청이 성공적으로 처리되었습니다.",
                          "data": {
                            "id": "550e8400-e29b-41d4-a716-446655440000",
                            "name": "카카오닉네임",
                            "nickname": "홍길동",
                            "email": "user@example.com",
                            "socialType": "KAKAO",
                            "mainRecordType": "EXERCISE",
                            "birthDate": [1990, 1, 1],
                            "goalDays": 20,
                            "onboardingCompleted": true,
                            "createdAt": "2025-09-02T02:46:41.454753"
                          }
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패 (토큰 없음/만료/잘못됨)",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "error": "토큰이 만료되었거나 유효하지 않습니다."
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음"
            )
        }
    )
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {

        log.info("프로필 업데이트 요청 수신");

        String userId = authentication.getName();
        
        UpdateProfileCommand command = new UpdateProfileCommand(
                userId,
                request.nickname(),
                request.birthDate()
        );

        UserResponse updatedUser = userApplicationService.updateProfile(command);

        log.info("프로필 업데이트 완료");

        return ResponseEntity.ok(ApiResponse.success(updatedUser));
    }

    /**
     * 목표 재설정 API (간소화 버전)
     * 기록 타입과 목표 일수만 변경하는 간단한 재설정
     *
     * @param request 목표 재설정 요청 DTO
     * @param authentication 인증된 사용자 정보
     * @return 업데이트된 사용자 정보
     */
    @Operation(
        summary = "목표 재설정", 
        description = """
            기존 사용자의 기록 타입과 목표 일수만 간단히 재설정합니다.
            
            ### 온보딩 재설정과의 차이점
            - 기록 타입과 목표 일수만 변경
            - 닉네임, 생년월일은 기존 값 유지
            - 더 간단하고 빠른 재설정
            
            ### HABIT 타입으로 변경시 자동 처리
            - 메인 기록 타입을 HABIT로 변경해도 자동 생성하지 않음 (일상/운동과 동일한 UX)
            - habitStartDate가 현재 날짜로 설정되고 habitPeriodInfo가 업데이트됨
            - 사용자 실제 행동 시에만 기록 생성되어 캘린더 표시
            
            ### 처리 상세
            - 실제 행동 기반 기록 생성으로 일관된 사용자 경험 제공
            - 완료 체크 시에만 캘린더에 불 아이콘 표시
            - habitStartDate가 현재 날짜로 설정
            - 생성된 기록들은 캘린더 API에서 확인 가능
            
            ### 사용 시나리오
            - 목표 완료 후 새로운 목표 설정
            - 기록 타입 변경 (운동 → 습관 등)
            - 목표 일수만 조정
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "목표 재설정 성공 (HABIT 타입으로 변경시 자동으로 메인 습관 기록들이 생성됨)",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "HABIT 타입으로 목표 재설정 성공",
                        summary = "HABIT 타입 목표 재설정 - 자동 메인 습관 기록 생성 포함",
                        value = """
                        {
                          "statusCode": 200,
                          "code": "SUCCESS",
                          "message": "요청이 성공적으로 처리되었습니다.",
                          "data": {
                            "id": "550e8400-e29b-41d4-a716-446655440000",
                            "name": "카카오닉네임",
                            "nickname": "홍길동",
                            "email": null,
                            "socialType": "KAKAO",
                            "mainRecordType": "HABIT",
                            "birthDate": "1998-06-02",
                            "goalDays": 10,
                            "habitStartDate": [2025, 11, 1],
                            "habitPeriodInfo": {
                              "startDate": [2025, 11, 1],
                              "endDate": [2025, 11, 10],
                              "totalDays": 10
                            },
                            "onboardingCompleted": true,
                            "createdAt": "2025-09-02T02:46:41.454753"
                          }
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패 (토큰 없음/만료/잘못됨)",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "error": "토큰이 만료되었거나 유효하지 않습니다."
                        }
                        """
                    )
                )
            )
        }
    )
    @PostMapping("/goal-reset")
    public ResponseEntity<ApiResponse<UserResponse>> resetGoal(
            @Valid @RequestBody GoalResetRequest request,
            Authentication authentication) {

        log.info("목표 재설정 요청 수신");
        log.info("요청 데이터: mainRecordType=[{}], goalDays=[{}]", 
                request.getMainRecordType(), request.getGoalDays());

        String userId = authentication.getName();
        
        GoalResetCommand command = new GoalResetCommand(
                userId,
                request.getMainRecordType(),
                request.getGoalDays()
        );

        UserResponse user = userApplicationService.resetGoal(command);

        log.info("목표 재설정 처리 완료");

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 습관 기간 정보 조회 API
     * 습관 타입 사용자의 목표 기간 정보를 반환합니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return 습관 목표 기간 정보
     */
    @Operation(
        summary = "습관 기간 정보 조회",
        description = """
            습관 타입 사용자의 목표 기간 정보를 조회합니다.
            
            ### 포함 정보
            - 습관 시작일 (첫 번째 습관 기록을 등록한 날짜)
            - 습관 종료일 (시작일 + 목표 일수)
            - 총 목표 일수
            
            ### 사용 시나리오
            - 카드 UI에서 진행 상황 표시
            - 달력에서 습관 기간 하이라이트
            - 목표 달성률 계산
            
            ### 주의사항
            - 습관 타입이 아닌 사용자는 null 반환
            - 아직 습관을 시작하지 않은 경우 null 반환
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "습관 기간 정보 조회 성공",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "statusCode": 200,
                          "code": "SUCCESS",
                          "message": "요청이 성공적으로 처리되었습니다.",
                          "data": {
                            "startDate": [2025, 10, 20],
                            "endDate": [2025, 10, 30], 
                            "totalDays": 10
                          }
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패 (토큰 없음/만료/잘못됨)",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "error": "토큰이 만료되었거나 유효하지 않습니다."
                        }
                        """
                    )
                )
            )
        }
    )
    @GetMapping("/habit-period")
    public ResponseEntity<ApiResponse<UserResponse.HabitPeriodInfo>> getHabitPeriodInfo(Authentication authentication) {
        
        log.info("습관 기간 정보 조회 요청 수신");
        
        String userId = authentication.getName();
        UserResponse user = userApplicationService.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        log.info("습관 기간 정보 조회 완료: userId={}", userId);
        
        return ResponseEntity.ok(ApiResponse.success(user.getHabitPeriodInfo()));
    }

    /**
     * FCM 토큰 삭제 API
     * 로그아웃 또는 알림 비활성화 시 FCM 토큰을 삭제합니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return 성공 응답
     */
    @Operation(
        summary = "FCM 토큰 삭제",
        description = """
            사용자의 FCM 토큰을 삭제합니다.
            
            ### 사용 시나리오
            - 사용자가 로그아웃할 때
            - 알림을 완전히 비활성화할 때
            - 회원탈퇴 시 (자동 호출)
            
            ### 처리 과정
            1. 사용자의 FCM 토큰을 null로 설정
            2. 더이상 푸시 알림을 받지 않음
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "204",
                description = "FCM 토큰 삭제 성공 (응답 본문 없음)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "인증 실패 (토큰 없음/만료/잘못됨)",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "error": "토큰이 만료되었거나 유효하지 않습니다."
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음"
            )
        }
    )
    @DeleteMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> deleteFcmToken(Authentication authentication) {

        log.info("FCM 토큰 삭제 요청 수신");

        String userId = authentication.getName();
        
        FcmTokenUpdateCommand command = new FcmTokenUpdateCommand(
                UserId.of(userId),
                null  // null로 설정하여 토큰 삭제
        );

        userApplicationService.updateFcmToken(command);

        log.info("FCM 토큰 삭제 완료");

        return ResponseEntity.noContent().build();
    }

}

