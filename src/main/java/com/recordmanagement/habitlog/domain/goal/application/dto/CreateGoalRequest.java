package com.recordmanagement.habitlog.domain.goal.application.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import lombok.Getter;

/**
 * 목표 생성 요청 DTO
 *
 * @author 전우선
 * @since 2025.11.04
 * @version 1.0.2
 */
@Getter
public class CreateGoalRequest {

    private final RecordType recordType;
    private final int goalDays;

    @JsonCreator
    public CreateGoalRequest(
            @JsonProperty("recordType") RecordType recordType,
            @JsonProperty("goalDays") int goalDays
    ) {
        this.recordType = recordType;
        this.goalDays = goalDays;
    }

    /**
     * 요청 데이터 유효성 검증
     */
    public void validate() {
        if (recordType == null) {
            throw new IllegalArgumentException("기록 타입은 필수입니다.");
        }

        if (goalDays != 10 && goalDays != 20 && goalDays != 30) {
            throw new IllegalArgumentException("목표일수는 10, 20, 30일만 가능합니다.");
        }
    }
}