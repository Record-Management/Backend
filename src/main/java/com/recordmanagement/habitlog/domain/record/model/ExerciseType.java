package com.recordmanagement.habitlog.domain.record.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 운동 타입 열거형
 * 
 * 사용자가 선택할 수 있는 10가지 사전 정의된 운동 타입을 관리합니다.
 * UI 회의에서 결정된 사항에 따라 운동 종류를 제한하여 일관된 사용자 경험을 제공합니다.
 * 
 * 지원 운동 목록:
 * - WEIGHT_TRAINING: 헬스/웨이트 트레이닝
 * - RUNNING: 러닝
 * - WALKING: 워킹
 * - SWIMMING: 수영
 * - CYCLING: 사이클링
 * - YOGA: 요가
 * - PILATES: 필라테스
 * - BASKETBALL: 농구
 * - SOCCER: 축구
 * - TENNIS: 테니스
 * 
 * 비즈니스 규칙:
 * - 총 10가지 운동만 지원 (확장성 고려하여 enum 사용)
 * - 각 운동은 한국어 표시명을 가짐
 * - 새로운 운동 추가 시 이 enum을 수정해야 함
 * 
 * @author 전우선
 * @since 2025.08.19
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "운동 타입")
public enum ExerciseType {
    
    @Schema(description = "헬스/웨이트 트레이닝")
    WEIGHT_TRAINING("헬스"),
    
    @Schema(description = "러닝")
    RUNNING("러닝"),
    
    @Schema(description = "워킹")
    WALKING("워킹"),
    
    @Schema(description = "수영")
    SWIMMING("수영"),
    
    @Schema(description = "사이클링")
    CYCLING("사이클링"),
    
    @Schema(description = "요가")
    YOGA("요가"),
    
    @Schema(description = "필라테스")
    PILATES("필라테스"),
    
    @Schema(description = "농구")
    BASKETBALL("농구"),
    
    @Schema(description = "축구")
    SOCCER("축구"),
    
    @Schema(description = "테니스")
    TENNIS("테니스");

    /**
     * 사용자에게 표시되는 한국어 운동명
     */
    @Schema(description = "운동 표시명", example = "헬스")
    private final String displayName;
}