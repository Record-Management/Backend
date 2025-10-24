package com.recordmanagement.habitlog.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase 설정 클래스
 * 
 * Firebase Admin SDK를 초기화하고 FirebaseMessaging 빈을 생성합니다.
 * firebase-service-account.json 파일을 사용하여 FCM 푸시 알림 서비스를 제공합니다.
 * 
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    /**
     * Firebase 앱을 초기화합니다.
     * 
     * firebase-service-account.json 파일을 classpath에서 읽어 초기화합니다.
     * 
     * @return 초기화된 FirebaseApp
     * @throws IOException 서비스 계정 파일을 읽을 수 없는 경우
     */
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            try (InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("Firebase 앱 초기화 완료: {}", app.getName());
                return app;
            }
        } else {
            log.info("Firebase 앱 이미 초기화됨");
            return FirebaseApp.getInstance();
        }
    }

    /**
     * FirebaseMessaging 인스턴스를 생성합니다.
     * 
     * @param firebaseApp 초기화된 Firebase 앱
     * @return FirebaseMessaging 인스턴스
     */
    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        FirebaseMessaging messaging = FirebaseMessaging.getInstance(firebaseApp);
        log.info("FirebaseMessaging 빈 생성 완료");
        return messaging;
    }
}