package com.recordmanagement.habitlog.domain.calendar.presentation.controller;

import com.recordmanagement.habitlog.domain.record.application.dto.CalendarResponse;
import com.recordmanagement.habitlog.domain.record.application.service.RecordApplicationService;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import com.recordmanagement.habitlog.global.common.response.ApiResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            
            ### 메인 기록 타입 표시 (v1.6.0)
            - 각 날짜에 `mainRecordTypeForDate` 필드가 추가되어 해당 날짜의 메인 기록 타입을 표시합니다
            - 클라이언트는 이 값을 기준으로 캘린더 아이콘을 결정할 수 있습니다
            - 목표 설정 시점에 결정된 메인 기록 타입이 목표 기간 동안 일관되게 유지됩니다
            
            ### 습관 기록 필터링 (v1.7.0)
            - **습관 타입 사용자**: 습관 목표 기간(시작일~종료일) 내의 습관 기록만 조회
            - **운동/일상 타입 사용자**: 모든 습관 기록 조회 (서브 기록으로 사용)
            - **과거 데이터 제외**: 목표 기간 이전의 습관 기록은 캘린더에 표시되지 않음
            - **정확한 아이콘 표시**: records 배열에 목표 기간 내 기록만 포함되어 올바른 메인 기록 판별
            
            ### 실제 행동 기반 캘린더 시스템 (v1.8.2)
            - **일상/운동과 동일한 UX**: 사용자가 실제 행동(완료 체크 등)을 할 때만 캘린더에 표시
            - **미래 날짜 생성 제거**: 습관 등록 시 미래 날짜까지 미리 생성하지 않음
            - **현재 날짜만 생성**: 습관 등록은 현재 날짜에만 기록 생성
            - **완료 체크 시**: 해당 날짜에 주황색 아이콘 표시
            
            ### 습관 타입 사용자 특별 표시 로직 (v1.8.3)
            - **모든 날짜 표시**: 작성된 모든 습관 기록을 캘린더에 표시 (과거/현재)
            - **2단계 시스템**: 습관 등록(회색) → 완료 처리(색상)
            - **미래 미생성**: 미래 날짜는 아예 생성하지 않음
            
            ### isCompleted 필드 (v1.8.0)
            - **모든 기록 타입**: `isCompleted` 필드로 완료 상태 명시
            - **습관 기록**: 실제 완료 체크 여부 (true/false)
            - **일상/운동 기록**: 기록 존재 자체가 완료 (항상 true)
            
            ### 사용자 타입별 캘린더 특징
            **습관 타입 사용자 (HABIT)**:
            - **모든 날짜 표시**: 작성된 모든 습관 기록을 캘린더에 표시 (과거/현재)
            - **2단계 시스템**: 등록(회색) → 완료(색상)
            - **미래 미생성**: 미래 날짜는 아예 생성하지 않음
            
            **운동/일상 타입 사용자 (EXERCISE/DAILY)**:
            - **모든 날짜 표시**: 과거/현재 모든 기록 표시
            - **메인+서브 조합**: 메인 기록(운동/일상) + 서브 습관 기록 가능
            - **자유로운 습관**: 습관 목표 기간 제한 없이 서브 기록으로 사용
            - **완료: 색상, 미완료: 회색, 기록 없음: 표시 안함**
            """,
            security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "캘린더 조회 성공",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "습관 타입 사용자 캘린더",
                    summary = "HABIT 타입 사용자 - 과거/현재 모든 작성된 습관 기록 표시",
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
                                    "date": "2025-11-12",
                                    "mainRecordTypeForDate": "HABIT",
                                    "records": [
                                        {
                                            "id": "habit_record_past",
                                            "type": "HABIT",
                                            "isCompleted": true
                                        }
                                    ]
                                },
                                {
                                    "date": "2025-11-14",
                                    "mainRecordTypeForDate": "HABIT",
                                    "records": [
                                        {
                                            "id": "habit_record_today",
                                            "type": "HABIT",
                                            "isCompleted": false
                                        }
                                    ]
                                }
                            ]
                        }
                    }
                    """
                ),
                @ExampleObject(
                    name = "운동 타입 사용자 캘린더",
                    summary = "EXERCISE 타입 사용자 - 메인 운동 + 서브 습관 조합, 과거 습관 기록 제외",
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
                                    "date": "2025-11-05",
                                    "mainRecordTypeForDate": "EXERCISE",
                                    "records": [
                                        {
                                            "id": "exercise_record_001",
                                            "type": "EXERCISE",
                                            "isCompleted": true
                                        },
                                        {
                                            "id": "habit_record_001",
                                            "type": "HABIT",
                                            "isCompleted": true
                                        }
                                    ]
                                },
                                {
                                    "date": "2025-11-06",
                                    "mainRecordTypeForDate": "EXERCISE",
                                    "records": [
                                        {
                                            "id": "habit_record_002",
                                            "type": "HABIT",
                                            "isCompleted": false
                                        }
                                    ]
                                }
                            ]
                        }
                    }
                    """
                )
            }
        )
    )
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