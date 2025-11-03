package com.recordmanagement.habitlog.domain.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Apple Transfer Sub 요청 DTO
 * 
 * Apple에서 사용자 실명 정보 변경 시 새로운 sub를 제공하는 경우 사용
 * 기존 사용자와 새 sub를 연결하기 위한 요청
 * 
 * @author 전우선
 * @since 2025.09.13
 * @version 1.0.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Apple Transfer Sub 요청 데이터",
    example = """
        {
          "oldSub": "000123.abc123def456ghi789.1234",
          "newSub": "000123.xyz789abc012def345.5678",
          "transferSub": "T_000123.transfer123.9999"
        }
        """
)
public class AppleTransferSubRequest {
    
    @Schema(
        description = "기존 Apple 사용자 sub",
        example = "000123.abc123def456ghi789.1234",
        required = true
    )
    @NotBlank(message = "기존 sub는 필수입니다")
    private String oldSub;
    
    @Schema(
        description = "새로운 Apple 사용자 sub",
        example = "000123.xyz789abc012def345.5678", 
        required = true
    )
    @NotBlank(message = "새 sub는 필수입니다")
    private String newSub;
    
    @Schema(
        description = "Apple에서 제공하는 전환용 임시 sub",
        example = "T_000123.transfer123.9999",
        required = true
    )
    @NotBlank(message = "전환 sub는 필수입니다")
    private String transferSub;
}