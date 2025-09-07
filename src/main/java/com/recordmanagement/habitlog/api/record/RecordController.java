package com.recordmanagement.habitlog.api.record;

import com.recordmanagement.habitlog.api.record.dto.CreateRecordRequest;
import com.recordmanagement.habitlog.api.record.dto.UpdateRecordRequest;
import com.recordmanagement.habitlog.application.record.RecordApplicationService;
import com.recordmanagement.habitlog.application.record.dto.CalendarResponse;
import com.recordmanagement.habitlog.application.record.dto.CreateRecordCommand;
import com.recordmanagement.habitlog.application.record.dto.DailyRecordResponse;
import com.recordmanagement.habitlog.application.record.dto.RecordResponse;
import com.recordmanagement.habitlog.application.record.dto.RecordsByTypeResponse;
import com.recordmanagement.habitlog.application.record.dto.UpdateRecordCommand;
import com.recordmanagement.habitlog.domain.record.model.RecordId;
import com.recordmanagement.habitlog.common.response.ApiResponse;
import com.recordmanagement.habitlog.config.jwt.JwtTokenProvider;
import com.recordmanagement.habitlog.domain.user.model.RecordType;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/records")
@Tag(name = "Record", description = "기록 관련 API")
public class RecordController {
    
    private static final Logger log = LoggerFactory.getLogger(RecordController.class);
    
    private final RecordApplicationService recordApplicationService;
    
    public RecordController(RecordApplicationService recordApplicationService) {
        this.recordApplicationService = recordApplicationService;
    }
    
