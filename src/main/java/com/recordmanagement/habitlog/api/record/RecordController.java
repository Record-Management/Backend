package com.recordmanagement.habitlog.api.record;

import com.recordmanagement.habitlog.api.record.dto.CreateRecordRequest;
import com.recordmanagement.habitlog.api.record.dto.UpdateRecordRequest;
import com.recordmanagement.habitlog.application.record.RecordApplicationService;
import com.recordmanagement.habitlog.application.record.dto.CreateRecordCommand;
import com.recordmanagement.habitlog.application.record.dto.RecordResponse;
import com.recordmanagement.habitlog.application.record.dto.UpdateRecordCommand;
import com.recordmanagement.habitlog.domain.record.model.RecordId;
import com.recordmanagement.habitlog.config.jwt.JwtTokenProvider;
import com.recordmanagement.habitlog.domain.user.model.RecordType;
import com.recordmanagement.habitlog.common.response.ApiResponse;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/daily-records")
@Tag(name = "Daily Record", description = "하루 기록 관련 API")
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
                    "content": "오늘은 정말 좋은 하루였다. 친구들과 맛있는 음식도 먹고 운동도 했어요.",
                    "emotion": "😊",
                    "recordDate": "2025-01-07",
                    "recordTime": "15:30",
                    "imageUrls": ["https://seeday-images.s3.ap-northeast-2.amazonaws.com/records/images/uuid1.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=3600&...", "https://seeday-images.s3.ap-northeast-2.amazonaws.com/records/images/uuid2.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=3600&..."]
                }
                """)
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201",
        description = "하루 기록 작성 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "code": "S201",
                    "statusCode": 201,
                    "message": "하루 기록이 성공적으로 작성되었습니다",
                    "data": {
                        "id": "b6e1b665-e1f3-4e2b-b6c1-efb9b32c7be8",
                        "type": "DAILY",
                        "emotion": "😊",
                        "content": "오늘은 정말 좋은 하루였다. 친구들과 맛있는 음식도 먹고 운동도 했어요.",
                        "imageUrls": ["https://seeday-images.s3.ap-northeast-2.amazonaws.com/records/images/uuid1.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=3600&...", "https://seeday-images.s3.ap-northeast-2.amazonaws.com/records/images/uuid2.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=3600&..."],
                        "recordDate": [2025, 1, 7],
                        "recordTime": [15, 30],
                        "createdAt": [2025, 9, 18, 15, 30, 0, 0],
                        "updatedAt": [2025, 9, 18, 15, 30, 0, 0]
                    }
                }
                """)
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "하루 기록 등록 제한 초과",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "statusCode": 400,
                    "code": "E40407",
                    "message": "하루에 등록할 수 있는 일상 기록은 최대 2개입니다.",
                    "data": null
                }
                """)
        )
    )
    @PostMapping
    public ResponseEntity<ApiResponse<RecordResponse>> createDailyRecord(
            HttpServletRequest httpRequest,
            Authentication authentication) {
        
        try {
            // HTTP Body 직접 읽기
            StringBuilder body = new StringBuilder();
            String line;
            try (BufferedReader reader = httpRequest.getReader()) {
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }
            
            log.info("HTTP Body: [{}]", body.toString());
            
            // ObjectMapper로 파싱
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            CreateRecordRequest request = mapper.readValue(body.toString(), CreateRecordRequest.class);
            
            log.info("파싱된 데이터: content=[{}], recordDate=[{}], recordTime=[{}], emotion=[{}]", 
                    request.getContent(), request.getRecordDate(), request.getRecordTime(), request.getEmotion());
            
            return createRecordByType(request, authentication, RecordType.DAILY);
            
        } catch (Exception e) {
            log.error("요청 처리 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    
    
    
    // 공통 생성 로직
    private ResponseEntity<ApiResponse<RecordResponse>> createRecordByType(
            CreateRecordRequest request, 
            Authentication authentication, 
            RecordType recordType) {
        
        log.info("{}작성 요청: content=[{}], recordDate=[{}], recordTime=[{}]", 
                recordType.getDescription(), request.getContent(), request.getRecordDate(), request.getRecordTime());
        
        // 인증이 없는 경우 테스트용 사용자 ID 사용
        String userIdValue;
        if (authentication != null && authentication.getName() != null) {
            userIdValue = authentication.getName();
        } else {
            userIdValue = "test-user-001";
        }
        UserId userId = UserId.of(userIdValue);
        
        
        // String을 LocalDate/LocalTime으로 변환
        LocalDate recordDate = LocalDate.parse(request.getRecordDate());
        LocalTime recordTime = LocalTime.parse(request.getRecordTime());
        
        CreateRecordCommand command = new CreateRecordCommand(
            userId,
            recordType, // 파라미터에서 받은 타입 사용
            request.getEmotion(),
            request.getContent(),
            request.getImageUrls(),
            recordDate,
            recordTime
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
                    "content": "수정된 하루 내용입니다. 저녁에 영화도 봤어요!",
                    "emotion": "😍",
                    "imageUrls": ["https://seeday-images.s3.ap-northeast-2.amazonaws.com/records/images/updated-uuid.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=3600&..."]
                }
                """)
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "하루 기록 수정 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "code": "S200",
                    "statusCode": 200,
                    "message": "하루 기록이 성공적으로 수정되었습니다",
                    "data": {
                        "id": "b6e1b665-e1f3-4e2b-b6c1-efb9b32c7be8",
                        "type": "DAILY",
                        "emotion": "😍",
                        "content": "수정된 하루 내용입니다. 저녁에 영화도 봤어요!",
                        "imageUrls": ["https://seeday-images.s3.ap-northeast-2.amazonaws.com/records/images/updated-uuid.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=3600&..."],
                        "recordDate": [2025, 1, 7],
                        "recordTime": [15, 30],
                        "createdAt": [2025, 9, 18, 15, 30, 0, 0],
                        "updatedAt": [2025, 9, 18, 15, 30, 0, 0]
                    }
                }
                """)
        )
    )
    @PutMapping("/{recordId}")
    public ResponseEntity<ApiResponse<RecordResponse>> updateDailyRecord(
            @PathVariable String recordId,
            @Valid @RequestBody UpdateRecordRequest request,
            Authentication authentication) {
        return updateRecordByType(recordId, request, authentication, RecordType.DAILY);
    }
    
    
    
    
    // 공통 수정 로직
    private ResponseEntity<ApiResponse<RecordResponse>> updateRecordByType(
            String recordId,
            UpdateRecordRequest request,
            Authentication authentication, 
            RecordType recordType) {
        
        log.info("{}수정 요청: recordId=[{}], content=[{}]", 
                recordType.getDescription(), recordId, request.getContent());
        
        // 인증이 없는 경우 테스트용 사용자 ID 사용
        String userIdValue;
        if (authentication != null && authentication.getName() != null) {
            userIdValue = authentication.getName();
        } else {
            userIdValue = "test-user-001";
        }
        UserId userId = UserId.of(userIdValue);
        
        UpdateRecordCommand command = new UpdateRecordCommand(
            RecordId.from(recordId),
            userId,
            recordType, // 파라미터에서 받은 타입 사용
            request.getEmotion(),
            request.getContent(),
            request.getImageUrls()
        );
        
        RecordResponse response = recordApplicationService.updateRecord(command);
        
        log.info("{}수정 완료: recordId=[{}]", recordType.getDescription(), response.id());
        
        return ResponseEntity.ok(ApiResponse.success(recordType.getDescription() + "이 성공적으로 수정되었습니다", response));
    }
    
    // ==================== 통합 API (삭제/조회) ====================
    
    @Operation(summary = "하루 기록 삭제", description = "하루 기록을 삭제합니다",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "하루 기록 삭제 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "statusCode": 200,
                    "code": "S200",
                    "message": "하루 기록이 성공적으로 삭제되었습니다",
                    "data": null
                }
                """)
        )
    )
    @DeleteMapping("/{recordId}")
    public ResponseEntity<ApiResponse<Void>> deleteRecord(
            @PathVariable String recordId,
            Authentication authentication) {
        
        log.info("하루 기록 삭제 요청: recordId=[{}]", recordId);
        
        String userIdValue = authentication.getName();
        
        recordApplicationService.deleteRecord(recordId, userIdValue);
        
        log.info("하루 기록 삭제 완료: recordId=[{}]", recordId);
        
        return ResponseEntity.ok(ApiResponse.success("하루 기록이 성공적으로 삭제되었습니다", null));
    }
    
}