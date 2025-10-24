package com.recordmanagement.habitlog.domain.exercise.presentation.controller;

import com.recordmanagement.habitlog.api.exercise.dto.CreateExerciseRecordRequest;
import com.recordmanagement.habitlog.api.exercise.dto.UpdateExerciseRecordRequest;
import com.recordmanagement.habitlog.domain.exercise.application.service.ExerciseRecordApplicationService;
import com.recordmanagement.habitlog.domain.exercise.application.dto.CreateExerciseRecordCommand;
import com.recordmanagement.habitlog.domain.exercise.application.dto.ExerciseRecordResponse;
import com.recordmanagement.habitlog.domain.exercise.application.dto.UpdateExerciseRecordCommand;
import com.recordmanagement.habitlog.global.common.response.ApiResponse;
import com.recordmanagement.habitlog.domain.exercise.domain.model.ExerciseRecordId;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/exercise-records")
@Tag(name = "Exercise Record", description = "운동기록 관련 API")
public class ExerciseRecordController {
    
    private static final Logger log = LoggerFactory.getLogger(ExerciseRecordController.class);
    
    private final ExerciseRecordApplicationService exerciseRecordApplicationService;
    
    public ExerciseRecordController(ExerciseRecordApplicationService exerciseRecordApplicationService) {
        this.exerciseRecordApplicationService = exerciseRecordApplicationService;
    }
    
