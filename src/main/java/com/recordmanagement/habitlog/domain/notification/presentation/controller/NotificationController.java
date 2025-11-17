package com.recordmanagement.habitlog.domain.notification.presentation.controller;

import com.recordmanagement.habitlog.domain.notification.application.service.NotificationApplicationService;
import com.recordmanagement.habitlog.domain.notification.application.service.NotificationHistoryApplicationService;
import com.recordmanagement.habitlog.domain.notification.domain.service.NotificationReadStatusService;
import com.recordmanagement.habitlog.domain.notification.application.dto.NotificationSettingsCommand;
import com.recordmanagement.habitlog.domain.notification.application.dto.NotificationSettingsResponse;
import com.recordmanagement.habitlog.domain.notification.application.dto.NotificationHistoryResponse;
import com.recordmanagement.habitlog.domain.notification.application.dto.NotificationHistoryWithStatusResponse;
import com.recordmanagement.habitlog.domain.notification.presentation.dto.UpdateNotificationSettingsRequest;
import com.recordmanagement.habitlog.global.common.response.ApiResponse;
import com.recordmanagement.habitlog.global.common.response.PagingResponse;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
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
 * 알림 설정 및 히스토리 관련 API 컨트롤러
 * 
 * 사용자의 알림 설정 관리와 알림 히스토리 조회 기능을 제공하는 REST API입니다.
 * 각 알림 타입별로 개별 활성화/비활성화가 가능하며, 읽음 처리 기능을 포함합니다.
 * 
 * 주요 기능:
 * - 알림 설정 조회/업데이트
 * - 알림 히스토리 조회 (읽음 상태 포함)
 * - 미읽은 알림 개수 조회
 * - 모든 알림 읽음 처리
 * 
 * 지원하는 알림 타입:
 * - dailyRecordNotification: 메인 기록 미등록 알림
 * - exerciseNotification: 운동 기록 미등록 알림
 * - habitNotification: 습관 기록 미등록 알림
 * - goalSettingNotification: 목표 미설정 알림
 * 
 * 자동 알림 시스템:
 * - 매일 오후 7시(한국시간) 자동 발송
 * - 오늘 기록 안 한 사용자 대상
 * - 목표 설정 안 한 사용자 대상
 * - 개인별 알림 설정에 따라 발송
 * 
 * 히스토리 저장 개선 사항 (v2.1.0):
 * - FCM 발송 성공 후에만 히스토리 저장
 * - 트랜잭션 보장으로 데이터 일관성 향상
 * - 발송 실패 시 불필요한 히스토리 저장 방지
 * 
 * 읽음 처리 기능:
 * - recentCheckedAt 필드로 읽음/안읽음 상태 판단
 * - 알림 센터 진입 시 자동 읽음 처리
 * 
 * @author 전우선
 * @since 2025.10.23
 * @version 2.1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 설정 및 히스토리 관련 API")
public class NotificationController {

    private final NotificationApplicationService notificationApplicationService;
    private final NotificationHistoryApplicationService notificationHistoryApplicationService;
    private final NotificationReadStatusService notificationReadStatusService;

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
            - 목표 미설정 알림 활성화 여부
            
            ### 자동 알림 시스템 연동
            - 매일 오후 7시에 설정값에 따라 알림 발송
            - 각 타입별 개별 on/off 가능
            - FCM 토큰 등록 필수
            
            ### 기본값 정책
            - 신규 사용자: 모든 알림이 **true**로 설정 (자동 생성)
            - 설정 조회 시 자동으로 기본 설정이 생성되어 일관된 결과 보장
            
