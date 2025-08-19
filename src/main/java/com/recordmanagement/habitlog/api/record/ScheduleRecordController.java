package com.recordmanagement.habitlog.api.record;

import com.recordmanagement.habitlog.application.record.ScheduleRecordApplicationService;
import com.recordmanagement.habitlog.application.record.dto.ScheduleRecordCreateCommand;
import com.recordmanagement.habitlog.application.record.dto.ScheduleRecordResponse;
import com.recordmanagement.habitlog.common.response.ApiResponse;
import com.recordmanagement.habitlog.api.record.dto.ScheduleRecordCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 일정 기록 관련 API 컨트롤러
 * 
 * 사용자의 개인 일정을 관리하는 REST API를 제공합니다.
 * 회의, 약속, 개인 스케줄 등 다양한 일정을 생성하고 관리할 수 있습니다.
 * 
 * 주요 기능:
 * - 일정 기록 생성
 * - 일정 기록 수정
 * - 일정 기록 삭제
 * - 특정 날짜의 일정 조회
 * - 일정 목록 조회
 * 
 * 지원하는 일정 타입:
 * - MEETING: 회의
 * - APPOINTMENT: 약속
 * - PERSONAL: 개인 일정
 * - WORK: 업무
 * - STUDY: 공부
 * - EXERCISE: 운동
 * - TRAVEL: 여행
 * - HEALTHCARE: 의료/건강
 * - SOCIAL: 사교 활동
 * - OTHER: 기타
 * 
 * 일정 속성:
 * - 제목 (필수)
 * - 일정 타입 (필수)
 * - 시작/종료 날짜
 * - 시작/종료 시간
 * - 메모 (선택)
 * 
 * 인증 요구사항:
 * - 모든 API는 JWT Bearer 토큰 인증 필요
 * - 사용자는 본인의 일정만 접근 가능
 * 
 * @author 전우선
 * @since 2025.08.19
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/schedule-records")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "일정 기록", description = "일정 기록 관련 API")
public class ScheduleRecordController {

    private final ScheduleRecordApplicationService scheduleRecordApplicationService;

    @Operation(
        summary = "일정 기록 생성",
        description = "새로운 일정 기록을 생성합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping
    public ApiResponse<ScheduleRecordResponse> createScheduleRecord(
            @Valid @RequestBody ScheduleRecordCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("일정 기록 생성 요청: userId={}", userDetails.getUsername());
        
        ScheduleRecordCreateCommand command = new ScheduleRecordCreateCommand(
                userDetails.getUsername(),
                request.getTitle(),
                request.getScheduleType(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getMemo()
        );
        
        ScheduleRecordResponse response = scheduleRecordApplicationService.createScheduleRecord(command);
        
        return ApiResponse.success(response);
    }

    @Operation(
        summary = "일정 기록 수정",
        description = "기존 일정 기록을 수정합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PutMapping("/{scheduleId}")
    public ApiResponse<ScheduleRecordResponse> updateScheduleRecord(
            @PathVariable String scheduleId,
            @Valid @RequestBody ScheduleRecordCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("일정 기록 수정 요청: userId={}, scheduleId={}", 
                userDetails.getUsername(), scheduleId);
        
        ScheduleRecordResponse response = scheduleRecordApplicationService.updateScheduleRecord(
                scheduleId,
                userDetails.getUsername(),
                request.getTitle(),
                request.getScheduleType(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getMemo()
        );
        
        return ApiResponse.success(response);
    }

    @Operation(
        summary = "일정 기록 삭제",
        description = "일정 기록을 삭제합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @DeleteMapping("/{scheduleId}")
    public ApiResponse<Void> deleteScheduleRecord(
            @PathVariable String scheduleId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("일정 기록 삭제 요청: userId={}, scheduleId={}", 
                userDetails.getUsername(), scheduleId);
        
        scheduleRecordApplicationService.deleteScheduleRecord(scheduleId, userDetails.getUsername());
        
        return ApiResponse.success(null);
    }

    @Operation(
        summary = "특정 날짜의 일정 기록 조회",
        description = "특정 날짜의 모든 일정 기록을 조회합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/{date}")
    public ApiResponse<List<ScheduleRecordResponse>> getScheduleRecordsByDate(
            @PathVariable String date,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("특정 날짜 일정 기록 조회 요청: userId={}, date={}", 
                userDetails.getUsername(), date);
        
        List<ScheduleRecordResponse> response = scheduleRecordApplicationService.getScheduleRecordsByDate(
                userDetails.getUsername(), date);
        
        return ApiResponse.success(response);
    }
}