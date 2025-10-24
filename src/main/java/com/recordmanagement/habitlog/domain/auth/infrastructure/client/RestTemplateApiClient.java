package com.recordmanagement.habitlog.domain.auth.infrastructure.client;

import com.recordmanagement.habitlog.domain.auth.domain.service.ExternalApiClient;
import com.recordmanagement.habitlog.domain.auth.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * RestTemplate 기반 외부 API 클라이언트 구현체
 * 
 * DIP 적용: Infrastructure 레이어에서 도메인 추상화 구현
 * - RestTemplate의 구체적인 기술을 도메인 인터페이스에 맞게 구현
 * - 예외 변환을 통해 도메인 예외로 추상화
 * - 다른 HTTP 클라이언트로 쉽게 교체 가능
 * 
 * @author 전우선
 * @since 2025.10.24
 * @version 1.0.0 (DIP 적용)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RestTemplateApiClient implements ExternalApiClient {

    private final RestTemplate restTemplate;

    @Override
    public <T> T post(String url, Map<String, String> headers, Object requestBody, Class<T> responseType) {
        try {
            log.debug("POST 요청 시작: url={}", url);
            
            HttpHeaders httpHeaders = createHeaders(headers);
            HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, httpHeaders);
            
            ResponseEntity<T> response = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity, responseType);
            
            log.debug("POST 요청 완료: url={}, status={}", url, response.getStatusCode());
            return response.getBody();
            
        } catch (RestClientException e) {
            log.error("POST 요청 실패: url={}, error={}", url, e.getMessage());
            throw ExternalApiException.callFailed();
        } catch (Exception e) {
            log.error("POST 요청 중 예상치 못한 오류: url={}, error={}", url, e.getMessage());
            throw ExternalApiException.callFailed();
        }
    }

    @Override
    public <T> T get(String url, Map<String, String> headers, Class<T> responseType) {
        try {
            log.debug("GET 요청 시작: url={}", url);
            
            HttpHeaders httpHeaders = createHeaders(headers);
            HttpEntity<Void> requestEntity = new HttpEntity<>(httpHeaders);
            
            ResponseEntity<T> response = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, responseType);
            
            log.debug("GET 요청 완료: url={}, status={}", url, response.getStatusCode());
            return response.getBody();
            
        } catch (RestClientException e) {
            log.error("GET 요청 실패: url={}, error={}", url, e.getMessage());
            throw ExternalApiException.callFailed();
        } catch (Exception e) {
            log.error("GET 요청 중 예상치 못한 오류: url={}, error={}", url, e.getMessage());
            throw ExternalApiException.callFailed();
        }
    }

    @Override
    public void delete(String url, Map<String, String> headers) {
        try {
            log.debug("DELETE 요청 시작: url={}", url);
            
            HttpHeaders httpHeaders = createHeaders(headers);
            HttpEntity<Void> requestEntity = new HttpEntity<>(httpHeaders);
            
            restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Void.class);
            
            log.debug("DELETE 요청 완료: url={}", url);
            
        } catch (RestClientException e) {
            log.error("DELETE 요청 실패: url={}, error={}", url, e.getMessage());
            throw ExternalApiException.callFailed();
        } catch (Exception e) {
            log.error("DELETE 요청 중 예상치 못한 오류: url={}, error={}", url, e.getMessage());
            throw ExternalApiException.callFailed();
        }
    }

    /**
     * Map을 HttpHeaders로 변환
     * 
     * @param headers 헤더 맵
     * @return HttpHeaders 객체
     */
    private HttpHeaders createHeaders(Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }
        
        return httpHeaders;
    }
}