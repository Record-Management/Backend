package com.recordmanagement.habitlog.domain.goal.application.dto;

import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

/**
 * 목표 생성 요청 DTO
 *
 * @author 전우선
 * @since 2025.11.04
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public class CreateGoalRequest {

    private final RecordType recordType;
    private final int goalDays;
    private final LocalDate startDate;

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

        if (startDate == null) {
            throw new IllegalArgumentException("시작일은 필수입니다.");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("시작일은 오늘 이후여야 합니다.");
        }
    }
}