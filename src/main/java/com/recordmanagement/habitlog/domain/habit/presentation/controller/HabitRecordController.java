package com.recordmanagement.habitlog.domain.habit.presentation.controller;

import com.recordmanagement.habitlog.domain.habit.presentation.dto.CreateHabitRecordRequest;
import com.recordmanagement.habitlog.domain.habit.presentation.dto.UpdateHabitRecordRequest;
import com.recordmanagement.habitlog.domain.habit.presentation.dto.UpdateCompletionStatusRequest;
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
               
               ## 모든 사용자 작성 가능
               - **메인 기록 타입이 습관인 사용자**: 메인 또는 서브 기록으로 작성
               - **메인 기록 타입이 운동/일상인 사용자**: 서브 기록으로 작성
               
               ## 메인 습관 기록 자동 생성 시스템 (v1.8.5)
               
               **자동 생성 범위**: 메인 습관 기록 작성 시 목표 종료일까지 DB에 자동 생성
               ```
               예시: 11월 19일에 30일 목표 메인 습관 기록 작성
               → 11월 19일~12월 18일까지 자동 생성 (30일간)
               ```
               
               **캘린더 표시 제한**: DB에 미래 기록이 있어도 API는 오늘까지만 응답
               ```
               DB: 11월 19일~12월 18일 (30개 기록)
               API 응답: 11월 19일~11월 19일 (오늘 1개만)
               ```
               
               **2단계 시스템**: 
               - **1단계 (등록)**: isCompleted=false → 회색 아이콘
               - **2단계 (완료)**: isCompleted=true → 색상 아이콘
               
               ## 필수/선택 항목
               **필수**: habitType, notificationEnabled, recordDate  
               **선택**: notificationTime, memo, isMainRecord
               
               ## 메인/서브 기록 결정 로직
               - **명시적 설정**: isMainRecord 값 직접 사용
               - **자동 결정**: 
                 - 이미 메인 습관 기록 존재 → 서브 기록
                 - 습관 타입 사용자 → 메인 가능성 있음
                 - 운동/일상 타입 사용자 → 서브 기록
               
               ## 기록 제한 사항
               - 하루 최대 2개 습관 기록, 최대 2가지 기록 타입
               - 습관 타입 사용자: 목표 기간 내 날짜만 허용
               - 다른 타입 사용자: 과거~오늘 날짜만 허용
               """,
               security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "습관기록 작성 성공",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = {
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "습관 타입 사용자의 메인 기록",
                        summary = "메인 기록 타입이 습관인 사용자의 메인 습관 기록 작성 + 목표 기간 자동 생성 (DB에만, API는 오늘만)",
                        value = """
                            {
                              "statusCode": 200,
                              "code": "SUCCESS",
                              "message": "요청이 성공적으로 처리되었습니다.",
                              "data": {
                                "id": "550e8400-e29b-41d4-a716-446655440000",
                                "type": "HABIT",
                                "recordDate": "2025-11-19",
                                "recordTime": null,
                                "createdAt": "2025-11-19T14:30:00",
                                "updatedAt": "2025-11-19T14:30:00",
                                "habitType": "WATER_DRINKING",
                                "notificationEnabled": true,
                                "notificationTime": "09:00:00",
                                "memo": "30일 물마시기 도전 시작! (DB에는 목표 종료일까지 자동 생성, API는 오늘까지만 응답)",
                                "isCompleted": false,
                                "isMainRecord": true
                              }
                            }
                            """
                    ),
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "운동 타입 사용자의 서브 기록",
                        summary = "메인 기록 타입이 운동인 사용자의 서브 습관 기록 작성",
                        value = """
                            {
                              "statusCode": 200,
                              "code": "SUCCESS",
                              "message": "요청이 성공적으로 처리되었습니다.",
                              "data": {
                                "id": "550e8400-e29b-41d4-a716-446655440001",
                                "type": "HABIT",
                                "recordDate": "2025-11-06",
                                "recordTime": null,
                                "createdAt": "2025-11-06T15:20:00",
                                "updatedAt": "2025-11-06T15:20:00",
                                "habitType": "READING",
                                "notificationEnabled": false,
                                "notificationTime": null,
                                "memo": "운동과 함께 독서도 시작 (서브 습관, 자동 생성 안됨)",
                                "isCompleted": false,
                                "isMainRecord": false
                              }
                            }
                            """
                    )
                }
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
                                "message": "하루에 등록할 수 있는 습관 기록은 최대 2개입니다.",
                                "data": null
                            }
                            """
                    ),
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "기록 종류 2가지 제한 초과",
                        summary = "하루 최대 2개 기록 제한 초과",
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
               description = """
               기존 습관기록을 수정합니다.
               
               **수정 가능한 모든 습관 기록:**
               - 메인 기록 타입에 관계없이 모든 사용자의 습관 기록 수정 가능
               - 습관 타입, 알림 설정, 메모, 메인/서브 상태 변경 가능
               
               **메인/서브 기록 결정:**
               - isMainRecord가 명시적으로 설정된 경우: 해당 값 사용
               - isMainRecord가 설정되지 않은 경우: 자동 결정
                 • 해당 날짜에 다른 메인 습관 기록이 있으면 → 서브 기록
                 • 사용자의 메인 기록 타입이 HABIT → 메인 가능성 있음
                 • 사용자의 메인 기록 타입이 EXERCISE/DAILY → 서브 기록
               
               **서브→메인 변경 시 자동 처리 (v1.8.5):**
               1. **오늘자 기존 메인**: 서브 기록으로 변경 (완료상태/내용 유지)
               2. **내일부터 기존 메인**: 목표 종료일까지 기존 메인 습관 기록 삭제 
               3. **새로운 메인 생성**: 내일부터 목표 종료일까지 새로운 메인 습관 기록 자동 생성
               4. **2단계 시스템**: 생성 시 isCompleted=false (회색), 완료 API로 true 변경 (색상)
               5. **캘린더 표시**: 작성된 모든 습관 기록 표시 (오늘까지만 API 응답)
               6. **목표 진행률 업데이트**: 습관 기록 변경 후 자동으로 목표 달성률 계산
               
               **제한 사항:**
               - 운동/일상 타입 사용자: 자동 일괄 업데이트 없음 (해당 기록만 수정)
               """,
               security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
        description = "습관기록 수정 성공 (서브→메인 변경 시 기존 메인 삭제 후 새 메인 자동 생성 포함)",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                name = "메인 습관기록 수정 성공",
                summary = "메인 습관기록 수정 성공 - 서브→메인 변경 시 기존 메인 삭제 후 새 메인 자동 생성",
                value = """
                {
                    "statusCode": 200,
                    "code": "S20000",
                    "message": "정상적으로 처리되었습니다.",
                    "data": {
                        "id": "habit_record_123",
                        "type": "HABIT",
                        "recordDate": "2025-11-01",
                        "recordTime": null,
                        "createdAt": "2025-11-01T14:30:00",
                        "updatedAt": "2025-11-01T16:45:00",
                        "habitType": "READING",
                        "notificationEnabled": false,
                        "notificationTime": "21:00:00",
                        "memo": "서브 독서가 메인으로 승격! (기존 메인 삭제 후 자동 생성됨)",
                        "isCompleted": false,
                        "isMainRecord": true
                    }
                }
                """
            )
        )),
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
               description = """
               특정 습관기록을 삭제합니다.
               
               **삭제 정책:**
               
               **서브 습관 기록 삭제:**
               - 해당 서브 기록만 삭제됩니다.
               
               **메인 습관 기록 삭제 (v1.8.5):**
               - **메인+서브 상황**: 오늘부터 목표 종료일까지 기존 메인 기록 삭제 → 서브 기록 중 하나가 메인으로 전환 → 새 메인 습관으로 내일부터 목표 종료일까지 자동 생성
               - **메인만 상황**: 메인 기록 삭제 시 **목표 기간 전체(시작일~종료일)의 해당 습관 모든 기록**이 삭제됩니다.
               
               **중요한 삭제 동작 (정리본 반영):**
               - 메인+서브 → 메인 삭제 시: 오늘부터 기존 메인 습관 완전 삭제, 서브가 메인으로 승격하여 내일부터 목표 종료일까지 자동 생성
               - 메인만 → 메인 삭제 시: **습관 포기로 간주**하여 목표기간 전체의 해당 습관 모든 기록이 삭제
               - **되돌릴 수 없는 작업**이므로 신중하게 사용해주세요.
               
               **자동 처리 상세:**
               - 메인+서브 상황: 메인 습관 기록 삭제 → 오늘부터 목표 종료일까지 기존 메인 기록 삭제 → 서브가 메인으로 전환 → 내일부터 목표 종료일까지 새 메인 습관 자동 생성
               - 메인만 상황: 메인 습관 기록 삭제 → 습관 포기로 간주 → 목표 기간 전체의 해당 습관 모든 기록 삭제 (시작일~종료일 모든 기록 포함)
               """,
               security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "습관기록 삭제 성공",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = {
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "서브 기록 삭제",
                        summary = "서브 습관기록 삭제 성공",
                        value = """
                            {
                                "statusCode": 200,
                                "code": "S20000",
                                "message": "정상적으로 처리되었습니다.",
                                "data": null
                            }
                            """
                    ),
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "메인+서브 상황 메인 삭제",
                        summary = "메인 기록 삭제 시 오늘부터 기존 메인 삭제 → 서브→메인 전환 → 내일부터 새 메인 자동 생성",
                        value = """
                            {
                                "statusCode": 200,
                                "code": "S20000",
                                "message": "정상적으로 처리되었습니다.",
                                "data": null
                            }
                            """
                    ),
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "메인만 상황 메인 삭제",
                        summary = "메인 기록만 있을 때 삭제 - 습관 포기로 전체 기간 삭제",
                        value = """
                            {
                                "statusCode": 200,
                                "code": "S20000",
                                "message": "정상적으로 처리되었습니다.",
                                "data": null
                            }
                            """
                    )
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "습관기록을 찾을 수 없음",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "기록 없음",
                    summary = "해당 ID의 습관기록이 존재하지 않음",
                    value = """
                        {
                            "statusCode": 404,
                            "code": "E40406",
                            "message": "존재하지 않는 기록입니다.",
                            "data": null
                        }
                        """
                )
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
               description = """
               습관기록의 완료 상태를 토글합니다. 홈 화면에서 간단하게 완료/미완료 상태를 변경할 때 사용합니다.
               
               ## 중요한 제한사항 (v1.8.5)
               **오늘 날짜만 완료 가능**: 과거나 미래 날짜의 습관 기록은 완료/미완료 상태를 변경할 수 없습니다.
               - 오늘 날짜의 습관 기록만 완료 상태 변경 가능
               - 과거/미래 날짜 시도 시 E40413 에러 반환
               - 목적: 실제 행동 기반 시스템, 당일 집중
               """,
               security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
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
        )),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "오늘이 아닌 날짜의 습관 완료 상태 변경 시도",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = """
                    {
                        "statusCode": 400,
                        "code": "E40413",
                        "message": "습관 완료 상태는 오늘 날짜에만 변경할 수 있습니다.",
                        "data": null
                    }
                    """
                )
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