package com.recordmanagement.habitlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habitlog 백엔드 애플리케이션 메인 클래스
 * 
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class HabitlogBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HabitlogBackendApplication.class, args);
    }

}
