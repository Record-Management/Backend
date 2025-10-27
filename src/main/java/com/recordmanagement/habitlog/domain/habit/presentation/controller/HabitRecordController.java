package com.recordmanagement.habitlog.domain.habit.presentation.controller;

import com.recordmanagement.habitlog.api.habit.dto.CreateHabitRecordRequest;
import com.recordmanagement.habitlog.api.habit.dto.UpdateHabitRecordRequest;
import com.recordmanagement.habitlog.api.habit.dto.UpdateCompletionStatusRequest;
import com.recordmanagement.habitlog.domain.habit.application.service.HabitRecordApplicationService;
import com.recordmanagement.habitlog.domain.habit.application.dto.CreateHabitRecordCommand;
import com.recordmanagement.habitlog.domain.habit.application.dto.HabitRecordResponse;
import com.recordmanagement.habitlog.domain.habit.application.dto.UpdateHabitRecordCommand;
import com.recordmanagement.habitlog.global.common.response.ApiResponse;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/habit-records")
@Tag(name = "Habit Record", description = "습관기록 관련 API")
public class HabitRecordController {
    
    private static final Logger log = LoggerFactory.getLogger(HabitRecordController.class);
    
    private final HabitRecordApplicationService habitRecordApplicationService;
    
    public HabitRecordController(HabitRecordApplicationService habitRecordApplicationService) {
        this.habitRecordApplicationService = habitRecordApplicationService;
    }
    
