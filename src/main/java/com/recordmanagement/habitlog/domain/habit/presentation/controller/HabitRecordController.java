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
               
               ** 모든 사용자 작성 가능:**
               - 메인 기록 타입이 습관인 사용자: 메인 또는 서브 기록으로 작성
               - 메인 기록 타입이 운동/일상인 사용자: 서브 기록으로 작성
               
               ** 필수 항목:**
               - habitType: 습관 종류 (필수)
               - notificationEnabled: 알림 설정 여부 (필수)
               - recordDate: 기록 날짜 (필수)
               
               **선택 항목:**
               - notificationTime: 알림 시간
               - memo: 메모/글쓰기
               - isMainRecord: 메인 기록 여부 (명시적 설정)
               
               ** 습관 기록 특징 (v1.8.2):**
               - **현재 날짜에만 생성**: 습관 등록 시 현재 날짜에만 기록 생성 (미래 날짜 미리 생성하지 않음)
               - **2단계 시스템**: 등록(회색, isCompleted=false) → 완료(색상, isCompleted=true)
               - **습관 타입 특별 표시**: 작성된 모든 습관 기록 캘린더 표시 (과거/현재)
               - **다른 타입 사용자**: 모든 날짜 습관 기록 표시 (서브 기록으로)
               - **목표 진행률 자동 업데이트**: 습관 기록 생성/완료 시 목표 달성률 실시간 반영
               
               ** 메인/서브 기록 결정 로직:**
               - isMainRecord가 명시적으로 설정된 경우: 해당 값 사용
               - isMainRecord가 설정되지 않은 경우: 자동 결정
                 • 이미 메인 습관 기록이 있으면 → 서브 기록
                 • 사용자의 메인 기록 타입이 HABIT → 메인 가능성 있음
                 • 사용자의 메인 기록 타입이 EXERCISE/DAILY → 서브 기록
               
               **기록 제한:**
               - 하루 최대 2개의 습관 기록 작성 가능
               - 하루 최대 2가지 기록 타입 작성 가능
               - 습관 타입 사용자: 습관 목표 기간 내 날짜만 허용 (현재 날짜 위주)
               - 다른 타입 사용자: 과거~오늘 날짜만 허용
               - 미래 날짜 미리 생성 안함: 실제 작성 시점에만 기록 생성
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
                        summary = "메인 기록 타입이 습관인 사용자의 메인 습관 기록 작성",
                        value = """
                            {
                              "statusCode": 200,
                              "code": "SUCCESS",
                              "message": "요청이 성공적으로 처리되었습니다.",
                              "data": {
                                "id": "550e8400-e29b-41d4-a716-446655440000",
                                "type": "HABIT",
                                "recordDate": "2025-11-06",
                                "recordTime": null,
                                "createdAt": "2025-11-06T14:30:00",
                                "updatedAt": "2025-11-06T14:30:00",
                                "habitType": "WATER_DRINKING",
                                "notificationEnabled": true,
                                "notificationTime": "09:00:00",
                                "memo": "물을 꾸준히 마시기 시작!",
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
                                "memo": "운동과 함께 독서도 시작",
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
               
               **자동 처리 (습관 타입 사용자만):**
               1. **오늘 기록 생성**: 현재 날짜에만 기록 생성 (미래 날짜 미리 생성하지 않음)
               2. **2단계 시스템**: 생성 시 isCompleted=false (회색), 완료 API로 true 변경 (색상)
               3. **캘린더 특별 표시**: 작성된 모든 습관 기록 표시 (과거/현재)
               4. **기존 메인 기록 변경**: 새로운 메인 기록 생성 시 기존 메인 기록들을 서브로 변경
               5. **목표 진행률 업데이트**: 습관 기록 생성 후 자동으로 목표 달성률 계산
               
               **제한 사항:**
               - 운동/일상 타입 사용자: 자동 일괄 업데이트 없음 (해당 기록만 수정)
               """,
               security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
        description = "습관기록 수정 성공 (습관 타입 사용자의 메인 기록 변경시 자동 일괄 업데이트 포함)",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                name = "메인 습관기록 수정 성공",
                summary = "메인 습관기록 수정 성공 - 자동 일괄 업데이트 포함",
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
                        "memo": "독서로 변경!",
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
               
               **메인 습관 기록 삭제:**
               - **메인+서브 상황**: 메인 기록 삭제 시 서브 기록 중 하나가 자동으로 메인으로 전환됩니다.
               - **메인만 상황**: 메인 기록 삭제 시 **목표 기간 전체(시작일~종료일)의 해당 습관 모든 기록**이 삭제됩니다.
               
               **중요한 삭제 동작:**
               - 서브→메인 전환 시 목표 기간까지의 모든 기록이 새 메인 습관으로 업데이트됩니다.
               - 메인만 삭제 시 **습관 포기로 간주**하여 목표기간 전체의 해당 습관 모든 기록이 삭제됩니다.
               - **되돌릴 수 없는 작업**이므로 신중하게 사용해주세요.
               
               **자동 처리 상세:**
               - 메인+서브 상황: 메인 습관 기록 삭제 → 서브 기록 중 하나가 메인으로 전환 → 목표 기간까지 모든 메인 기록이 새 습관으로 업데이트
               - 메인만 상황: 메인 습관 기록 삭제 → 습관 포기로 간주 → 목표 기간 전체의 해당 습관 모든 기록 삭제 (과거+미래 기록 모두 포함)
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
                        summary = "메인 기록 삭제 시 서브→메인 자동 전환",
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
               description = "습관기록의 완료 상태를 토글합니다. 홈 화면에서 간단하게 완료/미완료 상태를 변경할 때 사용합니다.",
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