package com.recordmanagement.habitlog.presentation.user;

import com.recordmanagement.habitlog.application.user.UserApplicationService;
import com.recordmanagement.habitlog.application.user.dto.UserWithdrawalCommand;
import com.recordmanagement.habitlog.common.response.ApiResponse;
import com.recordmanagement.habitlog.presentation.user.dto.UserWithdrawalRequest;
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
 * 사용자 관련 API 컨트롤러
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

        return ResponseEntity.ok(ApiResponse.success());
    }
}
