package com.recordmanagement.habitlog.config.security;

import com.recordmanagement.habitlog.config.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 보안 설정 클래스
 *
 * 주요 목적:
 * - JWT 기반 인증 시스템 구성
 * - API 엔드포인트별 접근 권한 제어
 * - 세션리스(Stateless) 보안 정책 적용
 *
 * 주요 설정:
 * - CSRF 보호 비활성화 (REST API 특성상 불필요)
 * - 세션 생성 정책: STATELESS (JWT 사용)
 * - 커스텀 JWT 인증 필터 등록
 * - URL별 접근 권한 세밀 제어
 *
 * 보안 정책:
 * - 공개 API: 인증 없이 접근 가능 (/api/auth/**, /swagger-ui/** 등)
 * - 보호된 API: JWT 토큰 인증 필수 (나머지 모든 /api/** 경로)
 * - API 문서: 개발/테스트 환경에서 접근 허용
 *
 * 필터 체인 순서:
 * 1. JwtAuthenticationFilter (JWT 토큰 검증)
 * 2. UsernamePasswordAuthenticationFilter (기본 인증 필터)
 * 3. 기타 Spring Security 기본 필터들
 *
 * 인증 플로우:
 * 1. 요청 URL이 공개 경로인지 확인
 * 2. 보호된 경로면 JWT 필터에서 토큰 검증
 * 3. 유효한 토큰이면 SecurityContext에 인증 정보 설정
 * 4. 컨트롤러로 요청 전달
 *
 * URL 접근 권한 매트릭스:
 * - /swagger-ui/** : 모든 사용자 (API 문서 UI)
 * - /v3/api-docs/** : 모든 사용자 (OpenAPI 스펙)
 * - /api/auth/** : 모든 사용자 (로그인, 회원가입, 토큰 갱신)
 * - /api/files/** : 인증된 사용자 (파일 업로드 - 보안상 인증 필요)
 * - /api/** : 인증된 사용자 (보호된 비즈니스 API)
 *
 * 비활성화된 기능:
 * - CSRF: SPA/모바일 앱에서 불필요
 * - 세션: JWT 토큰 기반 인증 사용
 * - 폼 로그인: 소셜 로그인만 지원
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    /** JWT 토큰 인증 필터 (커스텀 구현) */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Spring Security 필터 체인 설정
     *
     * 설정 체인:
     * 1. CSRF 비활성화
     * 2. 세션 정책 설정
     * 3. URL 권한 설정
     * 4. 필터 등록
     *
     * @param http Spring Security HTTP 보안 설정 빌더
     * @return 구성된 SecurityFilterChain 객체
     * @throws Exception 설정 과정에서 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 비활성화: REST API는 상태 저장 안 하므로 필요 없음
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 관리 정책: JWT 사용으로 세션 상태를 서버에 저장하지 않음
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/webjars/**"
                        ).permitAll() // API 문서 관련 경로는 모두 허용

                        .requestMatchers("/api/auth/**").permitAll() // 인증 관련 API 공개

                        .requestMatchers("/api/files/**").authenticated() // 파일 업로드 API는 인증 필요

                        .anyRequest().authenticated() // 그 외 모든 API는 인증 필요
                )

                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 등록
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
