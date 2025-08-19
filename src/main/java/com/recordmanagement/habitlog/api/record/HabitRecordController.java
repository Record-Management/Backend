package com.recordmanagement.habitlog.api.record;

import com.recordmanagement.habitlog.application.record.HabitRecordApplicationService;
import com.recordmanagement.habitlog.application.record.dto.HabitRecordCreateCommand;
import com.recordmanagement.habitlog.application.record.dto.HabitRecordResponse;
import com.recordmanagement.habitlog.common.response.ApiResponse;
import com.recordmanagement.habitlog.api.record.dto.HabitRecordCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * 습관 기록 관련 API 컨트롤러
 * 
 * 사용자의 일상 습관 기록을 관리하는 REST API를 제공합니다.
 * 물 마시기, 운동, 독서 등 다양한 습관의 완료 여부를 추적하고 관리할 수 있습니다.
 * 
 * 주요 기능:
 * - 습관 기록 생성 및 수정
 * - 습관 기록 삭제
 * - 특정 날짜의 습관 기록 조회
 * - 습관 완료 상태 토글
 * 
 * 지원하는 습관 타입:
 * - WATER: 물 마시기
 * - WALK: 산책하기
 * - EXERCISE: 운동하기
 * - READING: 독서하기
 * - MEDITATION: 명상하기
 * - SLEEP_EARLY: 일찍 자기
 * - NO_SMOKING: 금연하기
 * - NO_DRINKING: 금주하기
 * - HEALTHY_EATING: 건강한 식사
 * - STUDY: 공부하기
 * 
 * 인증 요구사항:
 * - 모든 API는 JWT Bearer 토큰 인증 필요
 * - 사용자는 본인의 습관 기록만 접근 가능
 * 
 * @author 전우선
 * @since 2025.08.19
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/habit-records")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "습관 기록", description = "습관 기록 관련 API")
public class HabitRecordController {

    private final HabitRecordApplicationService habitRecordApplicationService;

    @Operation(
        summary = "습관 기록 생성/수정",
        description = "특정 날짜의 습관 기록을 생성하거나 수정합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/{date}")
    public ApiResponse<HabitRecordResponse> createOrUpdateHabitRecord(
            @PathVariable String date,
            @Valid @RequestBody HabitRecordCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("습관 기록 생성/수정 요청: userId={}, date={}, habitType={}", 
                userDetails.getUsername(), date, request.getHabitType());
        
        HabitRecordCreateCommand command = new HabitRecordCreateCommand(
                userDetails.getUsername(),
                LocalDate.parse(date),
                request.getHabitType(),
                request.isCompleted(),
                request.getMemo()
        );
        
        HabitRecordResponse response = habitRecordApplicationService.createOrUpdateHabitRecord(command);
        
        return ApiResponse.success(response);
    }

    @Operation(
        summary = "습관 기록 삭제",
        description = "특정 날짜의 특정 습관 기록을 삭제합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @DeleteMapping("/{date}/{habitType}")
    public ApiResponse<Void> deleteHabitRecord(
            @PathVariable String date,
            @PathVariable String habitType,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("습관 기록 삭제 요청: userId={}, date={}, habitType={}", 
                userDetails.getUsername(), date, habitType);
        
        habitRecordApplicationService.deleteHabitRecord(
                userDetails.getUsername(),
                LocalDate.parse(date),
                habitType
        );
        
        return ApiResponse.success(null);
    }

    @Operation(
        summary = "특정 날짜의 습관 기록 조회",
        description = "특정 날짜의 모든 습관 기록을 조회합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/{date}")
    public ApiResponse<List<HabitRecordResponse>> getHabitRecordsByDate(
            @PathVariable String date,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("특정 날짜 습관 기록 조회 요청: userId={}, date={}", 
                userDetails.getUsername(), date);
        
        List<HabitRecordResponse> response = habitRecordApplicationService.getHabitRecordsByDate(
                userDetails.getUsername(), LocalDate.parse(date));
        
        return ApiResponse.success(response);
    }

    @Operation(
        summary = "습관 완료 상태 토글",
        description = "특정 습관의 완료 상태를 토글합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PatchMapping("/{date}/{habitType}/toggle")
    public ApiResponse<HabitRecordResponse> toggleHabitCompletion(
            @PathVariable String date,
            @PathVariable String habitType,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("습관 완료 상태 토글 요청: userId={}, date={}, habitType={}", 
                userDetails.getUsername(), date, habitType);
        
        HabitRecordResponse response = habitRecordApplicationService.toggleHabitCompletion(
                userDetails.getUsername(),
                LocalDate.parse(date),
                habitType
        );
        
        return ApiResponse.success(response);
    }
}