    @Operation(summary = "기록 작성", description = "새로운 기록을 작성합니다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "기록 작성 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "success": true,
                    "message": "기록이 성공적으로 작성되었습니다",
                    "data": {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "type": "DAILY",
                        "emotion": "😊",
                        "content": "오늘은 정말 좋은 하루였다",
                        "imageUrls": ["https://example.com/image1.jpg"],
                        "recordDate": "2025-01-07",
                        "createdAt": "2025-01-07T10:30:00",
                        "updatedAt": "2025-01-07T10:30:00"
                    }
                }
                """)
        )
    )
    @PostMapping
    public ResponseEntity<ApiResponse<RecordResponse>> createRecord(
            @Valid @RequestBody CreateRecordRequest request,
            Authentication authentication) {
        
        log.info("기록 작성 요청: type=[{}], content=[{}], recordDate=[{}]", 
                request.getType(), request.getContent(), request.getRecordDate());
        
        String userIdValue = authentication.getName();
        UserId userId = UserId.of(userIdValue);
        
        CreateRecordCommand command = new CreateRecordCommand(
            userId,
            request.getType(),
            request.getEmotion(),
            request.getContent(),
            request.getImageUrls(),
            request.getRecordDate()
        );
        
        RecordResponse response = recordApplicationService.createRecord(command);
        
        log.info("기록 작성 완료: recordId=[{}]", response.id());
        
        return ResponseEntity.ok(ApiResponse.success("기록이 성공적으로 작성되었습니다", response));
    }
    
    @Operation(summary = "기록 수정", description = "기존 기록을 수정합니다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "기록 수정 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "success": true,
                    "message": "기록이 성공적으로 수정되었습니다",
                    "data": {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "type": "EXERCISE",
                        "emotion": "😍",
                        "content": "수정된 내용입니다",
                        "imageUrls": ["https://example.com/image2.jpg"],
                        "recordDate": "2025-01-07",
                        "createdAt": "2025-01-07T10:30:00",
                        "updatedAt": "2025-01-07T11:15:00"
                    }
                }
                """)
        )
    )
    @PutMapping("/{recordId}")
    public ResponseEntity<ApiResponse<RecordResponse>> updateRecord(
            @PathVariable String recordId,
            @Valid @RequestBody UpdateRecordRequest request,
            Authentication authentication) {
        
        log.info("기록 수정 요청: recordId=[{}], content=[{}]", recordId, request.getContent());
        
        String userIdValue = authentication.getName();
        UserId userId = UserId.of(userIdValue);
        
        UpdateRecordCommand command = new UpdateRecordCommand(
            RecordId.from(recordId),
            userId,
            request.getType(),
            request.getEmotion(),
            request.getContent(),
            request.getImageUrls()
        );
        
        RecordResponse response = recordApplicationService.updateRecord(command);
        
        log.info("기록 수정 완료: recordId=[{}]", response.id());
        
        return ResponseEntity.ok(ApiResponse.success("기록이 성공적으로 수정되었습니다", response));
    }
    
    @Operation(summary = "기록 삭제", description = "기록을 삭제합니다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "기록 삭제 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "success": true,
                    "message": "기록이 성공적으로 삭제되었습니다"
                }
                """)
        )
    )
    @DeleteMapping("/{recordId}")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(
            @PathVariable String recordId,
            Authentication authentication) {
        
        log.info("기록 삭제 요청: recordId=[{}]", recordId);
        
        String userIdValue = authentication.getName();
        
        recordApplicationService.deleteRecord(recordId, userIdValue);
        
        log.info("기록 삭제 완료: recordId=[{}]", recordId);
        
        return ResponseEntity.ok(ApiResponse.success("기록이 성공적으로 삭제되었습니다", null));
    }
    
    @Operation(summary = "캘린더 조회", description = "월별 기록 현황을 조회합니다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "캘린더 조회 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "success": true,
                    "message": "캘린더가 성공적으로 조회되었습니다",
                    "data": {
                        "year": 2025,
                        "month": 1,
                        "dailyRecords": [
                            {
                                "date": "2025-01-07",
                                "records": [
                                    {
                                        "id": "550e8400-e29b-41d4-a716-446655440000",
                                        "type": "DAILY",
                                        "emotion": "😊",
                                        "hasImages": true
                                    },
                                    {
                                        "id": "550e8400-e29b-41d4-a716-446655440001",
                                        "type": "EXERCISE",
                                        "emotion": "💪",
                                        "hasImages": false
                                    }
                                ]
                            }
                        ]
                    }
                }
                """)
        )
    )
    @GetMapping("/calendar/{year}/{month}")
    public ResponseEntity<ApiResponse<CalendarResponse>> getCalendar(
            @PathVariable int year,
            @PathVariable int month,
            Authentication authentication) {
        
        log.info("캘린더 조회 요청: year=[{}], month=[{}]", year, month);
        
        String userIdValue = authentication.getName();
        
        CalendarResponse response = recordApplicationService.getCalendar(userIdValue, year, month);
        
        log.info("캘린더 조회 완료: year=[{}], month=[{}], records count=[{}]", 
                year, month, response.dailyRecords().size());
        
        return ResponseEntity.ok(ApiResponse.success("캘린더가 성공적으로 조회되었습니다", response));
    }
    
    @Operation(summary = "특정 날짜 기록 조회", description = "특정 날짜의 모든 기록을 상세 조회합니다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일일 기록 조회 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "success": true,
                    "message": "일일 기록이 성공적으로 조회되었습니다",
                    "data": {
                        "date": "2025-01-07",
                        "records": [
                            {
                                "id": "550e8400-e29b-41d4-a716-446655440000",
                                "type": "DAILY",
                                "emotion": "😊",
                                "content": "오늘은 정말 좋은 하루였습니다. 아침에 운동도 하고 친구들과 맛있는 음식도 먹었어요.",
                                "imageUrls": ["https://example.com/image1.jpg", "https://example.com/image2.jpg"],
                                "recordDate": "2025-01-07",
                                "createdAt": "2025-01-07T15:21:00",
                                "updatedAt": "2025-01-07T15:21:00"
                            }
                        ]
                    }
                }
                """)
        )
    )
    @GetMapping("/daily/{date}")
    public ResponseEntity<ApiResponse<DailyRecordResponse>> getDailyRecords(
            @PathVariable LocalDate date,
            Authentication authentication) {
        
        log.info("일일 기록 조회 요청: date=[{}]", date);
        
        String userIdValue = authentication.getName();
        
        DailyRecordResponse response = recordApplicationService.getDailyRecords(userIdValue, date);
        
        log.info("일일 기록 조회 완료: date=[{}], records count=[{}]", 
                date, response.records().size());
        
        return ResponseEntity.ok(ApiResponse.success("일일 기록이 성공적으로 조회되었습니다", response));
    }
    
    @Operation(summary = "타입별 기록 조회", description = "특정 타입의 모든 기록을 조회합니다")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "타입별 기록 조회 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "success": true,
                    "message": "타입별 기록이 성공적으로 조회되었습니다",
                    "data": {
                        "type": "EXERCISE",
                        "typeDescription": "운동 기록",
                        "totalCount": 15,
                        "records": [
                            {
                                "id": "550e8400-e29b-41d4-a716-446655440000",
                                "type": "EXERCISE",
                                "emotion": "💪",
                                "content": "오늘 헬스장에서 1시간 운동했습니다!",
                                "imageUrls": ["https://example.com/workout.jpg"],
                                "recordDate": "2025-01-07",
                                "createdAt": "2025-01-07T18:30:00",
                                "updatedAt": "2025-01-07T18:30:00"
                            }
                        ]
                    }
                }
                """)
        )
    )
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<RecordsByTypeResponse>> getRecordsByType(
            @PathVariable RecordType type,
            Authentication authentication) {
        
        log.info("타입별 기록 조회 요청: type=[{}]", type);
        
        String userIdValue = authentication.getName();
        
        RecordsByTypeResponse response = recordApplicationService.getRecordsByType(userIdValue, type);
        
        log.info("타입별 기록 조회 완료: type=[{}], records count=[{}]", 
                type, response.totalCount());
        
        return ResponseEntity.ok(ApiResponse.success("타입별 기록이 성공적으로 조회되었습니다", response));
    }
}