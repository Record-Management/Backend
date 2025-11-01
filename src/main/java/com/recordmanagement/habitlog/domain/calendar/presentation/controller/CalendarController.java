package com.recordmanagement.habitlog.domain.calendar.presentation.controller;

import com.recordmanagement.habitlog.domain.record.application.service.RecordApplicationService;
import com.recordmanagement.habitlog.domain.record.application.dto.CalendarResponse;
import com.recordmanagement.habitlog.global.common.response.ApiResponse;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@Tag(name = "Calendar", description = "캘린더 관련 API")
public class CalendarController {
    
    private static final Logger log = LoggerFactory.getLogger(CalendarController.class);
    
    private final RecordApplicationService recordApplicationService;
    
    public CalendarController(RecordApplicationService recordApplicationService) {
        this.recordApplicationService = recordApplicationService;
    }
    
    @Operation(summary = "캘린더 조회", description = """
            월별 기록 현황을 조회합니다. 타입별 필터링이 가능합니다.
            
            ### 🆕 자동 습관 슬롯 생성 (v1.3.0)
            - 메인 기록 타입이 HABIT인 사용자의 경우, 습관 목표 기간 전체에 걸쳐 자동으로 습관 슬롯이 생성됩니다
            - 실제 습관 기록이 없는 날짜에는 플레이스홀더 기록이 표시되어 목표 달성 현황을 명확히 볼 수 있습니다
            - 플레이스홀더 기록은 isMainRecord: true, isCompleted: false로 표시됩니다
            """,
            security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "캘린더 조회 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "statusCode": 200,
                    "code": "S200",
                    "message": "캘린더가 성공적으로 조회되었습니다",
                    "data": {
                        "year": 2025,
                        "month": 1,
                        "monthlyRecords": [
                            {
                                "date": "2025-01-05",
                                "records": [
                                    {
                                        "id": "placeholder-user123-2025-01-05",
                                        "type": "HABIT",
                                        "isMainRecord": true,
                                        "isCompleted": false
                                    }
                                ]
                            },
                            {
                                "date": "2025-01-07",
                                "records": [
                                    {
                                        "id": "550e8400-e29b-41d4-a716-446655440000",
                                        "type": "DAILY"
                                    },
                                    {
                                        "id": "habit-real-id-123",
                                        "type": "HABIT",
                                        "isMainRecord": true,
                                        "isCompleted": true
                                    }
                                ]
                            }
                        ]
                    }
                }
                """)
        )
    )
    @GetMapping("/{year}/{month}")
    public ResponseEntity<ApiResponse<CalendarResponse>> getCalendar(
            @PathVariable int year,
            @PathVariable int month,
            @Parameter(description = "필터링할 기록 타입. 미지정시 모든 타입 조회", example = "DAILY") 
            @RequestParam(required = false) RecordType type,
            Authentication authentication) {
        
        log.info("캘린더 조회 요청: year=[{}], month=[{}], type=[{}]", year, month, type);
        
        String userIdValue = authentication.getName();
        
        CalendarResponse response = recordApplicationService.getCalendar(userIdValue, year, month, type);
        
        log.info("캘린더 조회 완료: year=[{}], month=[{}], type=[{}], records count=[{}]", 
                year, month, type, response.monthlyRecords().size());
        
        return ResponseEntity.ok(ApiResponse.success("캘린더가 성공적으로 조회되었습니다", response));
    }
}