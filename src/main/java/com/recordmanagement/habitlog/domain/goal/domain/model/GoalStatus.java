package com.recordmanagement.habitlog.domain.goal.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 목표 상태 열거형
 *
 * @author 전우선
 * @since 2025.11.04
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum GoalStatus {

    /**
     * 진행중
     */
    IN_PROGRESS("진행중"),

    /**
     * 완료 (목표 기간 종료)
     */
    COMPLETED("완료"),

    /**
     * 포기/중단
     */
    ABANDONED("포기");

    private final String displayName;
}