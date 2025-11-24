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
            
            ### 캘린더 표시 로직 (v1.8.6) - 프론트 분기처리 최적화
            
            **과거 날짜**: 작성된 기록만 표시, 미작성은 빈 배열
            - 작성된 기록: `records: [실제기록객체]` → 색상 아이콘
            - 미작성 기록: `records: []` → 프론트에서 `length === 0 && 과거날짜`로 회색 아이콘 처리
            - 장점: 플레이스홀더 객체 없어서 타입 분기처리 불필요
            
            **현재 날짜 (오늘)**: 작성된 기록만 표시, 미작성은 빈 배열
            - 작성된 기록: `records: [실제기록객체]` → 색상/회색 아이콘
            - 미작성 기록: `records: []` → 프론트에서 `length === 0 && 현재날짜`로 빈 공간 처리
            - 습관: 3단계 시스템 (미작성=빈공간, 작성=isCompleted:false, 완료=isCompleted:true)
            - 홈 화면: `records.length >= 2`로 간단한 작성 제한 체크
            
            **미래 날짜**: 완전 빈 배열
            - `records: []` → 프론트에서 `length === 0 && 미래날짜`로 빈 공간 처리
            - DB에 미래 자동생성 기록이 있어도 API 응답에서 제외
            
            ### 메인 기록 타입 표시 (v1.9.0)
            - 각 날짜에 `mainRecordTypeForDate` 필드가 추가되어 해당 날짜의 메인 기록 타입을 표시합니다
            - **목표 기간 내**: 해당 목표의 메인 기록 타입 반환 (HABIT/EXERCISE/DAILY)
            - **목표 미설정 기간**: `null` 반환 (목표가 설정되지 않은 기간)
            - 클라이언트는 이 값을 기준으로 캘린더 아이콘을 결정할 수 있습니다
            
            ### 습관 기록 특별 처리 (v1.9.1)
            - **메인 습관 자동 생성**: 메인 습관 기록 생성 시 목표 종료일까지 DB에 자동 생성
            - **오늘까지만 응답**: DB의 미래 기록은 API에서 제외 (실제 행동 기반)
            - **메인 습관 표시 로직** (mainRecordTypeForDate = HABIT인 경우):
              * 미작성 (자동생성 그대로): records 빈 배열
              * 작성 (사용자 수정): isCompleted=false로 표시  
              * 완료: isCompleted=true로 표시
            - **다른 타입 사용자**: 모든 습관 기록 표시 (서브 기록으로)
            
            ### isCompleted 필드 상태
            - **습관 기록**: 실제 완료 체크 여부 (true=완료, false=미완료, null=미작성)
            - **일상/운동 기록**: 기록 존재 자체가 완료 (항상 true)
            
            ### 사용자 타입별 특징
            **습관 타입 사용자 (HABIT)**:
            - 과거: 모든 습관 기록 + 미작성 회색 표시
            - 현재: 작성된 습관만 표시 (3단계: 빈공간→회색→색상)
            - 미래: 표시하지 않음
            
            **운동/일상 타입 사용자 (EXERCISE/DAILY)**:
            - 과거: 모든 기록 + 미작성 회색 표시  
            - 현재: 작성된 기록만 표시
            - 미래: 표시하지 않음
            - 메인 기록 + 서브 습관 기록 조합 가능
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
                    summary = "HABIT 타입 사용자 - 프론트 분기처리 최적화 (빈 배열 활용)",
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
                                    "date": "2025-11-17",
                                    "mainRecordTypeForDate": "HABIT",
                                    "records": [
                                        {
                                            "id": "habit_record_17_completed",
                                            "type": "HABIT",
                                            "isCompleted": true
                                        }
                                    ]
                                },
                                {
                                    "date": "2025-11-18",
                                    "mainRecordTypeForDate": "HABIT",
                                    "records": []
                                },
                                {
                                    "date": "2025-11-19",
                                    "mainRecordTypeForDate": "HABIT",
                                    "records": [
                                        {
                                            "id": "habit_record_19_today",
                                            "type": "HABIT",
                                            "isCompleted": false
                                        }
                                    ]
                                },
                                {
                                    "date": "2025-11-20",
                                    "mainRecordTypeForDate": "HABIT",
                                    "records": []
                                }
                            ]
                        }
                    }
                    """
                ),
                @ExampleObject(
                    name = "운동 타입 사용자 캘린더", 
                    summary = "EXERCISE 타입 사용자 - 빈 배열로 미작성 상태 표시",
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
                                    "date": "2025-11-17",
                                    "mainRecordTypeForDate": "EXERCISE",
                                    "records": [
                                        {
                                            "id": "exercise_record_17",
                                            "type": "EXERCISE",
                                            "isCompleted": true
                                        },
                                        {
                                            "id": "habit_record_17",
                                            "type": "HABIT",
                                            "isCompleted": true
                                        }
                                    ]
                                },
                                {
                                    "date": "2025-11-18",
                                    "mainRecordTypeForDate": "EXERCISE",
                                    "records": []
                                },
                                {
                                    "date": "2025-11-19",
                                    "mainRecordTypeForDate": "EXERCISE",
                                    "records": [
                                        {
                                            "id": "habit_record_19",
                                            "type": "HABIT",
                                            "isCompleted": false
                                        }
                                    ]
                                },
                                {
                                    "date": "2025-11-20",
                                    "mainRecordTypeForDate": "EXERCISE",
                                    "records": []
                                }
                            ]
                        }
                    }
                    """
                ),
                @ExampleObject(
                    name = "목표 미설정 기간 캘린더",
                    summary = "목표가 설정되지 않은 기간 - mainRecordTypeForDate가 null",
                    value = """
                    {
                        "statusCode": 200,
                        "code": "S200",
                        "message": "캘린더가 성공적으로 조회되었습니다",
                        "data": {
                            "year": 2025,
                            "month": 10,
                            "monthlyRecords": [
                                {
                                    "date": "2025-10-22",
                                    "mainRecordTypeForDate": null,
                                    "records": [
                                        {
                                            "id": "daily_record_22",
                                            "type": "DAILY",
                                            "isCompleted": true
                                        }
                                    ]
                                },
                                {
                                    "date": "2025-10-23",
                                    "mainRecordTypeForDate": null,
                                    "records": []
                                },
                                {
                                    "date": "2025-11-08",
                                    "mainRecordTypeForDate": null,
                                    "records": [
                                        {
                                            "id": "habit_record_08",
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