package com.recordmanagement.habitlog.api.calendar;

import com.recordmanagement.habitlog.application.calendar.CalendarApplicationService;
import com.recordmanagement.habitlog.application.calendar.dto.DailyRecordsResponse;
import com.recordmanagement.habitlog.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 달력 관련 API 컨트롤러
 * 
 * 달력 화면에서 사용하는 통합 기록 조회 기능을 제공합니다.
 * 특정 날짜의 모든 기록 타입을 한 번에 조회하여 달력 UI에 표시할 수 있습니다.
 * 
 * 주요 기능:
 * - 특정 날짜의 통합 기록 조회
 * - 일상/습관/일정 기록 통합 반환
 * - 달력 화면 최적화된 데이터 구조 제공
 * 
 * 조회 대상 기록 타입:
 * - 일상 기록 (DailyRecord): 기분, 제목, 내용, 이미지
 * - 습관 기록 (HabitRecord): 완료된 습관 목록
 * - 일정 기록 (ScheduleRecord): 해당 날짜의 스케줄
 * 
 * 제외 대상:
 * - 운동 기록: 글쓰기 화면에서만 사용되므로 달력에서 제외
 * 
 * 응답 데이터 구조:
 * - 각 기록 타입별 존재 여부 플래그
 * - 기록 요약 정보 (제목, 완료 개수 등)
 * - 달력 UI에서 즉시 표시 가능한 형태
 * 
 * 사용 시나리오:
 * 1. 사용자가 달력 화면에 진입
 * 2. 각 날짜별로 이 API 호출
 * 3. 기록 존재 여부에 따라 달력에 표시
 * 4. 사용자가 특정 날짜 클릭 시 상세 정보 표시
 * 
 * 인증 요구사항:
 * - JWT Bearer 토큰 인증 필요
 * - 사용자는 본인의 기록만 조회 가능
 * 
 * @author 전우선
 * @since 2025.08.19
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "달력", description = "달력 관련 API")
public class CalendarController {

    private final CalendarApplicationService calendarApplicationService;

    @Operation(
        summary = "특정 날짜의 모든 기록 조회",
        description = "달력에서 선택한 날짜의 일상/일정/습관 기록을 조회합니다. (운동 기록 제외)",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/{date}")
    public ApiResponse<DailyRecordsResponse> getDailyRecords(
            @PathVariable String date,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("특정 날짜 기록 조회 요청: userId={}, date={}", 
                userDetails.getUsername(), date);
        
        DailyRecordsResponse response = calendarApplicationService.getDailyRecords(
                userDetails.getUsername(),
                LocalDate.parse(date)
        );
        
        return ApiResponse.success(response);
    }
}