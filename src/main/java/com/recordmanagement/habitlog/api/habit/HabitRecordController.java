package com.recordmanagement.habitlog.api.habit;

import com.recordmanagement.habitlog.api.habit.dto.CreateHabitRecordRequest;
import com.recordmanagement.habitlog.api.habit.dto.UpdateHabitRecordRequest;
import com.recordmanagement.habitlog.application.habit.HabitRecordApplicationService;
import com.recordmanagement.habitlog.application.habit.dto.CreateHabitRecordCommand;
import com.recordmanagement.habitlog.application.habit.dto.HabitRecordResponse;
import com.recordmanagement.habitlog.application.habit.dto.UpdateHabitRecordCommand;
import com.recordmanagement.habitlog.common.response.ApiResponse;
import com.recordmanagement.habitlog.domain.user.model.UserId;
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
               description = "새로운 습관기록을 작성합니다.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
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
                request.recordDate()
        );
        
        HabitRecordResponse response = habitRecordApplicationService.createHabitRecord(command);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    
    @PutMapping("/{habitRecordId}")
    @Operation(summary = "습관기록 수정", 
               description = "기존 습관기록을 수정합니다.",
               security = @SecurityRequirement(name = "bearerAuth"))
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
                request.memo()
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
}