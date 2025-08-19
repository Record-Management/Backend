package com.recordmanagement.habitlog.api.record;

import com.recordmanagement.habitlog.application.record.DailyRecordApplicationService;
import com.recordmanagement.habitlog.application.record.dto.DailyRecordCreateCommand;
import com.recordmanagement.habitlog.application.record.dto.DailyRecordResponse;
import com.recordmanagement.habitlog.common.response.ApiResponse;
import com.recordmanagement.habitlog.api.record.dto.DailyRecordCreateRequest;
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

/**
 * 일상 기록 관련 API 컨트롤러
 * 
 * 사용자의 일상 기록 CRUD 작업을 처리하는 REST API 컨트롤러입니다.
 * 달력 화면 하단에서 일상 기록을 작성, 수정, 삭제할 수 있는 기능을 제공합니다.
 * 
 * API 엔드포인트:
 * - POST   /api/daily-records/{date}   : 특정 날짜 일상기록 생성/수정
 * - DELETE /api/daily-records/{date}   : 특정 날짜 일상기록 삭제
 * 
 * 주요 특징:
 * - JWT 인증 필수 (Bearer 토큰)
 * - 하루에 하나의 일상기록만 허용 (중복 생성 시 자동 수정)
 * - 날짜 형식: YYYY-MM-DD (ISO 8601)
 * - 기분 상태는 필수 입력
 * - 제목, 내용, 이미지는 선택 입력
 * 
 * 응답 형식:
 * - 성공: ApiResponse<DailyRecordResponse> (200 OK)
 * - 실패: ApiResponse<Void> (400/401/404/500)
 * 
 * 예외 처리:
 * - 잘못된 날짜 형식: INVALID_DATE_FORMAT (400)
 * - 미래 날짜 기록: FUTURE_DATE_RECORD_NOT_ALLOWED (400)
 * - 인증 실패: UNAUTHORIZED (401)
 * - 기록 없음: DAILY_RECORD_NOT_FOUND (404)
 * 
 * @author 전우선
 * @since 2025.08.19
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/daily-records")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "일상 기록", description = "일상 기록 관련 API")
public class DailyRecordController {

    private final DailyRecordApplicationService dailyRecordApplicationService;

    /**
     * 특정 날짜의 일상 기록 생성 또는 수정
     * 
     * 달력 화면에서 특정 날짜를 선택하고 하단에서 일상 기록을 작성/수정하는 API입니다.
     * 
     * 처리 로직:
     * 1. 요청 날짜의 기존 일상 기록 존재 여부 확인
     * 2. 기존 기록이 있으면 수정, 없으면 새로 생성
     * 3. 기분 상태는 필수이며, 나머지는 선택 사항
     * 4. 수정 시간 자동 갱신
     * 
     * @param date 기록 날짜 (Path Variable, YYYY-MM-DD 형식)
     * @param request 일상 기록 생성/수정 요청 데이터
     * @param userDetails JWT 토큰에서 추출한 사용자 인증 정보
     * @return ApiResponse<DailyRecordResponse> 생성/수정된 일상 기록 정보
     */
    @Operation(
        summary = "일상 기록 생성/수정",
        description = "일상 기록을 생성하거나 수정합니다. 하루에 하나의 일상 기록만 가능합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/{date}")
    public ApiResponse<DailyRecordResponse> createOrUpdateDailyRecord(
            @PathVariable String date,
            @Valid @RequestBody DailyRecordCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("일상 기록 생성/수정 요청: userId={}, date={}", 
                userDetails.getUsername(), date);
        
        DailyRecordCreateCommand command = new DailyRecordCreateCommand(
                userDetails.getUsername(),
                LocalDate.parse(date),
                request.getMood(),
                request.getTitle(),
                request.getContent(),
                request.getImageUrl()
        );
        
        DailyRecordResponse response = dailyRecordApplicationService.createOrUpdateDailyRecord(command);
        
        return ApiResponse.success(response);
    }

    @Operation(
        summary = "일상 기록 삭제",
        description = "특정 날짜의 일상 기록을 삭제합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @DeleteMapping("/{date}")
    public ApiResponse<Void> deleteDailyRecord(
            @PathVariable String date,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("일상 기록 삭제 요청: userId={}, date={}", 
                userDetails.getUsername(), date);
        
        dailyRecordApplicationService.deleteDailyRecord(
                userDetails.getUsername(),
                LocalDate.parse(date)
        );
        
        return ApiResponse.success(null);
    }
}