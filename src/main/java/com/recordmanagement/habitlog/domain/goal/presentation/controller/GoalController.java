package com.recordmanagement.habitlog.domain.goal.presentation.controller;

import com.recordmanagement.habitlog.domain.goal.application.dto.CompleteGoalResponse;
import com.recordmanagement.habitlog.domain.goal.application.dto.CreateGoalRequest;
import com.recordmanagement.habitlog.domain.goal.application.dto.CreateGoalResponse;
import com.recordmanagement.habitlog.domain.goal.application.dto.CurrentGoalResponse;
import com.recordmanagement.habitlog.domain.goal.application.dto.GoalAchievementHistoryResponse;
import com.recordmanagement.habitlog.domain.goal.application.dto.GoalAchievementReportResponse;
import com.recordmanagement.habitlog.domain.goal.application.service.GoalApplicationService;
import com.recordmanagement.habitlog.domain.goal.domain.model.Goal;
import com.recordmanagement.habitlog.domain.user.domain.model.UserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.recordmanagement.habitlog.global.config.exception.CustomException;

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
@Tag(name = "Goal", description = """
    목표 관리 API
    
    ### 목표 시스템 특징
    - **자동 완료**: 목표 기간이 끝나면 매일 자정에 자동으로 완료 처리
    - **User 동기화**: 목표 완료 시 사용자 정보 자동 동기화 (mainRecordType, goalDays → null)
    - **진행률 추적**: 기록 생성 시마다 자동으로 목표 진행률 업데이트
    
    ### 목표 생명주기
    1. 목표 생성 → IN_PROGRESS 상태
    2. 기간 중 기록 작성으로 진행률 업데이트
    3. 기간 만료 시 자동 COMPLETED 처리 (매일 00:00 스케줄러)
    4. 사용자 정보 동기화 및 새 목표 생성 가능
    """)
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
            description = """
                사용자의 현재 진행중인 목표를 조회합니다.
                
                ### 조회 조건
                - 기간 내 (startDate ≤ 오늘 ≤ endDate)
                - 상태가 IN_PROGRESS
                - 기간이 지난 목표는 매일 자정 스케줄러가 자동 완료 처리
                
                ### 응답 방식
                - **목표 있음**: 목표 상세 정보 반환 (goalId, recordType, 진행률 등)
                - **목표 없음**: data: null 반환 (목표 설정 배너 표시 조건)
                """,
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
            description = "현재 목표 진행상황과 누적 달성 횟수, 최근 이력을 포함한 보고서를 조회합니다. recentHistory는 종료일 기준 내림차순으로 정렬됩니다.",
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
                                            summary = "currentPeriod가 null이고, recentHistory[0]이 가장 최근 종료된 목표 (종료일 기준 내림차순)",
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
        List<Goal> recentHistory = goalApplicationService.getGoalHistoryByEndDate(UserId.from(userId));

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
            description = """
                새로운 목표를 생성합니다.
                
                ### 생성 조건
                - 현재 진행중인 목표가 없어야 함
                - 목표일수는 10, 20, 30일만 가능
                - 목표 시작일은 생성 당일부터 자동 설정
                
                ### 자동 처리
                1. **User 정보 동기화**: mainRecordType, goalDays 자동 업데이트
                2. **기간 자동 완료**: endDate 도달 시 매일 자정 스케줄러가 자동 완료
                3. **나무 성장**: 기록 작성 시마다 treeStage 자동 업데이트
                
                ### 중복 생성 방지
                - 이미 진행중인 목표가 있으면 409 에러 반환
                - 기존 목표 완료 후에만 새 목표 생성 가능
                """,
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
                    description = "잘못된 요청 (유효하지 않은 파라미터 등)"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 진행중인 목표가 존재",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "목표 중복 생성 시도",
                                    value = """
                                            {
                                              "statusCode": 409,
                                              "code": "E40904",
                                              "message": "이미 진행중인 목표가 존재합니다."
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

    /**
     * 목표 초기화 (현재 목표 강제 완료)
     *
     * 중간에 메인 기록 타입을 변경하고 싶을 때 사용합니다.
     * 현재 목표를 완료 처리하고 recentHistory[0]에 저장한 후,
     * 새로운 목표를 설정할 수 있도록 초기화합니다.
     *
     * @param authentication 인증 정보 (JWT 토큰에서 userId 추출)
     * @return 강제 완료 결과
     */
    @PatchMapping("/current/force-complete")
    @Operation(
            summary = "목표 초기화 (현재 목표 강제 완료)",
            description = """
                현재 진행중인 목표를 강제로 완료 상태로 변경하여 목표를 초기화합니다.

                ### 🎯 목표 초기화가 필요한 경우

                사용자가 목표를 설정한 후 중간에 **메인 기록 타입을 변경**하고 싶을 때 사용합니다.

                **예시 시나리오:**
                1. 운동 기록으로 20일 목표를 설정함
                2. 20일 동안 운동이 메인 기록 타입으로 고정됨
                3. 중간에 운동을 그만두고 **습관 기록으로 전환하고 싶음**
                4. 하지만 20일이 끝나기 전까지는 운동이 메인으로 유지됨
                5. **목표 초기화**를 통해 현재 목표를 완료 처리하고 새로운 목표 설정 가능

                ### 📌 사용 목적
                - **사용자 자율성 부여**: 목표 기간 중에도 메인 기록 타입 변경 가능
                - **유연한 목표 관리**: 운동 → 습관, 습관 → 일상 등 자유롭게 전환
                - **기록 보존**: 이전 목표는 달성 보고서(`recentHistory[0]`)에 히스토리로 저장

                ### 📋 완료 후 변화
                1. **목표 상태**: IN_PROGRESS → COMPLETED
                2. **완료 시간**: completedAt 자동 설정
                3. **진행률 유지**: 현재 달성률 그대로 유지
                4. **User 정보**: mainRecordType, goalDays → **null로 초기화**
                5. **미래 메인 습관 기록**: 내일부터의 기록들 자동 삭제 (목표 타입 관계없이)
                6. **달성 보고서**: **recentHistory[0]에 완료된 목표로 저장**
                7. **새 목표**: 즉시 새로운 목표 설정 가능 (다른 타입으로 변경 가능)

                ### 🔄 사용 흐름
                1. **목표 초기화 호출** → 현재 목표 완료 처리
                2. **달성 보고서 확인** → `recentHistory[0]`에서 이전 목표 확인 가능
                3. **새 목표 설정** → 원하는 타입(HABIT/EXERCISE/DAILY)으로 새로운 목표 생성

                ### 💡 참고사항
                - 메인 화면의 ALL 타입에서는 메인 기록 타입이 우선 표시됨
                - 목표 초기화 후에는 다시 목표를 설정해야 메인 화면 사용 가능
                - 이전 목표 기록들은 삭제되지 않고 히스토리에 보존됨
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "목표 초기화 성공 (완료된 목표는 recentHistory[0]에 저장됨)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.recordmanagement.habitlog.global.common.response.ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "목표 초기화 성공",
                                    value = """
                                            {
                                              "statusCode": 200,
                                              "code": "S200",
                                              "message": "현재 목표가 성공적으로 완료되었습니다",
                                              "data": {
                                                "completedGoalId": "550e8400-e29b-41d4-a716-446655440000",
                                                "completedRecordType": "HABIT",
                                                "completedGoalDays": 30,
                                                "finalAchievementRate": 73.3,
                                                "canCreateNewGoal": true,
                                                "message": "목표가 완료되었습니다. 달성 보고서 recentHistory[0]에서 확인할 수 있습니다."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "완료할 목표가 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "목표 없음",
                                    value = """
                                            {
                                              "statusCode": 404,
                                              "code": "E404",
                                              "message": "현재 진행중인 목표가 없습니다."
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
    public ResponseEntity<com.recordmanagement.habitlog.global.common.response.ApiResponse<CompleteGoalResponse>> forceCompleteCurrentGoal(
            Authentication authentication) {

        String userId = authentication.getName();
        log.info("목표 초기화: 현재 목표 강제 완료 요청 - userId: {}", userId);

        // 완료 전 현재 목표 정보 조회 (응답용)
        Optional<Goal> currentGoal = goalApplicationService.getCurrentGoal(UserId.from(userId));
        if (currentGoal.isEmpty()) {
            throw new CustomException(com.recordmanagement.habitlog.global.config.exception.ErrorCode.GOAL_NOT_FOUND);
        }

        Goal goalToComplete = currentGoal.get();
        
        // 목표 강제 완료 실행
        goalApplicationService.forceCompleteCurrentGoal(UserId.from(userId));
        
        // 새 목표 설정 가능 여부 확인
        boolean canCreateNew = goalApplicationService.canCreateNewGoal(UserId.from(userId));

        CompleteGoalResponse response = CompleteGoalResponse.from(goalToComplete, canCreateNew);

        return ResponseEntity.ok(com.recordmanagement.habitlog.global.common.response.ApiResponse.success("현재 목표가 성공적으로 완료되었습니다", response));
    }

}