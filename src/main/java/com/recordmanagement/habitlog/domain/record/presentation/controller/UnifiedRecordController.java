package com.recordmanagement.habitlog.domain.unified.presentation.controller;

import com.recordmanagement.habitlog.domain.record.application.service.RecordApplicationService;
import com.recordmanagement.habitlog.domain.record.application.dto.DailyRecordResponse;
import com.recordmanagement.habitlog.domain.record.application.dto.UnifiedRecordResponse;
import com.recordmanagement.habitlog.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/records")
@Tag(name = "Daily Overview", description = "일일 통합 기록 조회 API")
public class UnifiedRecordController {
    
    private static final Logger log = LoggerFactory.getLogger(UnifiedRecordController.class);
    
    private final RecordApplicationService recordApplicationService;
    
    public UnifiedRecordController(RecordApplicationService recordApplicationService) {
        this.recordApplicationService = recordApplicationService;
    }
    
    @Operation(summary = "일일 기록 통합 조회", 
               description = """
                   특정 날짜의 모든 타입 기록(일상, 운동, 습관)을 통합하여 조회합니다.
                   
                   ## 캘린더 시스템과 동일한 표시 로직 (v1.8.6)
                   
                   **과거 날짜**: 해당 날짜에 실제로 작성된 모든 기록 반환
                   - 작성된 기록만 포함 (`records: [실제기록객체들]`)
                   - 미작성 시 빈 배열 (`records: []`) - 프론트에서 분기처리
                   - 목적: 플레이스홀더 없이 간단한 데이터 구조
                   
                   **현재 날짜 (오늘)**: 해당 날짜에 작성된 모든 기록 반환
                   - 작성된 기록만 포함 (`records: [실제기록객체들]`)
                   - 미작성 시 빈 배열 (`records: []`)
                   - 습관 기록: isCompleted 상태로 완료/미완료 구분
                   - 홈 화면 제한 체크: `records.length >= 2`로 간단히 처리
                   
                   **미래 날짜**: 빈 배열 반환 (`records: []`)
                   - DB에 미래 자동 생성 기록이 있어도 API에서 제외
                   - 목적: 실제 행동 기반, 미리 계획하지 않음
                   
                   ## 습관 기록 특별 처리 (v1.8.5)
                   - **습관 타입 사용자**: 목표 기간 내 작성된 습관 기록 (오늘까지만)
                   - **운동/일상 타입 사용자**: 모든 날짜의 습관 기록 (서브 기록)
                   - **자동 생성 제외**: DB의 자동 생성 기록은 API 응답에서 제외
                   
                   ## 이미지 URL 처리
                   - 조회 시 자동으로 새로운 Pre-signed URL 생성 (1시간 유효)
                   - 이미지 접근 시마다 최신 URL 제공
                   """,
               security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "일일 기록 통합 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "statusCode": 200,
                        "code": "S200",
                        "message": "일일 기록이 성공적으로 조회되었습니다",
                        "data": {
                            "date": "2025-01-07",
                            "records": [
                                {
                                    "id": "550e8400-e29b-41d4-a716-446655440000",
                                    "type": "DAILY",
                                    "recordDate": "2025-10-27",
                                    "recordTime": "15:21",
                                    "createdAt": "2025-01-07T15:21:00",
                                    "updatedAt": "2025-01-07T15:21:00",
                                    "imageUrls": ["https://example.com/image1.jpg"],
                                    "emotion": "😊",
                                    "content": "오늘은 정말 좋은 하루였습니다."
                                },
                                {
                                    "id": "660e8400-e29b-41d4-a716-446655440001",
                                    "type": "EXERCISE",
                                    "recordDate": "2025-10-27",
                                    "recordTime": "16:30",
                                    "createdAt": "2025-01-07T16:30:00",
                                    "updatedAt": "2025-01-07T16:30:00",
                                    "imageUrls": [],
                                    "exerciseType": "CARDIO",
                                    "caloriesBurned": 300,
                                    "exerciseTimeMinutes": 30,
                                    "stepCount": 5000,
                                    "weight": 70.5,
                                    "dailyNote": "오늘 운동 너무 힘들었지만 뿌듯해요!"
                                },
                                {
                                    "id": "770e8400-e29b-41d4-a716-446655440002",
                                    "type": "HABIT",
                                    "recordDate": "2025-10-27",
                                    "recordTime": null,
                                    "createdAt": "2025-01-07T09:00:00",
                                    "updatedAt": "2025-01-07T09:00:00",
                                    "imageUrls": null,
                                    "habitType": "WATER",
                                    "notificationEnabled": true,
                                    "notificationTime": "09:00:00",
                                    "memo": "물 2L 달성! (실제 작성된 메인 습관 기록)",
                                    "isCompleted": true
                                }
                            ]
                        }
                    }
                    """)
            )
        ),
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
    })
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<DailyRecordResponse>> getRecordsByDate(
            @PathVariable LocalDate date,
            Authentication authentication) {
        
        log.info("일일 기록 통합 조회 요청: date=[{}]", date);
        
        String userIdValue = authentication.getName();
        
        DailyRecordResponse response = recordApplicationService.getRecordsByDate(userIdValue, date);
        
        log.info("일일 기록 통합 조회 완료: date=[{}], records count=[{}]", 
                date, response.records().size());
        
        return ResponseEntity.ok(ApiResponse.success("일일 기록이 성공적으로 조회되었습니다", response));
    }
    
    @Operation(summary = "개별 기록 조회", 
               description = """
                   기록 ID로 개별 기록의 상세 정보를 조회합니다.
                   일상 기록과 운동 기록을 모두 지원합니다.
                   
                   **이미지 URL 처리:**
                   - 조회 시 자동으로 새로운 Pre-signed URL이 생성됩니다 (1시간 유효)
                   """,
               security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "기록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "statusCode": 200,
                        "code": "S200",
                        "message": "기록이 성공적으로 조회되었습니다",
                        "data": {
                            "id": "550e8400-e29b-41d4-a716-446655440000",
                            "type": "DAILY",
                            "recordDate": "2025-10-27",
                            "recordTime": "15:21",
                            "createdAt": "2025-01-07T15:21:00",
                            "updatedAt": "2025-01-07T15:21:00",
                            "imageUrls": ["https://example.com/image1.jpg"],
                            "emotion": "😊",
                            "content": "오늘은 정말 좋은 하루였습니다."
                        }
                    }
                    """)
            )
        ),
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
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "접근 권한 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "statusCode": 403,
                        "code": "E40305",
                        "message": "해당 기록에 접근할 권한이 없습니다",
                        "data": null
                    }
                    """)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "기록을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "statusCode": 404,
                        "code": "E40406",
                        "message": "존재하지 않는 기록입니다",
                        "data": null
                    }
                    """)
            )
        )
    })
    @GetMapping("/{recordId}")
    public ResponseEntity<ApiResponse<UnifiedRecordResponse>> getRecordById(
            @PathVariable String recordId,
            Authentication authentication) {
        
        log.info("개별 기록 조회 요청: recordId=[{}]", recordId);
        
        String userIdValue = authentication.getName();
        
        UnifiedRecordResponse response = recordApplicationService.getRecordById(userIdValue, recordId);
        
        log.info("개별 기록 조회 완료: recordId=[{}], type=[{}]", recordId, response.type());
        
        return ResponseEntity.ok(ApiResponse.success("기록이 성공적으로 조회되었습니다", response));
    }
    
}