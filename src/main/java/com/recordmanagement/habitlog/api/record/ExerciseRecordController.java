package com.recordmanagement.habitlog.api.record;

import com.recordmanagement.habitlog.application.record.ExerciseRecordApplicationService;
import com.recordmanagement.habitlog.application.record.dto.ExerciseRecordCreateCommand;
import com.recordmanagement.habitlog.application.record.dto.ExerciseRecordResponse;
import com.recordmanagement.habitlog.common.response.ApiResponse;
import com.recordmanagement.habitlog.api.record.dto.ExerciseRecordCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 운동 기록 관련 API 컨트롤러
 * 
 * 사용자의 운동 활동 기록을 관리하는 REST API를 제공합니다.
 * 글쓰기 화면에서 주로 사용되며, 운동 종류별 상세 데이터를 기록할 수 있습니다.
 * 
 * 주요 기능:
 * - 운동 기록 생성
 * - 운동 기록 수정
 * - 운동 기록 삭제  
 * - 특정 날짜의 운동 기록 조회
 * - 운동 기록 목록 조회
 * 
 * 지원하는 운동 타입:
 * - WEIGHT_TRAINING: 웨이트 트레이닝
 * - RUNNING: 러닝
 * - WALKING: 걷기/산책
 * - SWIMMING: 수영
 * - CYCLING: 사이클링
 * - YOGA: 요가
 * - PILATES: 필라테스
 * - BASKETBALL: 농구
 * - SOCCER: 축구
 * - TENNIS: 테니스
 * 
 * 기록 가능한 데이터:
 * - 운동 종류 (필수)
 * - 칼로리 소모량 (선택)
 * - 운동 시간 (분 단위, 선택)
 * - 몸무게 (kg 단위, 선택)
 * - 걸음수 (선택)
 * - 개인 메모 (선택)
 * 
 * 사용 시나리오:
 * - 주로 "글쓰기" 화면에서 사용
 * - 하루에 하나의 운동 기록만 작성 가능
 * - 체중 관리 및 운동 성과 추적 용도
 * - 달력 화면에서는 표시되지 않음 (글쓰기 전용)
 * 
 * 데이터 검증:
 * - 모든 수치형 데이터는 0 이상의 값만 허용
 * - 날짜별 중복 기록 방지
 * - 메모는 최대 1000자 제한
 * 
 * 인증 요구사항:
 * - 모든 API는 JWT Bearer 토큰 인증 필요
 * - 사용자는 본인의 운동 기록만 접근 가능
 * 
 * @author 전우선
 * @since 2025.08.19
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/exercise-records")
@Tag(name = "운동 기록", description = "운동 기록 관련 API")
public class ExerciseRecordController {
    
    private final ExerciseRecordApplicationService exerciseRecordApplicationService;
    
    public ExerciseRecordController(ExerciseRecordApplicationService exerciseRecordApplicationService) {
        this.exerciseRecordApplicationService = exerciseRecordApplicationService;
    }
    
    /**
     * 운동 기록 생성/수정 API
     */
    @Operation(
        summary = "운동 기록 생성/수정",
        description = "운동 기록을 생성하거나 수정합니다. 하루에 하나의 운동 기록만 가능합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping
    public ResponseEntity<ApiResponse<ExerciseRecordResponse>> createOrUpdateExerciseRecord(
            @Valid @RequestBody ExerciseRecordCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("운동 기록 생성/수정 요청: userId={}, date={}", 
                userDetails.getUsername(), request.getRecordDate());
        
        ExerciseRecordCreateCommand command = new ExerciseRecordCreateCommand(
                userDetails.getUsername(),
                request.getRecordDate(),
                request.getExerciseType(),
                request.getCalories(),
                request.getDurationMinutes(),
                request.getWeight(),
                request.getSteps(),
                request.getMemo()
        );
        
        ExerciseRecordResponse response = exerciseRecordApplicationService.createOrUpdateExerciseRecord(command);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 운동 기록 기간별 조회 API
     */
    @Operation(
        summary = "운동 기록 기간별 조회",
        description = "사용자의 기간별 운동 기록을 조회합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<ExerciseRecordResponse>>> getExerciseRecords(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("운동 기록 기간별 조회 요청: userId={}, period={} ~ {}", 
                userDetails.getUsername(), startDate, endDate);
        
        List<ExerciseRecordResponse> response = exerciseRecordApplicationService.getExerciseRecords(
                userDetails.getUsername(),
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 특정 날짜 운동 기록 조회 API
     */
    @Operation(
        summary = "특정 날짜 운동 기록 조회",
        description = "특정 날짜의 운동 기록을 조회합니다.",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/{date}")
    public ResponseEntity<ApiResponse<ExerciseRecordResponse>> getExerciseRecord(
            @PathVariable String date,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("특정 날짜 운동 기록 조회 요청: userId={}, date={}", 
                userDetails.getUsername(), date);
        
        Optional<ExerciseRecordResponse> response = exerciseRecordApplicationService.getExerciseRecord(
                userDetails.getUsername(),
                LocalDate.parse(date)
        );
        
        if (response.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(response.get()));
        } else {
            return ResponseEntity.ok(ApiResponse.success(null));
        }
    }
}