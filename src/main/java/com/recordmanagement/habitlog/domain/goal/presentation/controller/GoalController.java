package com.recordmanagement.habitlog.domain.goal.presentation.controller;

import com.recordmanagement.habitlog.domain.goal.application.dto.*;
import com.recordmanagement.habitlog.domain.goal.application.service.GoalApplicationService;
import com.recordmanagement.habitlog.domain.goal.domain.model.Goal;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 목표 관리 컨트롤러
 *
 * @author 전우선
 * @since 2025.11.04
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Goal", description = "목표 관리 API")
public class GoalController {

    private final GoalApplicationService goalApplicationService;

    /**
     * 현재 목표 조회
     *
     * @param authentication 인증 정보 (JWT 토큰에서 userId 추출)
     * @return 현재 목표 정보
     */
    @GetMapping("/current")
    @Operation(
            summary = "현재 목표 조회",
            description = "사용자의 현재 진행중인 목표를 조회합니다. 목표가 없으면 빈 응답을 반환합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "현재 목표 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.recordmanagement.habitlog.global.common.response.ApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "진행중인 습관 목표 (20일)",
                                            value = """
                                                    {
                                                      "statusCode": 200,
                                                      "code": "S200",
                                                      "message": "현재 목표 조회가 성공적으로 완료되었습니다",
                                                      "data": {
                                                        "goalId": "550e8400-e29b-41d4-a716-446655440000",
                                                        "recordType": "HABIT",
                                                        "goalDays": 20,
                                                        "startDate": "2025-11-01",
                                                        "endDate": "2025-11-20",
                                                        "completedDays": 7,
                                                        "achievementRate": 35.0,
                                                        "treeStage": "STAGE_2",
                                                        "canCreateNew": false
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "진행중인 운동 목표 (30일)",
                                            value = """
                                                    {
                                                      "statusCode": 200,
                                                      "code": "S200",
                                                      "message": "현재 목표 조회가 성공적으로 완료되었습니다",
                                                      "data": {
                                                        "goalId": "550e8400-e29b-41d4-a716-446655440001",
                                                        "recordType": "EXERCISE",
                                                        "goalDays": 30,
                                                        "startDate": "2025-11-01",
                                                        "endDate": "2025-11-30",
                                                        "completedDays": 15,
                                                        "achievementRate": 50.0,
                                                        "treeStage": "STAGE_2",
                                                        "canCreateNew": false
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "진행중인 일상 목표 (10일)",
                                            value = """
                                                    {
                                                      "statusCode": 200,
                                                      "code": "S200",
                                                      "message": "현재 목표 조회가 성공적으로 완료되었습니다",
                                                      "data": {
                                                        "goalId": "550e8400-e29b-41d4-a716-446655440002",
                                                        "recordType": "DAILY",
                                                        "goalDays": 10,
                                                        "startDate": "2025-11-01",
                                                        "endDate": "2025-11-10",
                                                        "completedDays": 9,
                                                        "achievementRate": 90.0,
                                                        "treeStage": "STAGE_4",
                                                        "canCreateNew": false
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "진행중인 목표가 없는 경우",
                                            value = """
                                                    {
                                                      "statusCode": 200,
                                                      "code": "S200",
                                                      "message": "현재 목표 조회가 성공적으로 완료되었습니다",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (토큰 없음/만료/잘못됨)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "error": "토큰이 만료되었거나 유효하지 않습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<com.recordmanagement.habitlog.global.common.response.ApiResponse<CurrentGoalResponse>> getCurrentGoal(
            Authentication authentication) {

        String userId = authentication.getName();
        log.info("Getting current goal for user: {}", userId);

        Optional<Goal> currentGoal = goalApplicationService.getCurrentGoal(UserId.from(userId));
        boolean canCreateNew = goalApplicationService.canCreateNewGoal(UserId.from(userId));

        CurrentGoalResponse response = currentGoal
                .map(goal -> CurrentGoalResponse.from(goal, canCreateNew))
                .orElse(null);

        return ResponseEntity.ok(com.recordmanagement.habitlog.global.common.response.ApiResponse.success("현재 목표 조회가 성공적으로 완료되었습니다", response));
    }

    /**
     * 목표 달성 보고서 조회
     *
     * @param authentication 인증 정보 (JWT 토큰에서 userId 추출)
     * @return 목표 달성 보고서
     */
    @GetMapping("/achievement/report")
    @Operation(
            summary = "목표 달성 보고서 조회",
            description = "현재 목표 진행상황과 누적 달성 횟수, 최근 이력을 포함한 보고서를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "달성 보고서 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.recordmanagement.habitlog.global.common.response.ApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "현재 진행중인 목표가 있는 경우",
                                            value = """
                                                    {
                                                      "statusCode": 200,
                                                      "code": "S200",
                                                      "message": "목표 달성 보고서 조회가 성공적으로 완료되었습니다",
                                                      "data": {
                                                        "currentPeriod": {
                                                          "goalId": "550e8400-e29b-41d4-a716-446655440000",
                                                          "recordType": "HABIT",
                                                          "goalDays": 20,
                                                          "startDate": "2025-11-01",
                                                          "endDate": "2025-11-20",
                                                          "completedDays": 7,
                                                          "achievementRate": 35.0,
                                                          "treeStage": "STAGE_2",
                                                          "isInProgress": true
                                                        },
                                                        "cumulativeAchievementCount": 3,
                                                        "recentHistory": [
                                                        {
                                                          "goalId": "550e8400-e29b-41d4-a716-446655440003",
                                                          "recordType": "EXERCISE",
                                                          "goalDays": 10,
                                                          "startDate": "2025-10-20",
                                                          "endDate": "2025-10-29",
                                                          "completedDays": 10,
                                                          "achievementRate": 100.0,
                                                          "finalTreeStage": "STAGE_4",
                                                          "status": "완료"
                                                        },
                                                        {
                                                          "goalId": "550e8400-e29b-41d4-a716-446655440004",
                                                          "recordType": "DAILY",
                                                          "goalDays": 30,
                                                          "startDate": "2025-09-15",
                                                          "endDate": "2025-10-14",
                                                          "completedDays": 28,
                                                          "achievementRate": 93.3,
                                                          "finalTreeStage": "STAGE_4",
                                                          "status": "완료"
                                                        }
                                                        ]
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "진행중인 목표가 없는 경우",
                                            value = """
                                                    {
                                                      "currentPeriod": null,
                                                      "cumulativeAchievementCount": 5,
                                                      "recentHistory": [
                                                        {
                                                          "goalId": "550e8400-e29b-41d4-a716-446655440005",
                                                          "recordType": "HABIT",
                                                          "goalDays": 20,
                                                          "startDate": "2025-10-10",
                                                          "endDate": "2025-10-29",
                                                          "completedDays": 20,
                                                          "achievementRate": 100.0,
                                                          "finalTreeStage": "STAGE_4",
                                                          "status": "완료"
                                                        }
                                                      ]
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (토큰 없음/만료/잘못됨)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "error": "토큰이 만료되었거나 유효하지 않습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<com.recordmanagement.habitlog.global.common.response.ApiResponse<GoalAchievementReportResponse>> getAchievementReport(
            Authentication authentication) {

        String userId = authentication.getName();
        log.info("Getting achievement report for user: {}", userId);

        Optional<Goal> currentGoal = goalApplicationService.getCurrentGoal(UserId.from(userId));
        long cumulativeCount = goalApplicationService.getCumulativeAchievementCount(UserId.from(userId));
        List<Goal> recentHistory = goalApplicationService.getGoalHistory(UserId.from(userId));

        GoalAchievementReportResponse response = GoalAchievementReportResponse.from(
                currentGoal.orElse(null), cumulativeCount, recentHistory);

        return ResponseEntity.ok(com.recordmanagement.habitlog.global.common.response.ApiResponse.success("목표 달성 보고서 조회가 성공적으로 완료되었습니다", response));
    }

    /**
     * 목표 달성 이력 조회
     *
     * @param authentication 인증 정보 (JWT 토큰에서 userId 추출)
     * @return 목표 달성 이력
     */
    @GetMapping("/achievement/history")
    @Operation(
            summary = "목표 달성 이력 조회",
            description = "사용자의 모든 목표 이력을 최신순으로 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "달성 이력 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.recordmanagement.habitlog.global.common.response.ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "totalCount": 5,
                                              "completedCount": 3,
                                              "goals": [
                                                {
                                                  "goalId": "goal-uuid-123",
                                                  "recordType": "HABIT",
                                                  "goalDays": 20,
                                                  "startDate": "2025-11-01",
                                                  "endDate": "2025-11-20",
                                                  "completedDays": 7,
                                                  "achievementRate": 35.0,
                                                  "finalTreeStage": "STAGE_2",
                                                  "status": "진행중",
                                                  "createdAt": "2025-11-01T09:00:00",
                                                  "completedAt": null
                                                },
                                                {
                                                  "goalId": "goal-uuid-456",
                                                  "recordType": "EXERCISE",
                                                  "goalDays": 10,
                                                  "startDate": "2025-10-20",
                                                  "endDate": "2025-10-29",
                                                  "completedDays": 10,
                                                  "achievementRate": 100.0,
                                                  "finalTreeStage": "STAGE_4",
                                                  "status": "완료",
                                                  "createdAt": "2025-10-20T09:00:00",
                                                  "completedAt": "2025-10-29T23:59:59"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (토큰 없음/만료/잘못됨)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "error": "토큰이 만료되었거나 유효하지 않습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<com.recordmanagement.habitlog.global.common.response.ApiResponse<GoalAchievementHistoryResponse>> getAchievementHistory(
            Authentication authentication) {

        String userId = authentication.getName();
        log.info("Getting achievement history for user: {}", userId);

        List<Goal> allGoals = goalApplicationService.getGoalHistory(UserId.from(userId));
        List<Goal> completedGoals = goalApplicationService.getCompletedGoals(UserId.from(userId));

        GoalAchievementHistoryResponse response = GoalAchievementHistoryResponse.from(allGoals, completedGoals);

        return ResponseEntity.ok(com.recordmanagement.habitlog.global.common.response.ApiResponse.success("목표 달성 이력 조회가 성공적으로 완료되었습니다", response));
    }

    /**
     * 새로운 목표 생성
     *
     * @param authentication 인증 정보 (JWT 토큰에서 userId 추출)
     * @param request 목표 생성 요청
     * @return 생성된 목표 정보
     */
    @PostMapping("/new")
    @Operation(
            summary = "새로운 목표 생성",
            description = "새로운 목표를 생성합니다. 진행중인 목표가 있으면 생성할 수 없습니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "목표 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.recordmanagement.habitlog.global.common.response.ApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "습관 목표 생성 성공",
                                            value = """
                                                    {
                                                      "statusCode": 200,
                                                      "code": "S200",
                                                      "message": "새로운 목표가 성공적으로 생성되었습니다",
                                                      "data": {
                                                        "goalId": "550e8400-e29b-41d4-a716-446655440010",
                                                        "recordType": "HABIT",
                                                        "goalDays": 30,
                                                        "startDate": "2025-11-10",
                                                        "endDate": "2025-12-09",
                                                        "message": "목표가 성공적으로 생성되었습니다."
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "운동 목표 생성 성공",
                                            value = """
                                                    {
                                                      "statusCode": 200,
                                                      "code": "S200", 
                                                      "message": "새로운 목표가 성공적으로 생성되었습니다",
                                                      "data": {
                                                        "goalId": "550e8400-e29b-41d4-a716-446655440011",
                                                        "recordType": "EXERCISE",
                                                        "goalDays": 20,
                                                        "startDate": "2025-11-10",
                                                        "endDate": "2025-11-29",
                                                        "message": "목표가 성공적으로 생성되었습니다."
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "일상 목표 생성 성공",
                                            value = """
                                                    {
                                                      "statusCode": 200,
                                                      "code": "S200",
                                                      "message": "새로운 목표가 성공적으로 생성되었습니다",
                                                      "data": {
                                                        "goalId": "550e8400-e29b-41d4-a716-446655440012",
                                                        "recordType": "DAILY", 
                                                        "goalDays": 10,
                                                        "startDate": "2025-11-10",
                                                        "endDate": "2025-11-19",
                                                        "message": "목표가 성공적으로 생성되었습니다."
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이미 진행중인 목표 존재, 유효하지 않은 파라미터 등)"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (토큰 없음/만료/잘못됨)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "토큰 만료",
                                    value = """
                                            {
                                              "error": "토큰이 만료되었거나 유효하지 않습니다."
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<com.recordmanagement.habitlog.global.common.response.ApiResponse<CreateGoalResponse>> createNewGoal(
            Authentication authentication,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "목표 생성 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateGoalRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "습관 목표 생성 요청",
                                            value = """
                                                    {
                                                      "recordType": "HABIT",
                                                      "goalDays": 30
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "운동 목표 생성 요청",
                                            value = """
                                                    {
                                                      "recordType": "EXERCISE",
                                                      "goalDays": 20
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "일상 목표 생성 요청",
                                            value = """
                                                    {
                                                      "recordType": "DAILY",
                                                      "goalDays": 10
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @RequestBody CreateGoalRequest request) {

        String userId = authentication.getName();
        log.info("Creating new goal for user: {}, request: {}", userId, request);

        request.validate();

        Goal createdGoal = goalApplicationService.createGoal(
                UserId.from(userId),
                request.getRecordType(),
                request.getGoalDays()
        );

        CreateGoalResponse response = CreateGoalResponse.from(createdGoal);

        return ResponseEntity.ok(com.recordmanagement.habitlog.global.common.response.ApiResponse.success("새로운 목표가 성공적으로 생성되었습니다", response));
    }

}