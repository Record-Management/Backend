package com.recordmanagement.habitlog.global.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 데이터가 없는 간단한 성공 응답 전용 클래스
 * 예: 좋아요 요청, 삭제 완료 등에서 사용
 * 
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
@Schema(description = "데이터 없는 성공 응답")
public class SuccessResponse {

    @Schema(description = "요청 처리 성공 여부", example = "true")
    private final boolean success = true;

    @Schema(description = "응답 코드", example = "S200")
    private final String code = "S200";

    @Schema(description = "응답 메시지", example = "요청이 정상적으로 처리되었습니다.")
    private final String message = "요청이 정상적으로 처리되었습니다.";
}
