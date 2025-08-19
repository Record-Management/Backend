package com.recordmanagement.habitlog.application.user.dto;

import com.recordmanagement.habitlog.domain.record.model.RecordType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 온보딩 완료 요청 커맨드
 * 
 * - 사용자가 온보딩 과정을 완료했을 때 전달되는 데이터
 * - 메인 기록 타입을 포함하여 전달
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "온보딩 완료 요청 커맨드")
public class OnboardingCompletionCommand {
    
    @Schema(description = "사용자 ID", example = "user-1234-uuid")
    private final String userId;
    
    @Schema(description = "메인 기록 타입")
    private final RecordType mainRecordType;
}