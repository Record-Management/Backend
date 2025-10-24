package com.recordmanagement.habitlog.domain.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Apple Transfer Sub 처리 커맨드
 * 
 * @param oldSub 기존 Apple 사용자 sub
 * @param newSub 새로운 Apple 사용자 sub  
 * @param transferSub Apple에서 제공하는 전환용 임시 sub
 * 
 * @author 전우선
 * @since 2025.09.13
 * @version 1.0.0
 */
@Schema(description = "Apple Transfer Sub 처리 커맨드")
public record AppleTransferSubCommand(
        @Schema(description = "기존 Apple 사용자 sub") 
        String oldSub,
        
        @Schema(description = "새로운 Apple 사용자 sub")
        String newSub,
        
        @Schema(description = "Apple에서 제공하는 전환용 임시 sub")
        String transferSub
) {
}