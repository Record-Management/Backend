package com.recordmanagement.habitlog.api.record;

import com.recordmanagement.habitlog.api.record.dto.CreateRecordRequest;
import com.recordmanagement.habitlog.api.record.dto.UpdateRecordRequest;
import com.recordmanagement.habitlog.application.record.RecordApplicationService;
import com.recordmanagement.habitlog.application.record.dto.CalendarResponse;
import com.recordmanagement.habitlog.application.record.dto.CreateRecordCommand;
import com.recordmanagement.habitlog.application.record.dto.DailyRecordResponse;
import com.recordmanagement.habitlog.application.record.dto.RecordResponse;
import com.recordmanagement.habitlog.application.record.dto.UpdateRecordCommand;
import com.recordmanagement.habitlog.domain.record.model.RecordId;
import com.recordmanagement.habitlog.config.jwt.JwtTokenProvider;
import com.recordmanagement.habitlog.domain.user.model.RecordType;
import com.recordmanagement.habitlog.common.response.ApiResponse;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/records")
@Tag(name = "Record", description = "기록 관련 API")
public class RecordController {
    
    private static final Logger log = LoggerFactory.getLogger(RecordController.class);
    
    private final RecordApplicationService recordApplicationService;
    
    public RecordController(RecordApplicationService recordApplicationService) {
        this.recordApplicationService = recordApplicationService;
    }
    
    // ==================== 타입별 생성 API ====================
    
