package com.recordmanagement.habitlog.domain.goal.domain.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 목표 식별자 Value Object
 *
 * @author 전우선
 * @since 2025.11.04
 * @version 1.0.0
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoalId {

    private String value;

    private GoalId(String value) {
        this.value = value;
    }

    /**
     * 새로운 GoalId 생성
     *
     * @return 새로운 GoalId
     */
    public static GoalId generate() {
        return new GoalId(UUID.randomUUID().toString());
    }

    /**
     * 문자열로부터 GoalId 생성
     *
     * @param value GoalId 문자열
     * @return GoalId
     */
    public static GoalId from(String value) {
        return new GoalId(value);
    }
}