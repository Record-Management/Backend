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
               - repeatEndsOn: 반복 종료일
               - location: 위치
               - memo: 메모

               ## 알림 타입
               - NONE: 알림 없음
               - ONE_DAY_BEFORE: 1일 전 오전 9시
               - TWO_DAYS_BEFORE: 2일 전 오전 9시
               - CUSTOM: 사용자 지정 (startDate 당일 customHours시에 알림, customHours 필수)

               ## 반복 타입
               - NONE: 반복 없음
               - DAY: 매일
               - WEEK: 매주
               - MONTH: 매월 (31일 일정은 30일까지 있는 달은 누락)
               - YEAR: 매년 (2월 29일 일정은 평년은 누락)
               """,
               security = @SecurityRequirement(name = "bearerAuth"))
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
