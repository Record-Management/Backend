package com.recordmanagement.habitlog.domain.user.application.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원탈퇴 커맨드
 * 
 * @author 전우선
 * @since 2025.08.01
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public class UserWithdrawalCommand {
    
    private final String userId;
    private final String reason; // 탈퇴 사유 (선택)
}
