package com.recordmanagement.habitlog.domain.record.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 습관 타입 열거형
 * 
 * 사용자가 선택할 수 있는 10가지 사전 정의된 습관 타입을 관리합니다.
 * UI 회의에서 결정된 사항에 따라 습관 종류를 제한하여 일관된 사용자 경험을 제공합니다.
 * 
 * 지원 습관 목록:
 * - WATER: 물 마시기
 * - WALK: 산책하기  
 * - EXERCISE: 운동하기
 * - READING: 독서하기
 * - MEDITATION: 명상하기
 * - CLEAN: 청소하기
 * - STUDY: 공부하기
 * - SLEEP: 일찍 자기
 * - VITAMIN: 비타민 섭취
 * - DIARY: 일기 쓰기
 * 
 * 비즈니스 규칙:
 * - 총 10가지 습관만 지원 (확장성 고려하여 enum 사용)
 * - 각 습관은 한국어 표시명을 가짐
 * - 새로운 습관 추가 시 이 enum을 수정해야 함
 * 
 * @author 전우선
 * @since 2025.08.19
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "습관 타입")
public enum HabitType {
    
    @Schema(description = "물 마시기")
    WATER("물 마시기"),
    
    @Schema(description = "산책하기")
    WALK("산책하기"),
    
    @Schema(description = "운동하기")
    EXERCISE("운동하기"),
    
    @Schema(description = "독서하기")
    READING("독서하기"),
    
    @Schema(description = "명상하기")
    MEDITATION("명상하기"),
    
    @Schema(description = "청소하기")
    CLEAN("청소하기"),
    
    @Schema(description = "공부하기")
    STUDY("공부하기"),
    
    @Schema(description = "일찍 자기")
    SLEEP("일찍 자기"),
    
    @Schema(description = "비타민 섭취")
    VITAMIN("비타민 섭취"),
    
    @Schema(description = "일기 쓰기")
    DIARY("일기 쓰기");

    /**
     * 사용자에게 표시되는 한국어 습관명
     */
    @Schema(description = "습관 표시명", example = "물 마시기")
    private final String displayName;
}