    @Operation(summary = "하루 기록 작성", description = "새로운 하루 기록을 작성합니다",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "하루 기록 작성 요청",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "emotion": "😊",
                    "content": "오늘은 정말 좋은 하루였다",
                    "imageUrls": ["https://example.com/image1.jpg"],
                    "recordDate": "2025-01-07",
                    "recordTime": "10:30"
                }
                """)
        )
    )
    @PostMapping("/daily")
    public ResponseEntity<ApiResponse<RecordResponse>> createDailyRecord(
            @Valid @RequestBody CreateRecordRequest request,
            Authentication authentication) {
        return createRecordByType(request, authentication, RecordType.DAILY);
    }
    
    @Operation(summary = "운동 기록 작성", description = "새로운 운동 기록을 작성합니다",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping("/exercise")
    public ResponseEntity<ApiResponse<RecordResponse>> createExerciseRecord(
            @Valid @RequestBody CreateRecordRequest request,
            Authentication authentication) {
        return createRecordByType(request, authentication, RecordType.EXERCISE);
    }
    
    @Operation(summary = "습관 기록 작성", description = "새로운 습관 기록을 작성합니다",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping("/habit")
    public ResponseEntity<ApiResponse<RecordResponse>> createHabitRecord(
            @Valid @RequestBody CreateRecordRequest request,
            Authentication authentication) {
        return createRecordByType(request, authentication, RecordType.HABIT);
    }
    
    @Operation(summary = "일정 기록 작성", description = "새로운 일정 기록을 작성합니다",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<RecordResponse>> createScheduleRecord(
            @Valid @RequestBody CreateRecordRequest request,
            Authentication authentication) {
        return createRecordByType(request, authentication, RecordType.SCHEDULE);
    }
    
    // 공통 생성 로직
    private ResponseEntity<ApiResponse<RecordResponse>> createRecordByType(
            CreateRecordRequest request, 
            Authentication authentication, 
            RecordType recordType) {
        
        log.info("{}작성 요청: content=[{}], recordDate=[{}], recordTime=[{}]", 
                recordType.getDescription(), request.getContent(), request.getRecordDate(), request.getRecordTime());
        
        String userIdValue = authentication.getName();
        UserId userId = UserId.of(userIdValue);
        
        CreateRecordCommand command = new CreateRecordCommand(
            userId,
            recordType, // 파라미터에서 받은 타입 사용
            request.getEmotion(),
            request.getContent(),
            request.getImageUrls(),
            request.getRecordDate(),
            request.getRecordTime()
        );
        
        RecordResponse response = recordApplicationService.createRecord(command);
        
        log.info("{}작성 완료: recordId=[{}]", recordType.getDescription(), response.id());
        
        return ResponseEntity.status(201).body(ApiResponse.created(recordType.getDescription() + "이 성공적으로 작성되었습니다", response));
    }
    
    // ==================== 타입별 수정 API ====================
    
    @Operation(summary = "하루 기록 수정", description = "기존 하루 기록을 수정합니다",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "하루 기록 수정 요청",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "emotion": "😍",
                    "content": "수정된 하루 내용입니다",
                    "imageUrls": ["https://example.com/image2.jpg"],
                    "recordTime": "11:15"
                }
                """)
        )
    )
    @PutMapping("/daily/{recordId}")
    public ResponseEntity<ApiResponse<RecordResponse>> updateDailyRecord(
            @PathVariable String recordId,
            @Valid @RequestBody UpdateRecordRequest request,
            Authentication authentication) {
        return updateRecordByType(recordId, request, authentication, RecordType.DAILY);
    }
    
    @Operation(summary = "운동 기록 수정", description = "기존 운동 기록을 수정합니다",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping("/exercise/{recordId}")
    public ResponseEntity<ApiResponse<RecordResponse>> updateExerciseRecord(
            @PathVariable String recordId,
            @Valid @RequestBody UpdateRecordRequest request,
            Authentication authentication) {
        return updateRecordByType(recordId, request, authentication, RecordType.EXERCISE);
    }
    
    @Operation(summary = "습관 기록 수정", description = "기존 습관 기록을 수정합니다",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping("/habit/{recordId}")
    public ResponseEntity<ApiResponse<RecordResponse>> updateHabitRecord(
            @PathVariable String recordId,
            @Valid @RequestBody UpdateRecordRequest request,
            Authentication authentication) {
        return updateRecordByType(recordId, request, authentication, RecordType.HABIT);
    }
    
    @Operation(summary = "일정 기록 수정", description = "기존 일정 기록을 수정합니다",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping("/schedule/{recordId}")
    public ResponseEntity<ApiResponse<RecordResponse>> updateScheduleRecord(
            @PathVariable String recordId,
            @Valid @RequestBody UpdateRecordRequest request,
            Authentication authentication) {
        return updateRecordByType(recordId, request, authentication, RecordType.SCHEDULE);
    }
    
    // 공통 수정 로직
    private ResponseEntity<ApiResponse<RecordResponse>> updateRecordByType(
            String recordId,
            UpdateRecordRequest request,
            Authentication authentication, 
            RecordType recordType) {
        
        log.info("{}수정 요청: recordId=[{}], content=[{}], recordTime=[{}]", 
                recordType.getDescription(), recordId, request.getContent(), request.getRecordTime());
        
        String userIdValue = authentication.getName();
        UserId userId = UserId.of(userIdValue);
        
        UpdateRecordCommand command = new UpdateRecordCommand(
            RecordId.from(recordId),
            userId,
            recordType, // 파라미터에서 받은 타입 사용
            request.getEmotion(),
            request.getContent(),
            request.getImageUrls(),
            request.getRecordTime()
        );
        
        RecordResponse response = recordApplicationService.updateRecord(command);
        
        log.info("{}수정 완료: recordId=[{}]", recordType.getDescription(), response.id());
        
        return ResponseEntity.ok(ApiResponse.success(recordType.getDescription() + "이 성공적으로 수정되었습니다", response));
    }
    
    // ==================== 통합 API (삭제/조회) ====================
    
    @Operation(summary = "기록 삭제", description = "기록을 삭제합니다 (모든 타입 공통)",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "기록 삭제 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "statusCode": 200,
                    "code": "S200",
                    "message": "기록이 성공적으로 삭제되었습니다",
                    "data": null
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
    
    @Operation(summary = "캘린더 조회", description = "월별 기록 현황을 조회합니다. 타입별 필터링이 가능합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
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
                        "dailyRecords": [
                            {
                                "date": "2025-01-07",
                                "records": [
                                    {
                                        "id": "550e8400-e29b-41d4-a716-446655440000",
                                        "type": "DAILY",
                                        "emotion": "😊"
                                    },
                                    {
                                        "id": "550e8400-e29b-41d4-a716-446655440001",
                                        "type": "EXERCISE",
                                        "emotion": "💪"
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
            @Parameter(description = "필터링할 기록 타입 목록. 미지정시 모든 타입 조회", example = "DAILY,EXERCISE") 
            @RequestParam(required = false) List<RecordType> types,
            Authentication authentication) {
        
        log.info("캘린더 조회 요청: year=[{}], month=[{}], types=[{}]", year, month, types);
        
        String userIdValue = authentication.getName();
        
        CalendarResponse response = recordApplicationService.getCalendar(userIdValue, year, month, types);
        
        log.info("캘린더 조회 완료: year=[{}], month=[{}], types=[{}], records count=[{}]", 
                year, month, types, response.dailyRecords().size());
        
        return ResponseEntity.ok(ApiResponse.success("캘린더가 성공적으로 조회되었습니다", response));
    }
    
    @Operation(summary = "특정 날짜 기록 조회", description = "특정 날짜의 모든 기록을 상세 조회합니다",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일일 기록 조회 성공",
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
                                "emotion": "😊",
                                "content": "오늘은 정말 좋은 하루였습니다. 아침에 운동도 하고 친구들과 맛있는 음식도 먹었어요.",
                                "imageUrls": ["https://example.com/image1.jpg", "https://example.com/image2.jpg"],
                                "recordDate": "2025-01-07",
                                "recordTime": "15:21",
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
    
}