package com.recordmanagement.habitlog.domain.record.model;

import com.recordmanagement.habitlog.domain.common.BaseEntity;
import com.recordmanagement.habitlog.domain.record.model.ExerciseType;
import com.recordmanagement.habitlog.domain.user.model.UserId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * 운동 기록 도메인 엔티티
 * 
 * 사용자의 운동 활동을 기록하고 관리하는 도메인 모델입니다.
 * 운동 종류, 칼로리, 운동 시간, 몸무게, 걸음수 등의 상세 정보를 포함합니다.
 * 
 * 주요 기능:
 * - 운동 기록 생성 및 수정
 * - 10가지 운동 타입 지원 (헬스, 러닝, 워킹, 수영, 사이클링, 요가, 필라테스, 농구, 축구, 테니스)
 * - 운동 성과 데이터 기록 (칼로리, 시간, 몸무게, 걸음수)
 * - 개인 메모 추가 기능
 * - 생성/수정 시간 자동 관리
 * 
 * 도메인 규칙:
 * - 사용자당 하루에 하나의 운동 기록만 허용
 * - 운동 타입은 필수 선택 (10가지 사전 정의된 타입 중 선택)
 * - 칼로리, 운동시간, 몸무게, 걸음수는 선택 사항
 * - 음수 값은 허용하지 않음
 * - 메모는 자유 텍스트 (최대 1000자)
 * 
 * 사용 시나리오:
 * - 운동 후 성과 기록
 * - 체중 관리를 위한 데이터 추적
 * - 운동 습관 분석을 위한 기초 데이터
 * 
 * @author 전우선
 * @since 2025.08.19
 * @version 1.0.0
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "운동 기록 도메인 엔티티")
public class ExerciseRecord extends BaseEntity {
    
    @Schema(description = "사용자 ID")
    private UserId userId;
    
    @Schema(description = "기록 날짜")
    private LocalDate recordDate;
    
    @Schema(description = "운동 타입")
    private ExerciseType exerciseType;
    
    @Schema(description = "칼로리", example = "300")
    private Integer calories;
    
    @Schema(description = "운동 시간 (분)", example = "60")
    private Integer durationMinutes;
    
    @Schema(description = "몸무게 (kg)", example = "70.5")
    private Double weight;
    
    @Schema(description = "걸음수", example = "8000")
    private Integer steps;
    
    @Schema(description = "메모")
    private String memo;
    
    /**
     * 새로운 운동 기록 생성
     * 
     * 사용자가 운동 후 성과를 기록할 때 호출됩니다.
     * 고유 식별자(UUID)와 생성/수정 시간이 자동으로 설정됩니다.
     * 
     * 데이터 검증:
     * - 칼로리: 0 이상의 정수 (null 허용)
     * - 운동시간: 0 이상의 정수 (분 단위, null 허용)
     * - 몸무게: 0 이상의 실수 (kg 단위, null 허용)
     * - 걸음수: 0 이상의 정수 (null 허용)
     * 
     * @param userId 기록을 작성하는 사용자의 ID (필수)
     * @param recordDate 운동 날짜 (필수, 하루에 하나만 허용)
     * @param exerciseType 운동 종류 (필수, ExerciseType enum 값)
     * @param calories 소모 칼로리 (선택, 0 이상)
     * @param durationMinutes 운동 시간 분 단위 (선택, 0 이상)
     * @param weight 측정한 몸무게 kg 단위 (선택, 0 이상)
     * @param steps 걸음수 (선택, 0 이상)
     * @param memo 개인 메모 (선택, 최대 1000자)
     * 
     * @throws IllegalArgumentException userId가 null인 경우
     * @throws IllegalArgumentException recordDate가 null인 경우
     * @throws IllegalArgumentException exerciseType이 null인 경우
     * @throws IllegalArgumentException 칼로리, 시간, 몸무게, 걸음수가 음수인 경우
     */
    public ExerciseRecord(UserId userId, LocalDate recordDate, ExerciseType exerciseType, 
                         Integer calories, Integer durationMinutes, Double weight, Integer steps, String memo) {
        this.userId = userId;
        this.recordDate = recordDate;
        this.exerciseType = exerciseType;
        this.calories = calories;
        this.durationMinutes = durationMinutes;
        this.weight = weight;
        this.steps = steps;
        this.memo = memo;
    }
    
    /**
     * 운동 기록 수정
     */
    public void updateRecord(ExerciseType exerciseType, Integer calories, Integer durationMinutes, 
                           Double weight, Integer steps, String memo) {
        this.exerciseType = exerciseType;
        this.calories = calories;
        this.durationMinutes = durationMinutes;
        this.weight = weight;
        this.steps = steps;
        this.memo = memo;
        this.updateTimestamp();
    }
}