package com.recordmanagement.habitlog.api.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원탈퇴 요청 DTO
 * 
 * @author 전우선
 * @since 2025.08.01
 * @version 1.0.0
 */
@Getter
@NoArgsConstructor
@Schema(description = "회원탈퇴 요청")
public class UserWithdrawalRequest {

    @Schema(
        description = "탈퇴 사유",
        example = "더 이상 서비스를 이용하지 않음",
        maxLength = 500
    )
    @Size(max = 500, message = "탈퇴 사유는 500자 이하로 입력해주세요")
    private String reason;
}
