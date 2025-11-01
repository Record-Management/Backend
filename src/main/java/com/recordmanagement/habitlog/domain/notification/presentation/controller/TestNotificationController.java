package com.recordmanagement.habitlog.domain.notification.presentation.controller;

import com.recordmanagement.habitlog.global.config.scheduler.DailyNotificationScheduler;
import com.recordmanagement.habitlog.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 테스트용 알림 컨트롤러
 * 
 * 개발/테스트 환경에서 알림 기능을 즉시 테스트할 수 있는 API를 제공합니다.
 * 
 * @author 전우선
 * @since 2025.11.01
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/test/notifications")
@RequiredArgsConstructor
@Tag(name = "Test Notification", description = "테스트용 알림 API")
public class TestNotificationController {

    private final DailyNotificationScheduler dailyNotificationScheduler;

    /**
     * 테스트용 즉시 알림 발송 API
     * 스케줄러를 기다리지 않고 즉시 알림 로직을 실행합니다.
     */
    @Operation(
        summary = "테스트용 즉시 알림 발송",
        description = """
            개발/테스트 환경에서 매일 알림 스케줄러를 즉시 실행합니다.
            
            ### 실행 내용
            - 활성 사용자 조회
            - 오늘 기록 안 한 사용자 확인
            - 목표 설정 안 한 사용자 확인
            - 조건에 맞는 사용자에게 즉시 알림 발송
            
            ### 사용 시나리오
            - FCM 토큰 등록 후 즉시 알림 테스트
            - 알림 기능 동작 확인
            - 개발 환경에서 스케줄러 로직 검증
            
            ### 주의사항
            - 실제 사용자에게 알림이 발송되므로 테스트 환경에서만 사용
            - 프로덕션 환경에서는 사용 금지
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "테스트 알림 발송 완료",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "statusCode": 200,
                          "code": "SUCCESS",
                          "message": "테스트 알림이 성공적으로 발송되었습니다.",
                          "data": {
                            "message": "테스트 알림 발송 완료"
                          }
                        }
                        """
                    )
                )
            )
        }
    )
    @PostMapping("/send-now")
    public ResponseEntity<ApiResponse<TestNotificationResponse>> sendTestNotificationNow() {
        
        log.info("테스트용 즉시 알림 발송 요청 수신");
        
        try {
            // 스케줄러 로직을 즉시 실행
            dailyNotificationScheduler.sendDailyNotifications();
            
            TestNotificationResponse response = new TestNotificationResponse("테스트 알림 발송 완료");
            
            log.info("테스트용 즉시 알림 발송 완료");
            
            return ResponseEntity.ok(ApiResponse.success("테스트 알림이 성공적으로 발송되었습니다.", response));
            
        } catch (Exception e) {
            log.error("테스트용 즉시 알림 발송 실패: {}", e.getMessage(), e);
            
            TestNotificationResponse response = new TestNotificationResponse("테스트 알림 발송 실패: " + e.getMessage());
            
            return ResponseEntity.ok(ApiResponse.success("테스트 알림 발송 중 오류가 발생했습니다.", response));
        }
    }

    /**
     * 테스트 알림 응답 DTO
     */
    public record TestNotificationResponse(String message) {}
}