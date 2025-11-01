package com.recordmanagement.habitlog.domain.user.application.dto;

import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;

/**
 * 목표 재설정 커맨드
 * 
 * @param userId 사용자 ID
 * @param mainRecordType 메인 기록 타입
 * @param goalDays 목표 일수
 * 
 * @author 전우선
 * @since 2025.11.01
 * @version 1.0.0
 */
public record GoalResetCommand(
    String userId,
    RecordType mainRecordType,
    Integer goalDays
) {}