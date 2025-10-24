package com.recordmanagement.habitlog.domain.auth.domain.service;

import java.util.Map;

/**
 * 외부 API 클라이언트 추상화 인터페이스
 * 
 * DIP 적용: 도메인 서비스가 인프라스트럭처 기술에 의존하지 않도록 추상화
 * - 도메인 레이어에서 정의하여 의존성 역전
 * - 구체적인 HTTP 클라이언트 기술(RestTemplate, WebClient 등)에 독립적
 * - 테스트 용이성 향상
 * 
 * 책임:
 * - 외부 API 호출에 대한 일반적인 인터페이스 제공
 * - HTTP 메서드별 추상화된 메서드 제공
 * - 다양한 구현체로 교체 가능한 구조
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0 (DIP 적용)
 */
public interface ExternalApiClient {

    /**
     * POST 요청 수행
     * 
     * @param url 요청 URL
     * @param headers 요청 헤더 (nullable)
     * @param requestBody 요청 본문 (nullable)
     * @param responseType 응답 타입 클래스
     * @param <T> 응답 타입
     * @return 응답 객체
     * @throws ExternalApiException API 호출 실패 시
     */
    <T> T post(String url, Map<String, String> headers, Object requestBody, Class<T> responseType);

    /**
     * GET 요청 수행
     * 
     * @param url 요청 URL
     * @param headers 요청 헤더 (nullable)
     * @param responseType 응답 타입 클래스
     * @param <T> 응답 타입
     * @return 응답 객체
     * @throws ExternalApiException API 호출 실패 시
     */
    <T> T get(String url, Map<String, String> headers, Class<T> responseType);

    /**
     * DELETE 요청 수행
     * 
     * @param url 요청 URL
     * @param headers 요청 헤더 (nullable)
     * @throws ExternalApiException API 호출 실패 시
     */
    void delete(String url, Map<String, String> headers);
}