            ### 사용 시나리오
            - 설정 화면 진입 시 현재 알림 설정 표시
            - 앱 시작 시 알림 설정 확인
            - 푸시 알림 수신 여부 제어
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
                            "habitNotificationEnabled": false,
                            "goalSettingNotificationEnabled": true
                          }
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "알림 설정을 찾을 수 없음 (기본값으로 생성됨)"
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
            - exerciseNotificationEnabled: 운동 기록 미등록 알림
            - habitNotificationEnabled: 습관 기록 미등록 알림
            - goalSettingNotificationEnabled: 목표 미설정 알림
            
            ### 선택적 업데이트
            - null이 아닌 값만 업데이트됩니다
            - 설정이 없는 경우 기본 설정을 자동 생성합니다
            
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
                            "habitNotificationEnabled": true,
                            "goalSettingNotificationEnabled": false
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
                request.getHabitNotificationEnabled(),
                request.getGoalSettingNotificationEnabled()
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
            
            ### 응답 정보
            - notifications: 알림 히스토리 목록 (페이징)
            - recentCheckedAt: 알림 센터 마지막 확인 시간 (읽음/안읽음 판단용)
            
            ### 날짜/시간 필드 타입
            - sentAt: **[int, int, int, int, int, int]** (날짜시간 배열: [년, 월, 일, 시, 분, 초])
            - recentCheckedAt: **[int, int, int, int, int, int]** (날짜시간 배열: [년, 월, 일, 시, 분, 초])
            
            ### 정렬 순서
            - 최신 알림부터 표시 (sentAt 역순)
            
            ### 페이징 정보
            - page: 페이지 번호 (0부터 시작, 기본값 0)
            - size: 페이지 크기 (기본값 20, 최대 100)
            
            ### 읽음 상태 판단
            - recentCheckedAt이 null이면 최초 조회 (모든 알림이 새로움)
            - sentAt > recentCheckedAt인 알림은 읽지 않은 알림
            - sentAt <= recentCheckedAt인 알림은 읽은 알림
            
            ### 사용 시나리오
            - 앱 내 알림 센터 화면
            - 알림 목록 무한 스크롤
            - 읽음/안읽음 상태 표시
            """,
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "알림 히스토리 조회 성공",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = {
                        @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "알림 히스토리가 있는 경우",
                            value = """
                        {
                          "statusCode": 200,
                          "code": "SUCCESS",
                          "message": "요청이 성공적으로 처리되었습니다.",
                          "data": {
                            "notifications": {
                              "items": [
                                {
                                  "id": "notification-123",
                                  "type": "DAILY_RECORD_REMINDER",
                                  "title": "HabitLog",
                                  "message": "오늘 습관 실천은 어떠셨나요? 기록해보세요!",
                                  "sentAt": "2025-11-17T19:00:00",
                                  "isRead": false
                                },
                                {
                                  "id": "notification-122",
                                  "type": "DAILY_RECORD_REMINDER",
                                  "title": "HabitLog",
                                  "message": "오늘 운동은 어떠셨나요? 기록해보세요!",
                                  "sentAt": "2025-11-16T19:00:00",
                                  "isRead": true
                                },
                                {
                                  "id": "notification-121",
                                  "type": "GOAL_SETTING_REMINDER",
                                  "title": "HabitLog",
                                  "message": "목표를 설정해서 습관을 시작해보세요!",
                                  "sentAt": "2025-11-15T19:00:00",
                                  "isRead": true
                                }
                              ],
                              "pageInfo": {
                                "page": 0,
                                "size": 20,
                                "totalElements": 15,
                                "totalPages": 1
                              }
                            },
                            "recentCheckedAt": "2025-11-16T20:30:00"
                          }
                        }
                        """
                        ),
                        @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "알림 히스토리가 없는 경우",
                            value = """
                        {
                          "statusCode": 200,
                          "code": "SUCCESS",
                          "message": "요청이 성공적으로 처리되었습니다.",
                          "data": {
                            "notifications": {
                              "items": [],
                              "pageInfo": {
                                "page": 0,
                                "size": 20,
                                "totalElements": 0,
                                "totalPages": 0
                              }
                            },
                            "recentCheckedAt": null
                          }
                        }
                        """
                        )
                    }
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
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<NotificationHistoryWithStatusResponse>> getNotificationHistory(
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
        
        NotificationHistoryWithStatusResponse response = notificationHistoryApplicationService
                .getNotificationHistoryWithStatus(UserId.from(userId), pageable);

        log.info("알림 히스토리 조회 완료: userId={}, recentCheckedAt={}", 
                userId, response.getRecentCheckedAt());

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
            
            ### 처리 내용
            1. 모든 미읽은 알림을 읽음 상태로 변경
            2. 알림 센터 마지막 확인 시간(lastCheckedAt) 업데이트
            
            ### 주요 변경 사항 (v2.0.0)
            - 히스토리 저장 시점: FCM 발송 성공 후에만 저장됨
            - 트랜잭션 보장: readOnly 모드에서 변경 작업이 정상 실행됨
            - 데이터 일관성: 발송 실패 시 불필요한 히스토리 저장 방지
            
            ### 사용 시나리오
            - 알림 센터 화면 진입 시 자동 읽음 처리
            - "모두 읽음" 버튼 클릭 시
            
            ### 효과
            - 이후 알림 히스토리 조회 시 recentCheckedAt 값이 업데이트됨
            - 프론트엔드에서 읽음/안읽음 상태 정확히 판단 가능
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
    @PutMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<MarkAllReadResponse>> markAllAsRead(Authentication authentication) {

        log.info("모든 알림 읽음 처리 요청 수신");

        String userId = authentication.getName();
        UserId userIdObj = UserId.from(userId);
        
        // 1. 알림 히스토리 읽음 처리
        int updatedCount = notificationHistoryApplicationService.markAllAsRead(userIdObj);
        
        // 2. 알림 센터 마지막 확인 시간 업데이트
        notificationReadStatusService.updateLastCheckedAt(userIdObj);

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