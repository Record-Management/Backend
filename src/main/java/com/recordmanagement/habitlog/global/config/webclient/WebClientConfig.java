package com.recordmanagement.habitlog.global.config.webclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClientConfig - WebClient 설정 클래스
 *
 * WebClient 인스턴스를 빈으로 등록하여 애플리케이션 전역에서 재사용 가능하도록 구성한다.
 * 기본적으로 메모리 내 버퍼 크기를 조절하여 대용량 응답 처리에 대비한다.
 *
 * 주요 기능:
 * - WebClient.Builder를 사용해 커스터마이징된 WebClient 생성
 * - 최대 메모리 버퍼 크기 16MB로 설정 (기본 256KB 대비 증가)
 * - 필요시 기본 헤더나 필터 등 추가 설정 가능
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Configuration
public class WebClientConfig {

    /**
     * WebClient 빈 생성 메서드
     *
     * @return 커스터마이징된 WebClient 인스턴스
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                // 대용량 응답 대비 최대 메모리 버퍼 사이즈 16MB로 설정
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                // 기본 설정 추가 가능 (예: 기본 헤더, 필터 등)
                .build();
    }

    /**
     * RestTemplate 빈 생성 메서드
     * 
     * Apple 로그인 등 HTTP 요청에 사용됩니다.
     *
     * @return RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
