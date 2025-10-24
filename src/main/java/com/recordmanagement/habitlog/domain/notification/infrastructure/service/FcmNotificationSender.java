package com.recordmanagement.habitlog.domain.notification.infrastructure.service;

import com.google.firebase.messaging.*;
import com.recordmanagement.habitlog.domain.notification.application.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FCM 기반 알림 발송자 구현체
 * 
 * DIP 적용: Infrastructure 레이어에서 Application 추상화 구현
 * - Firebase Cloud Messaging 구체 기술을 추상화에 맞게 구현
 * - 다른 알림 서비스(APNS, SMS 등)로 쉽게 교체 가능
 * - Application 레이어의 비즈니스 로직과 분리
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0 (DIP 적용)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FcmNotificationSender implements NotificationSender {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public boolean sendNotification(String token, String title, String body, Map<String, String> data) {
        try {
            log.debug("FCM 알림 발송 시작: token={}", maskToken(token));
            
            Message.Builder messageBuilder = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());
            
            // 추가 데이터가 있으면 설정
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }
            
            Message message = messageBuilder.build();
            String messageId = firebaseMessaging.send(message);
            
            log.debug("FCM 알림 발송 성공: token={}, messageId={}", maskToken(token), messageId);
            return true;
            
        } catch (FirebaseMessagingException e) {
            log.error("FCM 알림 발송 실패: token={}, error={}", maskToken(token), e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("FCM 알림 발송 중 예상치 못한 오류: token={}, error={}", maskToken(token), e.getMessage());
            return false;
        }
    }

    @Override
    public int sendBulkNotification(String[] tokens, String title, String body, Map<String, String> data) {
        if (tokens == null || tokens.length == 0) {
            log.warn("FCM 일괄 발송: 토큰이 없습니다");
            return 0;
        }
        
        try {
            log.debug("FCM 일괄 알림 발송 시작: 토큰 {}개", tokens.length);
            
            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .addAllTokens(List.of(tokens));
            
            // 추가 데이터가 있으면 설정
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }
            
            MulticastMessage message = messageBuilder.build();
            BatchResponse response = firebaseMessaging.sendMulticast(message);
            
            int successCount = response.getSuccessCount();
            int failureCount = response.getFailureCount();
            
            log.debug("FCM 일괄 알림 발송 완료: 성공 {}개, 실패 {}개", successCount, failureCount);
            
            // 실패한 토큰들 로깅 (디버그용)
            if (failureCount > 0) {
                logFailedTokens(response, tokens);
            }
            
            return successCount;
            
        } catch (FirebaseMessagingException e) {
            log.error("FCM 일괄 알림 발송 실패: error={}", e.getMessage());
            return 0;
        } catch (Exception e) {
            log.error("FCM 일괄 알림 발송 중 예상치 못한 오류: error={}", e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean sendTopicNotification(String topic, String title, String body, Map<String, String> data) {
        try {
            log.debug("FCM 토픽 알림 발송 시작: topic={}", topic);
            
            Message.Builder messageBuilder = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());
            
            // 추가 데이터가 있으면 설정
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }
            
            Message message = messageBuilder.build();
            String messageId = firebaseMessaging.send(message);
            
            log.debug("FCM 토픽 알림 발송 성공: topic={}, messageId={}", topic, messageId);
            return true;
            
        } catch (FirebaseMessagingException e) {
            log.error("FCM 토픽 알림 발송 실패: topic={}, error={}", topic, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("FCM 토픽 알림 발송 중 예상치 못한 오류: topic={}, error={}", topic, e.getMessage());
            return false;
        }
    }

    /**
     * 실패한 토큰들 로깅 (디버그용)
     */
    private void logFailedTokens(BatchResponse response, String[] tokens) {
        List<SendResponse> responses = response.getResponses();
        List<String> failedTokens = new ArrayList<>();
        
        for (int i = 0; i < responses.size(); i++) {
            if (!responses.get(i).isSuccessful()) {
                failedTokens.add(maskToken(tokens[i]));
            }
        }
        
        log.debug("FCM 발송 실패 토큰들: {}", failedTokens);
    }

    /**
     * 토큰 마스킹 (로깅용)
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 10) {
            return "****";
        }
        return token.substring(0, 5) + "****" + token.substring(token.length() - 5);
    }
}