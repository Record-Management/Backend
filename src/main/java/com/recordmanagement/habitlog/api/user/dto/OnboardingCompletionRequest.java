package com.recordmanagement.habitlog.api.user.dto;

import com.recordmanagement.habitlog.domain.record.model.RecordType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * 온보딩 완료 요청 DTO
 * 
 * - 클라이언트에서 온보딩 완료 시 전송하는 요청 데이터
 * - 메인 기록 타입을 포함
 */
@Getter
@NoArgsConstructor
@Schema(description = "온보딩 완료 요청")
public class OnboardingCompletionRequest {
    
    @NotNull(message = "메인 기록 타입은 필수입니다")
    @Schema(description = "메인 기록 타입", example = "EXERCISE")
    private RecordType mainRecordType;
}