package com.recordmanagement.habitlog.domain.notification.application;

import java.util.Map;

/**
 * 알림 발송자 추상화 인터페이스
 * 
 * DIP 적용: Application 레이어에서 정의한 추상화
 * - 구체적인 알림 발송 기술(FCM, APNS 등)에 독립적
 * - Infrastructure 레이어의 구현체로 교체 가능
 * - 테스트 용이성 향상
 * 
 * 책임:
 * - 알림 발송에 대한 일반적인 인터페이스 제공
 * - 다양한 알림 플랫폼으로 확장 가능한 구조
 * - Application 비즈니스 로직과 Infrastructure 분리
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0 (DIP 적용)
 */
public interface NotificationSender {

    /**
     * 단일 사용자에게 알림 발송
     * 
     * @param token 사용자의 알림 토큰 (FCM 토큰 등)
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (nullable)
     * @return 발송 성공 여부
     */
    boolean sendNotification(String token, String title, String body, Map<String, String> data);

    /**
     * 여러 사용자에게 알림 일괄 발송
     * 
     * @param tokens 사용자들의 알림 토큰 목록
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (nullable)
     * @return 발송 성공한 토큰 개수
     */
    int sendBulkNotification(String[] tokens, String title, String body, Map<String, String> data);

    /**
     * 특정 토픽에 알림 발송
     * 
     * @param topic 토픽 이름
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (nullable)
     * @return 발송 성공 여부
     */
    boolean sendTopicNotification(String topic, String title, String body, Map<String, String> data);
}