    @PostMapping
    @Operation(summary = "습관기록 작성", 
               description = """
               새로운 습관기록을 작성합니다.
               
               **필수 항목:**
               - habitType: 습관 종류 (필수)
               - notificationEnabled: 알림 설정 여부 (필수)
               - recordDate: 기록 날짜 (필수, 오늘 날짜만 허용)
               
               **선택 항목:**
               - notificationTime: 알림 시간
               - memo: 메모/글쓰기
               - isMainRecord: 메인 기록 여부
               """,
               security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "습관기록 작성 성공",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "성공 응답",
                    summary = "습관기록 작성 성공",
                    value = """
                        {
                            "statusCode": 200,
                            "code": "S20000",
                            "message": "정상적으로 처리되었습니다.",
                            "data": {
                                "id": "habit_record_123",
                                "type": "HABIT",
                                "recordDate": "2025-10-27",
                                "recordTime": "14:30:00",
                                "createdAt": "2025-10-16T14:30:00",
                                "updatedAt": "2025-10-16T14:30:00",
                                "habitType": "EXERCISE",
                                "notificationEnabled": true,
                                "notificationTime": "09:00:00",
                                "memo": "오늘도 운동 완료!",
                                "isCompleted": false,
                                "isMainRecord": true
                            }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "습관기록 등록 제한 초과",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = {
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "하루 1개 제한 초과",
                        summary = "습관기록 하루 1개 제한 초과",
                        value = """
                            {
                                "statusCode": 400,
                                "code": "E40409",
                                "message": "하루에 등록할 수 있는 습관 기록은 최대 1개입니다.",
                                "data": null
                            }
                            """
                    ),
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "기록 종류 2가지 제한 초과",
                        summary = "하루 최대 2가지 기록 종류 제한 초과",
                        value = """
                            {
                                "statusCode": 400,
                                "code": "E40410",
                                "message": "하루에 등록할 수 있는 기록 종류는 최대 2가지입니다.",
                                "data": null
                            }
                            """
                    )
                }
            )
        )
    })
    public ResponseEntity<ApiResponse<HabitRecordResponse>> createHabitRecord(
            @Valid @RequestBody CreateHabitRecordRequest request,
            Authentication authentication) {
        
        log.info("습관기록 작성 요청: userId=[{}], habitType=[{}]", 
                authentication.getName(), request.habitType());
        
        CreateHabitRecordCommand command = new CreateHabitRecordCommand(
                UserId.of(authentication.getName()),
                request.habitType(),
                request.notificationEnabled(),
                request.notificationTime(),
                request.memo(),
                request.recordDate(),
                request.isMainRecord()
        );
        
        HabitRecordResponse response = habitRecordApplicationService.createHabitRecord(command);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    
    @PutMapping("/{habitRecordId}")
    @Operation(summary = "습관기록 수정", 
               description = "기존 습관기록을 수정합니다.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "습관기록 수정 성공",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                name = "성공 응답",
                summary = "습관기록 수정 성공",
                value = """
                    {
                        "statusCode": 200,
                        "code": "S20000",
                        "message": "정상적으로 처리되었습니다.",
                        "data": {
                            "id": "habit_record_123",
                            "type": "HABIT",
                            "recordDate": "2025-10-27",
                            "recordTime": "14:30:00",
                            "createdAt": "2025-10-16T14:30:00",
                            "updatedAt": "2025-10-16T16:45:00",
                            "habitType": "READING",
                            "notificationEnabled": false,
                            "notificationTime": "21:00:00",
                            "memo": "수정된 독서 기록입니다!",
                            "isCompleted": true,
                            "isMainRecord": true
                        }
                    }
                    """
            )
        )
    )
    public ResponseEntity<ApiResponse<HabitRecordResponse>> updateHabitRecord(
            @PathVariable String habitRecordId,
            @Valid @RequestBody UpdateHabitRecordRequest request,
            Authentication authentication) {
        
        log.info("습관기록 수정 요청: habitRecordId=[{}], userId=[{}]", 
                habitRecordId, authentication.getName());
        
        UpdateHabitRecordCommand command = new UpdateHabitRecordCommand(
                UserId.of(authentication.getName()),
                request.habitType(),
                request.notificationEnabled(),
                request.notificationTime(),
                request.memo(),
                request.isMainRecord()
        );
        
        HabitRecordResponse response = habitRecordApplicationService.updateHabitRecord(
                habitRecordId, 
                command
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @DeleteMapping("/{habitRecordId}")
    @Operation(summary = "습관기록 삭제", 
               description = "특정 습관기록을 삭제합니다.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "습관기록 삭제 성공",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                name = "성공 응답",
                summary = "습관기록 삭제 성공",
                value = """
                    {
                        "statusCode": 200,
                        "code": "S20000",
                        "message": "정상적으로 처리되었습니다.",
                        "data": null
                    }
                    """
            )
        )
    )
    public ResponseEntity<ApiResponse<Void>> deleteHabitRecord(
            @PathVariable String habitRecordId,
            Authentication authentication) {
        
        log.info("습관기록 삭제 요청: habitRecordId=[{}], userId=[{}]", 
                habitRecordId, authentication.getName());
        
        habitRecordApplicationService.deleteHabitRecord(
                habitRecordId, 
                UserId.of(authentication.getName())
        );
        
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    @PatchMapping("/{habitRecordId}/completion")
    @Operation(summary = "습관기록 완료 상태 변경", 
               description = "습관기록의 완료 상태를 토글합니다. 홈 화면에서 간단하게 완료/미완료 상태를 변경할 때 사용합니다.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "완료 상태 변경 성공",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                name = "성공 응답",
                summary = "완료 상태 변경 성공",
                value = """
                    {
                        "statusCode": 200,
                        "code": "S20000",
                        "message": "정상적으로 처리되었습니다.",
                        "data": {
                            "id": "habit_record_123",
                            "type": "HABIT",
                            "recordDate": "2025-10-27",
                            "recordTime": null,
                            "createdAt": "2025-10-16T14:30:00",
                            "updatedAt": "2025-10-16T16:45:00",
                            "habitType": "WATER",
                            "notificationEnabled": true,
                            "notificationTime": "09:00:00",
                            "memo": "물 마시기 완료!",
                            "isCompleted": true,
                            "isMainRecord": true
                        }
                    }
                    """
            )
        )
    )
    public ResponseEntity<ApiResponse<HabitRecordResponse>> updateCompletionStatus(
            @PathVariable String habitRecordId,
            @Valid @RequestBody UpdateCompletionStatusRequest request,
            Authentication authentication) {
        
        log.info("습관기록 완료 상태 변경 요청: habitRecordId=[{}], userId=[{}], isCompleted=[{}]", 
                habitRecordId, authentication.getName(), request.isCompleted());
        
        HabitRecordResponse response = habitRecordApplicationService.updateCompletionStatus(
                habitRecordId,
                UserId.of(authentication.getName()),
                request.isCompleted()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}