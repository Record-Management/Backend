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
            
            ### 🎯 메인 기록 타입 표시 (v1.6.0)
            - 각 날짜에 `mainRecordTypeForDate` 필드가 추가되어 해당 날짜의 메인 기록 타입을 표시합니다
            - 클라이언트는 이 값을 기준으로 캘린더 아이콘을 결정할 수 있습니다
            - 목표 설정 시점에 결정된 메인 기록 타입이 목표 기간 동안 일관되게 유지됩니다
            
            ### 🆕 자동 메인 습관 기록 시스템 (v1.5.0)
            - 메인 기록 타입이 HABIT인 사용자의 경우, 습관 목표 기간 전체에 메인 습관 기록이 자동 생성됩니다
            - 사용자가 메인 습관을 등록하거나 HABIT 타입으로 목표를 변경하면 남은 기간에 실제 DB 기록이 생성됩니다
            - 자동 생성된 기록은 isMainRecord: true, isCompleted: false로 시작하며 사용자가 완료 처리 가능합니다
            - 메인 습관 기록 수정시 남은 기간의 모든 메인 기록이 동일한 습관 타입으로 자동 업데이트됩니다
            
            ### 🔧 습관 포기 지원 (v1.5.0)
            - 모든 습관 기록을 삭제하면 캘린더에서 플레이스홀더 습관 기록이 완전히 제거됩니다
            - 습관을 다시 시작하고 싶으면 새로운 습관 기록을 등록하면 자동으로 플레이스홀더가 다시 생성됩니다
            
            ### 📋 HABIT 타입 사용자의 캘린더 특징
            - 목표 기간 전체에 메인 습관 기록이 자동 생성됨
            - 사용자가 완료 처리하면 isCompleted가 true로 변경
            - 메인 습관 수정시 남은 기간의 모든 기록이 동기화됨
            - 모든 습관 기록 삭제시 플레이스홀더가 완전히 제거됨 (습관 포기)
            """,
            security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "캘린더 조회 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "자동 생성된 메인 습관 기록 포함 캘린더",
                summary = "HABIT 타입 사용자의 캘린더 - mainRecordTypeForDate 필드 포함",
                value = """
                {
                    "statusCode": 200,
                    "code": "S200",
                    "message": "캘린더가 성공적으로 조회되었습니다",
                    "data": {
                        "year": 2025,
                        "month": 11,
                        "monthlyRecords": [
                            {
                                "date": "2025-11-01",
                                "mainRecordTypeForDate": "HABIT",
                                "records": [
                                    {
                                        "id": "habit_record_001",
                                        "type": "HABIT"
                                    }
                                ]
                            },
                            {
                                "date": "2025-11-02",
                                "mainRecordTypeForDate": "HABIT",
                                "records": [
                                    {
                                        "id": "habit_record_002",
                                        "type": "HABIT"
                                    }
                                ]
                            },
                            {
                                "date": "2025-11-03",
                                "mainRecordTypeForDate": "HABIT",
                                "records": [
                                    {
                                        "id": "daily_record_001",
                                        "type": "DAILY"
                                    },
                                    {
                                        "id": "habit_record_003",
                                        "type": "HABIT"
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