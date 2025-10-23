package com.recordmanagement.habitlog.api.notification;

import com.recordmanagement.habitlog.application.notification.NotificationApplicationService;
import com.recordmanagement.habitlog.application.notification.NotificationHistoryApplicationService;
import com.recordmanagement.habitlog.application.notification.dto.NotificationSettingsCommand;
import com.recordmanagement.habitlog.application.notification.dto.NotificationSettingsResponse;
import com.recordmanagement.habitlog.application.notification.dto.NotificationHistoryResponse;
import com.recordmanagement.habitlog.api.notification.dto.UpdateNotificationSettingsRequest;
import com.recordmanagement.habitlog.common.response.ApiResponse;
import com.recordmanagement.habitlog.common.response.PagingResponse;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 알림 설정 관련 API 컨트롤러
 * 
 * 사용자의 알림 설정을 관리하는 REST API를 제공합니다.
 * 각 알림 타입별로 개별 활성화/비활성화가 가능합니다.
 * 
 * 주요 기능:
 * - 알림 설정 조회
 * - 알림 설정 업데이트 (개별/전체)
 * 
 * 지원하는 알림 타입:
 * - dailyRecordNotification: 메인 기록 미등록 알림
 * - exerciseNotification: 운동 기록 알림
 * - habitNotification: 습관 기록 알림
 * 
 * @author 전우선
 * @since 2025.10.23
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 설정 관련 API")
public class NotificationController {

    private final NotificationApplicationService notificationApplicationService;
    private final NotificationHistoryApplicationService notificationHistoryApplicationService;

