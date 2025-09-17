package com.recordmanagement.habitlog.api.user;

import com.recordmanagement.habitlog.application.user.UserApplicationService;
import com.recordmanagement.habitlog.application.user.dto.UserWithdrawalCommand;
import com.recordmanagement.habitlog.application.user.dto.OnboardingCompletionCommand;
import com.recordmanagement.habitlog.application.user.dto.FcmTokenUpdateCommand;
import com.recordmanagement.habitlog.application.user.dto.UserResponse;
import com.recordmanagement.habitlog.common.response.ApiResponse;
import com.recordmanagement.habitlog.api.user.dto.UserWithdrawalRequest;
import com.recordmanagement.habitlog.api.user.dto.OnboardingCompletionRequest;
import com.recordmanagement.habitlog.api.user.dto.FcmTokenUpdateRequest;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import com.recordmanagement.habitlog.config.exception.CustomException;
import com.recordmanagement.habitlog.config.exception.ErrorCode;
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
 * 2. 메인 기록 타입 선택 (일상/습관/운동/일정 중 선택)
 * 3. 온보딩 완료 처리 (이 컨트롤러)
 * 4. 메인 화면 진입
 * 
 * 지원하는 메인 기록 타입:
 * - DAILY: 일상 기록 (기분, 제목, 내용, 이미지)
 * - HABIT: 습관 기록 (다양한 습관 완료 체크)
 * - EXERCISE: 운동 기록 (운동 종류, 칼로리, 시간 등)
 * - SCHEDULE: 일정 기록 (개인 스케줄 관리)
 * 
 * 회원 탈퇴 처리:
 * - 사용자 데이터 완전 삭제
 * - 관련 기록 데이터 모두 제거
 * - JWT 토큰 무효화
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
     * 회원탈퇴 API
     * 소셜 연결 해제 + 사용자 데이터 삭제를 자동으로 처리
     *
     * @param request 회원탈퇴 요청 DTO
     * @param userDetails 인증된 사용자 정보
     * @return 성공 응답
     */
    @Operation(
        summary = "회원탈퇴", 
        description = "소셜 플랫폼 연결해제와 함께 계정을 완전히 삭제합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication"),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "204",
                description = "탈퇴 성공 (응답 본문 없음)"
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
        security = @SecurityRequirement(name = "Bearer Authentication"),
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
                            "notificationEnabled": true,
                            "onboardingCompleted": true,
                            "createdAt": "2025-09-02T02:46:41.454753"
                          }
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
        description = "사용자의 온보딩 과정 완료를 처리합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication"),
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
                            "notificationEnabled": true,
                            "onboardingCompleted": true,
                            "createdAt": "2025-09-02T02:46:41.454753"
                          }
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
        log.info("요청 데이터: nickname=[{}], mainRecordType=[{}], birthDate=[{}], goalDays=[{}], notificationEnabled=[{}]", 
                request.getNickname(), request.getMainRecordType(), request.getBirthDate(), 
                request.getGoalDays(), request.getNotificationEnabled());

        String userId = authentication.getName();
        
        OnboardingCompletionCommand command = new OnboardingCompletionCommand(
                userId,
                request.getNickname(),
                request.getMainRecordType(),
                request.getBirthDate(),
                request.getGoalDays(),
                request.getNotificationEnabled()
        );

        UserResponse user = userApplicationService.completeOnboarding(command);

        log.info("온보딩 완료 처리 완료");

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @Operation(
        summary = "FCM 토큰 업데이트",
        description = "푸시 알림을 위한 FCM 토큰을 업데이트합니다.",
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
                description = "인증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음"
            )
        }
    )
    // TODO: Firebase 설정 완료 후 주석 해제
    // @PutMapping("/fcm-token")
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

}

