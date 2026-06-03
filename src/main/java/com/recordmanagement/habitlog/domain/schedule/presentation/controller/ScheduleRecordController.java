package com.recordmanagement.habitlog.domain.schedule.presentation.controller;

import com.recordmanagement.habitlog.domain.schedule.application.dto.ScheduleResponse;
import com.recordmanagement.habitlog.domain.schedule.application.service.ScheduleRecordApplicationService;
import com.recordmanagement.habitlog.domain.schedule.presentation.dto.CreateScheduleRequest;
import com.recordmanagement.habitlog.domain.schedule.presentation.dto.UpdateScheduleRequest;
import com.recordmanagement.habitlog.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/schedule-records")
@RequiredArgsConstructor
@Tag(name = "Schedule Record", description = "일정 기록 관련 API")
public class ScheduleRecordController {

    private final ScheduleRecordApplicationService scheduleRecordApplicationService;

    @PostMapping
    @Operation(summary = "일정 기록 작성",
               description = """
               새로운 일정 기록을 작성합니다.

               ## 필수 항목
               - title: 일정 제목
               - startDate: 시작일
               - endDate: 종료일
               - notificationType: 알림 타입 (NONE, ONE_DAY_BEFORE, TWO_DAYS_BEFORE, CUSTOM)
               - repeatType: 반복 타입 (NONE, DAY, WEEK, MONTH, YEAR)
               - color: 색상 (RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, PINK, GRAY)

               ## 선택 항목
               - notificationCustomHours: 커스텀 알림 시간 (CUSTOM일 때만, 0-23)
               - notificationCustomMinutes: 커스텀 알림 분 (CUSTOM일 때만, 0-59)
               - repeatEndsOn: 반복 종료일
               - location: 위치
               - memo: 메모

               ## 알림 타입
               - NONE: 알림 없음
               - ONE_DAY_BEFORE: 시작일 1일 전 오전 9:00에 알림 (예: 6월 10일 일정 → 6월 9일 09:00 알림)
               - TWO_DAYS_BEFORE: 시작일 2일 전 오전 9:00에 알림 (예: 6월 10일 일정 → 6월 8일 09:00 알림)
               - CUSTOM: startDate 당일 사용자 지정 시간에 알림 (예: 6월 10일 일정, 01:00 설정 → 6월 10일 01:00 알림)
                 * customHours: 알림 시간 (0-23, CUSTOM일 때 필수)
                 * customMinutes: 알림 분 (0-59, CUSTOM일 때 필수)

               **중요:**
               - 알림은 설정 > 기록별 알림에서 '일정 알림'이 활성화된 경우에만 발송됩니다
               - 알림 시간이 이미 지난 경우 해당 알림은 발송되지 않습니다
               - 알림 히스토리에는 일정명(title)이 메시지로 저장됩니다

               ## 반복 타입
               - NONE: 반복 없음 (startDate ~ endDate 범위만 표시)
               - DAY: 매일 반복 (startDate부터 매일 표시)
               - WEEK: 매주 반복 (startDate와 같은 요일에만 표시, 예: 수요일 시작 → 매주 수요일)
               - MONTH: 매월 반복 (startDate와 같은 날짜에만 표시, 예: 15일 시작 → 매월 15일)
                 * 31일 일정은 30일까지 있는 달은 자동 스킵
               - YEAR: 매년 반복 (startDate와 같은 월-일에만 표시, 예: 6월 10일 시작 → 매년 6월 10일)
                 * 2월 29일 일정은 평년은 자동 스킵

               **반복 종료일(repeatEndsOn)**:
               - 설정 시: 해당 날짜까지만 반복 (이후 캘린더/알림 모두 종료)
               - 미설정 시: 계속 반복

               **반복 알림**:
               - 반복 일정의 알림은 반복될 때마다 발송됩니다
               - 예: 매주 수요일 반복 + ONE_DAY_BEFORE → 매주 화요일 09:00 알림

               ## 생성 제한
               - 오늘 생성할 수 있는 일정은 최대 2개입니다
               - 생성 시간(createdAt) 기준으로 판단합니다
               - 생성 가능 여부는 GET /api/daily-records/creation-limits 로 확인할 수 있습니다
               """,
               security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일정 생성 성공"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "일정 생성 제한 초과 또는 입력값 오류",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = {
                @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "일정 생성 제한 초과",
                    summary = "오늘 일정 2개 제한 초과",
                    value = """
                        {
                            "statusCode": 400,
                            "code": "E40414",
                            "message": "오늘 등록할 수 있는 일정은 최대 2개입니다.",
                            "data": null
                        }
                        """
                ),
                @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "입력값 오류",
                    summary = "필수 항목 누락 또는 형식 오류",
                    value = """
                        {
                            "statusCode": 400,
                            "code": "E40001",
                            "message": "잘못된 입력 값입니다.",
                            "data": null
                        }
                        """
                )
            }
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                value = """
                {
                    "statusCode": 401,
                    "code": "E40101",
                    "message": "인증이 필요합니다.",
                    "data": null
                }
                """
            )
        )
    )
    public ResponseEntity<ScheduleResponse> createSchedule(
            Authentication authentication,
            @Valid @RequestBody CreateScheduleRequest request) {
        String userId = authentication.getName();
        ScheduleResponse response = scheduleRecordApplicationService.create(userId, request.toCommand());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{scheduleRecordId}")
    @Operation(summary = "일정 기록 수정",
               description = "기존 일정 기록을 수정합니다.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일정 수정 성공"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "입력값 오류",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                value = """
                    {
                        "statusCode": 400,
                        "code": "E40001",
                        "message": "잘못된 입력 값입니다.",
                        "data": null
                    }
                    """
            )
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                value = """
                {
                    "statusCode": 401,
                    "code": "E40101",
                    "message": "인증이 필요합니다.",
                    "data": null
                }
                """
            )
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "일정을 찾을 수 없음",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                value = """
                    {
                        "statusCode": 404,
                        "code": "E40401",
                        "message": "요청한 자원을 찾을 수 없습니다.",
                        "data": null
                    }
                    """
            )
        )
    )
    public ResponseEntity<ScheduleResponse> updateSchedule(
            Authentication authentication,
            @PathVariable String scheduleRecordId,
            @Valid @RequestBody UpdateScheduleRequest request) {
        String userId = authentication.getName();
        ScheduleResponse response = scheduleRecordApplicationService.update(
            userId, scheduleRecordId, request.toCommand()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{scheduleRecordId}")
    @Operation(summary = "일정 기록 삭제",
               description = "일정 기록을 삭제합니다.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "204",
        description = "일정 삭제 성공"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                value = """
                {
                    "statusCode": 401,
                    "code": "E40101",
                    "message": "인증이 필요합니다.",
                    "data": null
                }
                """
            )
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "일정을 찾을 수 없음",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                value = """
                    {
                        "statusCode": 404,
                        "code": "E40401",
                        "message": "요청한 자원을 찾을 수 없습니다.",
                        "data": null
                    }
                    """
            )
        )
    )
    public ResponseEntity<Void> deleteSchedule(
            Authentication authentication,
            @PathVariable String scheduleRecordId) {
        String userId = authentication.getName();
        scheduleRecordApplicationService.delete(userId, scheduleRecordId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{scheduleRecordId}")
    @Operation(summary = "일정 기록 단건 조회",
               description = "특정 일정 기록을 조회합니다.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일정 조회 성공"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                value = """
                {
                    "statusCode": 401,
                    "code": "E40101",
                    "message": "인증이 필요합니다.",
                    "data": null
                }
                """
            )
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "일정을 찾을 수 없음",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                value = """
                    {
                        "statusCode": 404,
                        "code": "E40401",
                        "message": "요청한 자원을 찾을 수 없습니다.",
                        "data": null
                    }
                    """
            )
        )
    )
    public ResponseEntity<ScheduleResponse> getSchedule(
            Authentication authentication,
            @PathVariable String scheduleRecordId) {
        String userId = authentication.getName();
        ScheduleResponse response = scheduleRecordApplicationService.findById(userId, scheduleRecordId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "날짜 범위별 일정 조회",
               description = """
               특정 날짜 범위 내의 일정을 조회합니다. (캘린더용)

               - 시작일과 종료일을 기준으로 해당 기간에 걸쳐있는 모든 일정을 반환
               - 예: 3월 1일 ~ 3월 31일 조회 시, 2월 28일 ~ 3월 5일 일정도 포함됨
               """,
               security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일정 목록 조회 성공"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "입력값 오류",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                value = """
                    {
                        "statusCode": 400,
                        "code": "E40001",
                        "message": "잘못된 입력 값입니다.",
                        "data": null
                    }
                    """
            )
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                value = """
                {
                    "statusCode": 401,
                    "code": "E40101",
                    "message": "인증이 필요합니다.",
                    "data": null
                }
                """
            )
        )
    )
    public ResponseEntity<List<ScheduleResponse>> getSchedulesByDateRange(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String userId = authentication.getName();
        List<ScheduleResponse> responses = scheduleRecordApplicationService.findByDateRange(
            userId, startDate, endDate
        );
        return ResponseEntity.ok(responses);
    }
}
