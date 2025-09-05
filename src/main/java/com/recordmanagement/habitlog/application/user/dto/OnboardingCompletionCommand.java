package com.recordmanagement.habitlog.application.user.dto;

import com.recordmanagement.habitlog.domain.user.model.RecordType;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 온보딩 완료 요청 커맨드
 * 
 * - 모든 온보딩 데이터를 한번에 처리
 */
@Schema(description = "온보딩 완료 요청 커맨드")
public record OnboardingCompletionCommand(
    @Schema(description = "사용자 ID", example = "user-1234-uuid")
    String userId,
    
    @Schema(description = "사용자 닉네임", example = "홍길동")
    String nickname,
    
    @Schema(description = "메인 기록 타입")
    RecordType mainRecordType,
    
    @Schema(description = "생년월일", example = "1998-06-02")
    LocalDate birthDate,
    
    @Schema(description = "목표 일수", example = "20")
    Integer goalDays,
    
    @Schema(description = "알림 허용 여부", example = "true")
    Boolean notificationEnabled
) {}