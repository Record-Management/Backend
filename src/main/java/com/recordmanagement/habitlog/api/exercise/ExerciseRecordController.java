package com.recordmanagement.habitlog.api.exercise;

import com.recordmanagement.habitlog.api.exercise.dto.CreateExerciseRecordRequest;
import com.recordmanagement.habitlog.api.exercise.dto.UpdateExerciseRecordRequest;
import com.recordmanagement.habitlog.application.exercise.ExerciseRecordApplicationService;
import com.recordmanagement.habitlog.application.exercise.dto.CreateExerciseRecordCommand;
import com.recordmanagement.habitlog.application.exercise.dto.DailyExerciseRecordResponse;
import com.recordmanagement.habitlog.application.exercise.dto.ExerciseRecordResponse;
import com.recordmanagement.habitlog.application.exercise.dto.UpdateExerciseRecordCommand;
import com.recordmanagement.habitlog.common.response.ApiResponse;
import com.recordmanagement.habitlog.domain.exercise.model.ExerciseRecordId;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
                   - 운동기록 중 최소 1개: caloriesBurned(칼로리) OR exerciseTimeMinutes(운동시간) OR stepCount(걸음수)
                   - dailyNote: 나의 기록 (필수)
                   - recordDate: 기록 날짜 (필수)
                   
                   **선택 항목:**
                   - weight: 몸무게
                   - imageUrls: 사진 (최대 3장)
                   
                   **운동 종목:**
                   RUNNING(러닝), GOLF(골프), BASKETBALL(농구), SWIMMING(수영), BASEBALL(야구),
                   YOGA(요가), WEIGHT_TRAINING(웨이트 트레이닝), CYCLING(자전거), SOCCER(축구), TENNIS(테니스)
                   """,
            security = @SecurityRequirement(name = "Bearer Authentication"))
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
                                "https://example.com/basketball-game1.jpg",
                                "https://example.com/basketball-game2.jpg"
                            ],
                            "recordDate": "2025-01-07"
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
                            "recordDate": "2025-01-07"
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
                            "recordDate": "2025-01-07"
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
                        "exerciseType": "BASKETBALL",
                        "caloriesBurned": 300,
                        "exerciseTimeMinutes": 60,
                        "stepCount": 5000,
                        "weight": 70.5,
                        "dailyNote": "오늘 농구 경기 정말 재밌었다! 팀워크가 좋았어요.",
                        "imageUrls": ["https://example.com/basketball-game.jpg"],
                        "recordDate": [2025, 1, 7],
                        "createdAt": [2025, 9, 18, 15, 30, 0, 0],
                        "updatedAt": [2025, 9, 18, 15, 30, 0, 0]
                    }
                }
                """)
        )
    )
    @PostMapping
    public ResponseEntity<ApiResponse<ExerciseRecordResponse>> createExerciseRecord(
            @Valid @RequestBody CreateExerciseRecordRequest request,
            Authentication authentication) {
        
        log.info("운동기록 작성 요청: exerciseType=[{}], recordDate=[{}]", 
                request.getExerciseType(), request.getRecordDate());
        
        String userIdValue = getUserIdValue(authentication);
        UserId userId = UserId.of(userIdValue);
        
        LocalDate recordDate = LocalDate.parse(request.getRecordDate());
        
        CreateExerciseRecordCommand command = new CreateExerciseRecordCommand(
            userId,
            request.getExerciseType(),
            request.getCaloriesBurned(),
            request.getExerciseTimeMinutes(),
            request.getStepCount(),
            request.getWeight(),
            request.getDailyNote(),
            request.getImageUrls(),
            recordDate
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
                   - 운동기록 중 최소 1개: caloriesBurned(칼로리) OR exerciseTimeMinutes(운동시간) OR stepCount(걸음수)
                   - dailyNote: 나의 기록 (필수)
                   
                   **선택 항목:**
                   - weight: 몸무게
                   - imageUrls: 사진 (최대 3장)
                   """,
            security = @SecurityRequirement(name = "Bearer Authentication"))
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
                            "imageUrls": ["https://example.com/running-track.jpg"]
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
                            "dailyNote": "요가 시간을 늘려서 더 깊은 명상을 했습니다."
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
                        "exerciseType": "RUNNING",
                        "caloriesBurned": 400,
                        "exerciseTimeMinutes": 45,
                        "stepCount": 6000,
                        "weight": 69.8,
                        "dailyNote": "수정된 운동 내용입니다. 오늘은 달리기를 했어요!",
                        "imageUrls": ["https://example.com/running-track.jpg"],
                        "recordDate": [2025, 1, 7],
                        "createdAt": [2025, 9, 18, 15, 30, 0, 0],
                        "updatedAt": [2025, 9, 18, 16, 15, 0, 0]
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
        
        UpdateExerciseRecordCommand command = new UpdateExerciseRecordCommand(
            ExerciseRecordId.from(exerciseRecordId),
            userId,
            request.getExerciseType(),
            request.getCaloriesBurned(),
            request.getExerciseTimeMinutes(),
            request.getStepCount(),
            request.getWeight(),
            request.getDailyNote(),
            request.getImageUrls()
        );
        
        ExerciseRecordResponse response = exerciseRecordApplicationService.updateExerciseRecord(command);
        
        log.info("운동기록 수정 완료: exerciseRecordId=[{}]", response.id());
        
        return ResponseEntity.ok(ApiResponse.success("운동기록이 성공적으로 수정되었습니다", response));
    }
    
    @Operation(summary = "운동기록 삭제", 
               description = "운동기록을 삭제합니다. 삭제된 운동기록은 복구할 수 없습니다.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
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
    
    @Operation(summary = "특정 날짜 운동기록 조회", 
               description = """
                   특정 날짜의 모든 운동기록을 조회합니다.
                   
                   **날짜 형식:** YYYY-MM-DD (예: 2025-01-07)
                   """,
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "일일 운동기록 조회 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "code": "S200",
                    "statusCode": 200,
                    "message": "일일 운동기록이 성공적으로 조회되었습니다",
                    "data": {
                        "date": [2025, 1, 7],
                        "exerciseRecords": [
                            {
                                "id": "b6e1b665-e1f3-4e2b-b6c1-efb9b32c7be8",
                                "exerciseType": "BASKETBALL",
                                "caloriesBurned": 300,
                                "exerciseTimeMinutes": 60,
                                "stepCount": 5000,
                                "weight": 70.5,
                                "dailyNote": "오늘 농구 경기 정말 재밌었다!",
                                "imageUrls": ["https://example.com/basketball.jpg"],
                                "recordDate": [2025, 1, 7],
                                "createdAt": [2025, 9, 18, 15, 30, 0, 0],
                                "updatedAt": [2025, 9, 18, 15, 30, 0, 0]
                            },
                            {
                                "id": "c7f2c776-f2g4-5f3c-c7d2-fgc0c43d8cf9",
                                "exerciseType": "RUNNING",
                                "caloriesBurned": 250,
                                "exerciseTimeMinutes": null,
                                "stepCount": null,
                                "weight": null,
                                "dailyNote": "아침 조깅으로 하루를 시작!",
                                "imageUrls": [],
                                "recordDate": [2025, 1, 7],
                                "createdAt": [2025, 9, 18, 8, 15, 0, 0],
                                "updatedAt": [2025, 9, 18, 8, 15, 0, 0]
                            }
                        ]
                    }
                }
                """)
        )
    )
    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<DailyExerciseRecordResponse>> getExerciseRecordsByDate(
            @PathVariable LocalDate date,
            Authentication authentication) {
        
        log.info("일일 운동기록 조회 요청: date=[{}]", date);
        
        String userIdValue = getUserIdValue(authentication);
        
        DailyExerciseRecordResponse response = exerciseRecordApplicationService.getDailyExerciseRecords(userIdValue, date);
        
        log.info("일일 운동기록 조회 완료: date=[{}], count=[{}]", 
                date, response.exerciseRecords().size());
        
        return ResponseEntity.ok(ApiResponse.success("일일 운동기록이 성공적으로 조회되었습니다", response));
    }
    
    @Operation(summary = "기간별 운동기록 조회", 
               description = """
                   지정된 기간의 운동기록을 조회합니다.
                   
                   **파라미터:**
                   - startDate: 조회 시작 날짜 (YYYY-MM-DD)
                   - endDate: 조회 종료 날짜 (YYYY-MM-DD)
                   
                   **주의사항:**
                   - 시작 날짜는 종료 날짜보다 이전이어야 합니다
                   - 최대 조회 가능 기간: 1년
                   """,
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "기간별 운동기록 조회 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "code": "S200",
                    "statusCode": 200,
                    "message": "기간별 운동기록이 성공적으로 조회되었습니다",
                    "data": [
                        {
                            "id": "b6e1b665-e1f3-4e2b-b6c1-efb9b32c7be8",
                            "exerciseType": "BASKETBALL",
                            "caloriesBurned": 300,
                            "exerciseTimeMinutes": 60,
                            "stepCount": 5000,
                            "weight": 70.5,
                            "dailyNote": "오늘 농구 경기 정말 재밌었다!",
                            "imageUrls": ["https://example.com/basketball.jpg"],
                            "recordDate": [2025, 1, 7],
                            "createdAt": [2025, 9, 18, 15, 30, 0, 0],
                            "updatedAt": [2025, 9, 18, 15, 30, 0, 0]
                        },
                        {
                            "id": "c7f2c776-f2g4-5f3c-c7d2-fgc0c43d8cf9",
                            "exerciseType": "RUNNING",
                            "caloriesBurned": 250,
                            "exerciseTimeMinutes": null,
                            "stepCount": null,
                            "weight": null,
                            "dailyNote": "아침 조깅으로 하루를 시작!",
                            "imageUrls": [],
                            "recordDate": [2025, 1, 6],
                            "createdAt": [2025, 9, 17, 8, 15, 0, 0],
                            "updatedAt": [2025, 9, 17, 8, 15, 0, 0]
                        }
                    ]
                }
                """)
        )
    )
    @GetMapping("/period")
    public ResponseEntity<ApiResponse<List<ExerciseRecordResponse>>> getExerciseRecordsBetween(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            Authentication authentication) {
        
        log.info("기간별 운동기록 조회 요청: startDate=[{}], endDate=[{}]", startDate, endDate);
        
        String userIdValue = getUserIdValue(authentication);
        
        List<ExerciseRecordResponse> response = exerciseRecordApplicationService.getExerciseRecordsBetween(
                userIdValue, startDate, endDate);
        
        log.info("기간별 운동기록 조회 완료: startDate=[{}], endDate=[{}], count=[{}]", 
                startDate, endDate, response.size());
        
        return ResponseEntity.ok(ApiResponse.success("기간별 운동기록이 성공적으로 조회되었습니다", response));
    }
    
    private String getUserIdValue(Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            return authentication.getName();
        } else {
            return "test-user-001";
        }
    }
}