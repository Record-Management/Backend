package com.recordmanagement.habitlog.api.user;

import com.recordmanagement.habitlog.application.user.UserApplicationService;
import com.recordmanagement.habitlog.application.user.dto.UserWithdrawalCommand;
import com.recordmanagement.habitlog.application.user.dto.OnboardingCompletionCommand;
import com.recordmanagement.habitlog.common.response.ApiResponse;
import com.recordmanagement.habitlog.api.user.dto.UserWithdrawalRequest;
import com.recordmanagement.habitlog.api.user.dto.OnboardingCompletionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
 * @since 2025.08.01
 * @version 1.0.0
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
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @DeleteMapping("/withdrawal")
    public ResponseEntity<ApiResponse<Void>> withdrawUser(
            @Valid @RequestBody UserWithdrawalRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("회원탈퇴 요청: userId={}", userDetails.getUsername());

        UserWithdrawalCommand command = new UserWithdrawalCommand(
                userDetails.getUsername(), // JWT에서 추출된 사용자 ID
                request.getReason()
        );

        userApplicationService.withdrawUser(command);

        log.info("회원탈퇴 처리 완료: userId={}", userDetails.getUsername());

        return ResponseEntity.noContent().build();
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
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/onboarding/complete")
    public ResponseEntity<ApiResponse<Void>> completeOnboarding(
            @Valid @RequestBody OnboardingCompletionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("온보딩 완료 요청: userId={}", userDetails.getUsername());

        OnboardingCompletionCommand command = new OnboardingCompletionCommand(
                userDetails.getUsername(),
                request.getNickname(),
                request.getMainRecordType(),
                request.getBirthDate(),
                request.getGoalDays(),
                request.getNotificationEnabled()
        );

        userApplicationService.completeOnboarding(command);

        log.info("온보딩 완료 처리 완료: userId={}", userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success());
    }

}
