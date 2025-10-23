package com.recordmanagement.habitlog.infrastructure.notification;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 푸시 알림 서비스
 * 
 * Firebase Cloud Messaging을 사용하여 푸시 알림을 발송합니다.
 * 
 * TODO: Firebase 설정 완료 후 @Service 주석 해제
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private final FirebaseMessaging firebaseMessaging;

    /**
     * 단일 사용자에게 푸시 알림을 발송합니다.
     * 
     * @param fcmToken 대상 사용자의 FCM 토큰
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (선택사항)
     * @return 발송 성공 여부
     */
    public boolean sendNotification(String fcmToken, String title, String body, Map<String, String> data) {
        if (fcmToken == null || fcmToken.trim().isEmpty()) {
            log.warn("FCM 토큰이 비어있어 알림을 발송할 수 없습니다.");
            return false;
        }

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setAlert(ApsAlert.builder()
                                            .setTitle(title)
                                            .setBody(body)
                                            .build())
                                    .setSound("default")
                                    .build())
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            String response = firebaseMessaging.send(messageBuilder.build());
            log.info("푸시 알림 발송 성공 - FCM 토큰: {}, 응답: {}", maskToken(fcmToken), response);
            return true;

        } catch (FirebaseMessagingException e) {
            log.error("푸시 알림 발송 실패 - FCM 토큰: {}, 에러: {}", maskToken(fcmToken), e.getMessage());
            return false;
        }
    }

    /**
     * 여러 사용자에게 동시에 푸시 알림을 발송합니다.
     * 
     * @param fcmTokens 대상 사용자들의 FCM 토큰 리스트
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (선택사항)
     * @return 발송 결과 (성공 개수, 실패 개수)
     */
    public BatchNotificationResult sendMulticastNotification(List<String> fcmTokens, String title, String body, Map<String, String> data) {
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            log.warn("FCM 토큰 리스트가 비어있어 알림을 발송할 수 없습니다.");
            return new BatchNotificationResult(0, 0);
        }

        // 빈 토큰 제거
        List<String> validTokens = fcmTokens.stream()
                .filter(token -> token != null && !token.trim().isEmpty())
                .toList();

        if (validTokens.isEmpty()) {
            log.warn("유효한 FCM 토큰이 없어 알림을 발송할 수 없습니다.");
            return new BatchNotificationResult(0, fcmTokens.size());
        }

        try {
            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                    .addAllTokens(validTokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setAlert(ApsAlert.builder()
                                            .setTitle(title)
                                            .setBody(body)
                                            .build())
                                    .setSound("default")
                                    .build())
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            BatchResponse response = firebaseMessaging.sendMulticast(messageBuilder.build());
            int successCount = response.getSuccessCount();
            int failureCount = response.getFailureCount();

            log.info("배치 푸시 알림 발송 완료 - 총 {}개 중 성공: {}개, 실패: {}개", 
                    validTokens.size(), successCount, failureCount);

            if (failureCount > 0) {
                response.getResponses().forEach(sendResponse -> {
                    if (!sendResponse.isSuccessful()) {
                        log.warn("개별 알림 발송 실패: {}", sendResponse.getException().getMessage());
                    }
                });
            }

            return new BatchNotificationResult(successCount, failureCount);

        } catch (FirebaseMessagingException e) {
            log.error("배치 푸시 알림 발송 실패 - 토큰 개수: {}, 에러: {}", validTokens.size(), e.getMessage());
            return new BatchNotificationResult(0, validTokens.size());
        }
    }

    /**
     * FCM 토큰을 마스킹합니다. (로깅용)
     * 
     * @param token FCM 토큰
     * @return 마스킹된 토큰
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 10) {
            return "****";
        }
        return token.substring(0, 5) + "****" + token.substring(token.length() - 5);
    }

    /**
     * 배치 알림 발송 결과
     */
    public record BatchNotificationResult(int successCount, int failureCount) {}
}