    @Operation(summary = "운동기록 작성", 
               description = """
                   새로운 운동기록을 작성합니다.
                   
                   **필수 항목:**
                   - exerciseType: 운동 종목 (필수)
                   - 운동기록 중 최소 1개: caloriesBurned(칼로리) OR exerciseTimeMinutes(운동시간) OR stepCount(걸음수) OR weight(몸무게)
                   - dailyNote: 나의 기록 (필수)
                   - recordDate: 기록 날짜 (필수)
                   - recordTime: 기록 시간 (필수)
                   
                   **선택 항목:**
                   - imageUrls: 사진 (최대 3장)
                   
                   **운동 종목:**
                   RUNNING(러닝), GOLF(골프), BASKETBALL(농구), SWIMMING(수영), BASEBALL(야구),
                   YOGA(요가), WEIGHT_TRAINING(웨이트 트레이닝), CYCLING(자전거), SOCCER(축구), TENNIS(테니스)
                   """,
            security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "운동기록 작성 요청",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "완전한 운동기록",
                    summary = "모든 필드가 포함된 예시",
                    value = """
                        {
                            "exerciseType": "BASKETBALL",
                            "caloriesBurned": 300,
                            "exerciseTimeMinutes": 60,
                            "stepCount": 5000,
                            "weight": 70.5,
                            "dailyNote": "오늘 농구 경기 정말 재밌었다! 팀워크가 좋았어요.",
                            "imageUrls": [
                                "https://seeday-images.s3.ap-northeast-2.amazonaws.com/records/images/uuid1.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=3600&...",
                                "https://seeday-images.s3.ap-northeast-2.amazonaws.com/records/images/uuid2.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=3600&..."
                            ],
                            "recordDate": "2025-01-07",
                            "recordTime": "13:30"
                        }
                        """
                ),
                @ExampleObject(
                    name = "최소 필수 항목만",
                    summary = "칼로리만 입력한 예시",
                    value = """
                        {
                            "exerciseType": "RUNNING",
                            "caloriesBurned": 250,
                            "dailyNote": "30분 달리기 완주했어요!",
                            "recordDate": "2025-01-07",
                            "recordTime": "07:00"
                        }
                        """
                ),
                @ExampleObject(
                    name = "운동시간만 입력",
                    summary = "운동시간만 입력한 예시",
                    value = """
                        {
                            "exerciseType": "YOGA",
                            "exerciseTimeMinutes": 45,
                            "dailyNote": "요가로 몸과 마음을 정화했어요.",
                            "recordDate": "2025-01-07",
                            "recordTime": "18:00"
                        }
                        """
                )
            }
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201",
        description = "운동기록 작성 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "code": "S201",
                    "statusCode": 201,
                    "message": "운동기록이 성공적으로 작성되었습니다",
                    "data": {
                        "id": "b6e1b665-e1f3-4e2b-b6c1-efb9b32c7be8",
                        "type": "EXERCISE",
                        "recordDate": "2025-01-07",
                        "recordTime": "13:30:00",
                        "createdAt": "2025-09-18T15:30:00",
                        "updatedAt": "2025-09-18T15:30:00",
                        "exerciseType": "BASKETBALL",
                        "caloriesBurned": 300,
                        "exerciseTimeMinutes": 60,
                        "stepCount": 5000,
                        "weight": 70.5,
                        "dailyNote": "오늘 농구 경기 정말 재밌었다! 팀워크가 좋았어요.",
                        "imageUrls": ["https://seeday-images.s3.ap-northeast-2.amazonaws.com/records/images/basketball-uuid.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=3600&..."]
                    }
                }
                """)
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "운동기록 등록 제한 초과",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "하루 1개 제한 초과",
                        summary = "운동기록 하루 1개 제한 초과",
                        value = """
                            {
                                "statusCode": 400,
                                "code": "E40408",
                                "message": "하루에 등록할 수 있는 운동 기록은 최대 1개입니다.",
                                "data": null
                            }
                            """
                    ),
                    @ExampleObject(
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
    @PostMapping
    public ResponseEntity<ApiResponse<ExerciseRecordResponse>> createExerciseRecord(
            @Valid @RequestBody CreateExerciseRecordRequest request,
            Authentication authentication) {
        
        log.info("운동기록 작성 요청: exerciseType=[{}], recordDate=[{}]", 
                request.getExerciseType(), request.getRecordDate());
        
        String userIdValue = getUserIdValue(authentication);
        UserId userId = UserId.of(userIdValue);
        
        LocalDate recordDate = LocalDate.parse(request.getRecordDate());
        LocalTime recordTime = LocalTime.parse(request.getRecordTime());
        
        CreateExerciseRecordCommand command = new CreateExerciseRecordCommand(
            userId,
            request.getExerciseType(),
            request.getCaloriesBurned(),
            request.getExerciseTimeMinutes(),
            request.getStepCount(),
            request.getWeight(),
            request.getDailyNote(),
            request.getImageUrls(),
            recordDate,
            recordTime
        );
        
        ExerciseRecordResponse response = exerciseRecordApplicationService.createExerciseRecord(command);
        
        log.info("운동기록 작성 완료: exerciseRecordId=[{}]", response.id());
        
        return ResponseEntity.status(201)
                .body(ApiResponse.created("운동기록이 성공적으로 작성되었습니다", response));
    }
    
    @Operation(summary = "운동기록 수정", 
               description = """
                   기존 운동기록을 수정합니다.
                   
                   **필수 항목:**
                   - exerciseType: 운동 종목 (필수)
                   - 운동기록 중 최소 1개: caloriesBurned(칼로리) OR exerciseTimeMinutes(운동시간) OR stepCount(걸음수) OR weight(몸무게)
                   - dailyNote: 나의 기록 (필수)
                   - recordTime: 기록 시간 (필수)
                   
                   **선택 항목:**
                   - imageUrls: 사진 (최대 3장)
                   """,
            security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "운동기록 수정 요청",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "완전한 수정 데이터",
                    summary = "모든 필드를 수정하는 예시",
                    value = """
                        {
                            "exerciseType": "RUNNING",
                            "caloriesBurned": 400,
                            "exerciseTimeMinutes": 45,
                            "stepCount": 6000,
                            "weight": 69.8,
                            "dailyNote": "수정된 운동 내용입니다. 오늘은 달리기를 했어요!",
                            "imageUrls": ["https://seeday-images.s3.ap-northeast-2.amazonaws.com/records/images/running-uuid.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Expires=3600&..."],
                            "recordTime": "07:30"
                        }
                        """
                ),
                @ExampleObject(
                    name = "부분 수정",
                    summary = "일부 필드만 수정하는 예시",
                    value = """
                        {
                            "exerciseType": "YOGA",
                            "exerciseTimeMinutes": 60,
                            "dailyNote": "요가 시간을 늘려서 더 깊은 명상을 했습니다.",
                            "recordTime": "18:30"
                        }
                        """
                )
            }
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "운동기록 수정 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "code": "S200",
                    "statusCode": 200,
                    "message": "운동기록이 성공적으로 수정되었습니다",
                    "data": {
                        "id": "b6e1b665-e1f3-4e2b-b6c1-efb9b32c7be8",
                        "type": "EXERCISE",
                        "recordDate": "2025-01-07",
                        "recordTime": "13:30:00",
                        "createdAt": "2025-09-18T15:30:00",
                        "updatedAt": "2025-09-18T16:15:00",
                        "exerciseType": "RUNNING",
                        "caloriesBurned": 400,
                        "exerciseTimeMinutes": 45,
                        "stepCount": 6000,
                        "weight": 69.8,
                        "dailyNote": "수정된 운동 내용입니다. 오늘은 달리기를 했어요!",
                        "imageUrls": ["https://example.com/running-track.jpg"]
                    }
                }
                """)
        )
    )
    @PutMapping("/{exerciseRecordId}")
    public ResponseEntity<ApiResponse<ExerciseRecordResponse>> updateExerciseRecord(
            @PathVariable String exerciseRecordId,
            @Valid @RequestBody UpdateExerciseRecordRequest request,
            Authentication authentication) {
        
        log.info("운동기록 수정 요청: exerciseRecordId=[{}], exerciseType=[{}]", 
                exerciseRecordId, request.getExerciseType());
        
        String userIdValue = getUserIdValue(authentication);
        UserId userId = UserId.of(userIdValue);
        
        LocalTime recordTime = LocalTime.parse(request.getRecordTime());
        
        UpdateExerciseRecordCommand command = new UpdateExerciseRecordCommand(
            ExerciseRecordId.from(exerciseRecordId),
            userId,
            request.getExerciseType(),
            request.getCaloriesBurned(),
            request.getExerciseTimeMinutes(),
            request.getStepCount(),
            request.getWeight(),
            request.getDailyNote(),
            request.getImageUrls(),
            recordTime
        );
        
        ExerciseRecordResponse response = exerciseRecordApplicationService.updateExerciseRecord(command);
        
        log.info("운동기록 수정 완료: exerciseRecordId=[{}]", response.id());
        
        return ResponseEntity.ok(ApiResponse.success("운동기록이 성공적으로 수정되었습니다", response));
    }
    
    @Operation(summary = "운동기록 삭제", 
               description = "운동기록을 삭제합니다. 삭제된 운동기록은 복구할 수 없습니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "운동기록 삭제 성공",
        content = @Content(

            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "code": "S200",
                    "statusCode": 200,
                    "message": "운동기록이 성공적으로 삭제되었습니다",
                    "data": null
                }
                """)
        )
    )
    @DeleteMapping("/{exerciseRecordId}")
    public ResponseEntity<ApiResponse<Void>> deleteExerciseRecord(
            @PathVariable String exerciseRecordId,
            Authentication authentication) {
        
        log.info("운동기록 삭제 요청: exerciseRecordId=[{}]", exerciseRecordId);
        
        String userIdValue = getUserIdValue(authentication);
        
        exerciseRecordApplicationService.deleteExerciseRecord(exerciseRecordId, userIdValue);
        
        log.info("운동기록 삭제 완료: exerciseRecordId=[{}]", exerciseRecordId);
        
        return ResponseEntity.ok(ApiResponse.success("운동기록이 성공적으로 삭제되었습니다", null));
    }
    
    
    private String getUserIdValue(Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            return authentication.getName();
        } else {
            return "test-user-001";
        }
    }
}