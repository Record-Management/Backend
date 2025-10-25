package com.recordmanagement.habitlog.domain.notification.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.recordmanagement.habitlog.global.common.response.PagingResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * 알림 히스토리 조회 응답 (읽음 상태 포함)
 * 
 * 프론트엔드 요구사항:
 * - 알림 히스토리 목록
 * - 최근 확인 시간 (읽음/안읽음 판단용)
 *
 * @author 전우선
 * @since 2025.10.25
 * @version 1.0.0
 */
@Value
@Builder
@Schema(description = "알림 히스토리 조회 응답 (읽음 상태 포함)")
public class NotificationHistoryWithStatusResponse {

    @Schema(description = "알림 히스토리 목록 (페이징)")
    PagingResponse<NotificationHistoryResponse> notifications;

    @Schema(description = "알림 센터 마지막 확인 시간 (null이면 최초 조회)", example = "2025-10-25T16:45:30")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime recentCheckedAt;

    /**
     * 알림 히스토리와 최근 확인 시간으로 응답 생성
     *
     * @param notifications 알림 히스토리 페이징 응답
     * @param recentCheckedAt 최근 확인 시간
     * @return 읽음 상태 포함 응답
     */
    public static NotificationHistoryWithStatusResponse of(
            PagingResponse<NotificationHistoryResponse> notifications,
            LocalDateTime recentCheckedAt) {
        return NotificationHistoryWithStatusResponse.builder()
                .notifications(notifications)
                .recentCheckedAt(recentCheckedAt)
                .build();
    }
}