    /**
     * 알림 설정 조회 API
     * 현재 사용자의 모든 알림 설정을 조회합니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return 알림 설정 정보
     */
    @Operation(
        summary = "알림 설정 조회",
        description = """
            현재 사용자의 모든 알림 설정을 조회합니다.
            
            ### 포함 정보
            - 메인 기록 미등록 알림 활성화 여부
            - 운동 기록 알림 활성화 여부
            - 습관 기록 알림 활성화 여부
            
            ### 사용 시나리오
            - 설정 화면 진입 시 현재 알림 설정 표시
            - 앱 시작 시 알림 설정 확인
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "알림 설정 조회 성공",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "statusCode": 200,
                          "code": "SUCCESS",
                          "message": "요청이 성공적으로 처리되었습니다.",
                          "data": {
                            "userId": "550e8400-e29b-41d4-a716-446655440000",
                            "dailyRecordNotificationEnabled": true,
                            "exerciseNotificationEnabled": true,
                            "habitNotificationEnabled": false
                          }
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "알림 설정을 찾을 수 없음 (기본값으로 생성됨)"
            )
        }
    )
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<NotificationSettingsResponse>> getNotificationSettings(
            Authentication authentication) {

        log.info("알림 설정 조회 요청 수신");

        String userId = authentication.getName();
        NotificationSettingsResponse settings = notificationApplicationService.getNotificationSettings(UserId.from(userId));

        log.info("알림 설정 조회 완료: userId={}", userId);

        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    /**
     * 알림 설정 업데이트 API
     * 사용자의 알림 설정을 선택적으로 업데이트합니다.
     *
     * @param request 알림 설정 업데이트 요청 DTO
     * @param authentication 인증된 사용자 정보
     * @return 업데이트된 알림 설정
     */
    @Operation(
        summary = "알림 설정 업데이트",
        description = """
            사용자의 알림 설정을 선택적으로 업데이트합니다.
            
            ### 업데이트 가능한 설정
            - dailyRecordNotificationEnabled: 메인 기록 미등록 알림
            - exerciseNotificationEnabled: 운동 기록 알림
            - habitNotificationEnabled: 습관 기록 알림
            
            ### 요청 예시
            - 특정 알림만 수정: {"dailyRecordNotificationEnabled": false}
            - 모든 알림 수정: {"dailyRecordNotificationEnabled": true, "exerciseNotificationEnabled": false, "habitNotificationEnabled": true}
            
            ### 사용 시나리오
            - 설정 화면에서 개별 알림 토글
            - 모든 알림 일괄 활성화/비활성화
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "알림 설정 업데이트 성공",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "statusCode": 200,
                          "code": "SUCCESS",
                          "message": "요청이 성공적으로 처리되었습니다.",
                          "data": {
                            "userId": "550e8400-e29b-41d4-a716-446655440000",
                            "dailyRecordNotificationEnabled": false,
                            "exerciseNotificationEnabled": true,
                            "habitNotificationEnabled": true
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
                description = "인증 실패"
            )
        }
    )
    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<NotificationSettingsResponse>> updateNotificationSettings(
            @Valid @RequestBody UpdateNotificationSettingsRequest request,
            Authentication authentication) {

        log.info("알림 설정 업데이트 요청 수신");

        String userId = authentication.getName();
        
        NotificationSettingsCommand command = new NotificationSettingsCommand(
                UserId.from(userId),
                request.getDailyRecordNotificationEnabled(),
                request.getExerciseNotificationEnabled(),
                request.getHabitNotificationEnabled()
        );

        NotificationSettingsResponse updatedSettings = notificationApplicationService.updateNotificationSettings(command);

        log.info("알림 설정 업데이트 완료");

        return ResponseEntity.ok(ApiResponse.success(updatedSettings));
    }

    /**
     * 알림 히스토리 조회 API
     * 사용자의 알림 히스토리를 페이징으로 조회합니다.
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param authentication 인증된 사용자 정보
     * @return 알림 히스토리 목록
     */
    @Operation(
        summary = "알림 히스토리 조회",
        description = """
            사용자의 알림 히스토리를 페이징으로 조회합니다.
            
            ### 정렬 순서
            - 최신 알림부터 표시 (sentAt 역순)
            
            ### 페이징 정보
            - page: 페이지 번호 (0부터 시작, 기본값 0)
            - size: 페이지 크기 (기본값 20, 최대 100)
            
            ### 사용 시나리오
            - 앱 내 알림 센터 화면
            - 알림 목록 무한 스크롤
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "알림 히스토리 조회 성공",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "statusCode": 200,
                          "code": "SUCCESS",
                          "message": "요청이 성공적으로 처리되었습니다.",
                          "data": {
                            "items": [
                              {
                                "mainRecordType": "EXERCISE",
                                "description": "꾸준한 운동 기록으로 건강한 습관을 만들어보세요!",
                                "sentAt": "2025-10-23T20:00:00"
                              },
                              {
                                "mainRecordType": "DAILY",
                                "description": "오늘의 소중한 순간을 기록으로 남겨보세요!",
                                "sentAt": "2025-10-23T19:00:00"
                              }
                            ],
                            "pageInfo": {
                              "page": 0,
                              "size": 20,
                              "totalElements": 2,
                              "totalPages": 1
                            }
                          }
                        }
                        """
                    )
                )
            )
        }
    )
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PagingResponse<NotificationHistoryResponse>>> getNotificationHistory(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 100)", example = "20")
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        log.info("알림 히스토리 조회 요청 수신: page={}, size={}", page, size);

        // 페이지 크기 제한
        if (size > 100) {
            size = 100;
        }

        String userId = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        
        Page<NotificationHistoryResponse> historyPage = notificationHistoryApplicationService
                .getNotificationHistory(UserId.from(userId), pageable);

        PagingResponse<NotificationHistoryResponse> response = PagingResponse.from(historyPage);

        log.info("알림 히스토리 조회 완료: userId={}, totalElements={}", userId, historyPage.getTotalElements());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 미읽은 알림 개수 조회 API
     * 사용자의 미읽은 알림 개수를 조회합니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return 미읽은 알림 개수
     */
    @Operation(
        summary = "미읽은 알림 개수 조회",
        description = """
            사용자의 미읽은 알림 개수를 조회합니다.
            
            ### 사용 시나리오
            - 앱 탭바의 알림 배지 표시
            - 푸시 알림 후 미읽은 개수 업데이트
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "미읽은 알림 개수 조회 성공",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "statusCode": 200,
                          "code": "SUCCESS",
                          "message": "요청이 성공적으로 처리되었습니다.",
                          "data": {
                            "unreadCount": 3
                          }
                        }
                        """
                    )
                )
            )
        }
    )
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(Authentication authentication) {

        log.info("미읽은 알림 개수 조회 요청 수신");

        String userId = authentication.getName();
        long unreadCount = notificationHistoryApplicationService.getUnreadCount(UserId.from(userId));

        UnreadCountResponse response = new UnreadCountResponse(unreadCount);

        log.info("미읽은 알림 개수 조회 완료: userId={}, count={}", userId, unreadCount);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 모든 알림 읽음 처리 API
     * 사용자의 모든 미읽은 알림을 읽음으로 처리합니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return 처리 결과
     */
    @Operation(
        summary = "모든 알림 읽음 처리",
        description = """
            사용자의 모든 미읽은 알림을 읽음으로 처리합니다.
            
            ### 사용 시나리오
            - 알림 센터 화면 진입 시 자동 읽음 처리
            - "모두 읽음" 버튼 클릭 시
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "모든 알림 읽음 처리 성공",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        value = """
                        {
                          "statusCode": 200,
                          "code": "SUCCESS",
                          "message": "모든 알림이 읽음 처리되었습니다.",
                          "data": {
                            "updatedCount": 5
                          }
                        }
                        """
                    )
                )
            )
        }
    )
    @PutMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<MarkAllReadResponse>> markAllAsRead(Authentication authentication) {

        log.info("모든 알림 읽음 처리 요청 수신");

        String userId = authentication.getName();
        int updatedCount = notificationHistoryApplicationService.markAllAsRead(UserId.from(userId));

        MarkAllReadResponse response = new MarkAllReadResponse(updatedCount);

        log.info("모든 알림 읽음 처리 완료: userId={}, updatedCount={}", userId, updatedCount);

        return ResponseEntity.ok(ApiResponse.success("모든 알림이 읽음 처리되었습니다.", response));
    }

    /**
     * 미읽은 알림 개수 응답 DTO
     */
    public record UnreadCountResponse(long unreadCount) {}

    /**
     * 모든 알림 읽음 처리 응답 DTO
     */
    public record MarkAllReadResponse(int updatedCount) {}
}