package com.recordmanagement.habitlog.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 페이징 처리된 데이터를 포함하는 공통 응답 포맷
 * 실제 데이터 리스트와 페이지 정보를 함께 제공하여
 * 프론트엔드에서 페이지네이션 UI 구현에 활용
 *
 * @param <T> 목록 데이터 타입
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
@Schema(description = "페이징 응답 포맷")
@AllArgsConstructor
public class PagingResponse<T> {

    @Schema(description = "조회된 데이터 목록")
    private final List<T> items;

    @Schema(description = "페이지 정보")
    private final PageInfo pageInfo;

    /**
     * 페이징 관련 메타 정보 포함 클래스
     * 현재 페이지, 페이지 크기, 전체 데이터 수, 전체 페이지 수 포함
     */
    @Getter
    @AllArgsConstructor
    @Schema(description = "페이지 정보")
    public static class PageInfo {

        @Schema(description = "현재 페이지 번호", example = "1")
        private final int page;

        @Schema(description = "한 페이지당 데이터 수", example = "10")
        private final int size;

        @Schema(description = "전체 데이터 개수", example = "123")
        private final long totalElements;

        @Schema(description = "전체 페이지 수", example = "13")
        private final int totalPages;
    }
}