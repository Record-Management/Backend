package com.recordmanagement.habitlog.domain.notification.application.util;

import com.recordmanagement.habitlog.domain.user.domain.model.RecordType;

/**
 * 알림 이미지 URL 생성 유틸리티
 *
 * 각 기록 타입별로 적절한 아이콘 이미지 URL을 제공합니다.
 * 프론트엔드에서 커스텀 아이콘 표시 후, 추후 앱 로고로 변경 시 imageUrl 필드만 제거하면 됩니다.
 *
 * @author 전우선
 * @since 2025.11.13
 * @version 1.0.0
 */
public class NotificationImageUtil {
    
    private static final String BASE_URL = "/images/notifications/";
    
    /**
     * 기록 타입에 따른 알림 아이콘 URL 반환
     *
     * @param recordType 기록 타입
     * @return 이미지 URL
     */
    public static String getImageUrl(RecordType recordType) {
        return switch (recordType) {
            case DAILY -> BASE_URL + "daily-record-icon.png";
            case EXERCISE -> BASE_URL + "exercise-record-icon.png";
            case HABIT -> BASE_URL + "habit-record-icon.png";
            default -> BASE_URL + "default-icon.png";
        };
    }
    
    /**
     * 목표 설정 알림 이미지 URL 반환
     *
     * @return 목표 설정 이미지 URL
     */
    public static String getGoalSettingImageUrl() {
        return BASE_URL + "goal-setting-icon.png";
    }
}