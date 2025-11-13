package com.recordmanagement.habitlog.global.config.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenApiConfig - Redoc 및 OpenAPI 문서화 설정 클래스
 *
 * SpringDoc(OpenAPI 3)를 활용하여 API 문서 자동 생성 및 커스터마이징을 담당합니다.
 *
 * 주요 기능:
 * - API 기본 정보(제목, 설명, 버전, 연락처) 설정
 * - JWT 인증 스키마 정의
 * - 서버 환경별 URL 설정
 * - Redoc에서 명확한 API 명세 제공
 *
 * 사용법:
 * - 해당 빈을 통해 OpenAPI 스펙을 구성하며,
 *   스프링 부트 실행 시 Redoc 문서에서 확인 가능
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * OpenAPI 기본 정보 빈 생성
     *
     * @return OpenAPI 인스턴스
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .openapi("3.0.1")
                .info(apiInfo())
                .servers(serverList())
                .components(securityComponents())
                .addSecurityItem(securityRequirement());
    }

    /**
     * API 기본 정보 설정
     */
    private Info apiInfo() {
        return new Info()
                .title("HabitLog API")
                .description("""
                        ## 🚀 HabitLog 백엔드 API 명세서 🚀
                        
                        습관 기록 및 관리를 위한 모바일 앱의 백엔드 API입니다.
                        
                        ### 🆕 v1.4.1 업데이트 (2025.11.13)
                        - ⏰ **자동 목표 완료**: 매일 자정 스케줄러가 만료된 목표를 자동 완료 처리
                        - 🔄 **User-Goal 동기화**: 목표 완료 시 사용자 정보 자동 동기화 (mainRecordType, goalDays → null)
                        - 📅 **기간 기반 목표**: 사용자가 기록 안 써도 목표 기간은 달력에 따라 진행
                        
                        ### 🆕 v1.4.0 업데이트 (2025.11.04)
                        - 🎯 **목표 달성 시스템**: 4단계 나무 성장과 함께하는 목표 관리
                        - 📊 **실시간 진행률 추적**: 기록 생성 시 자동 목표 진행률 업데이트
                        - 🌳 **나무 성장 단계**: 완료일수에 따른 4단계 시각적 피드백
                        - 📈 **달성 보고서**: 현재 목표와 누적 달성 이력 조회
                        - 🔄 **목표 생성/포기**: 진행중인 목표가 없을 때만 새 목표 생성 가능
                        
                        ### 📋 v1.3.0 업데이트 (2025.11.01)
                        - 🎯 **습관 목표 기간 시스템 개선**: 온보딩 시점부터 목표 달성까지 체계적 관리
                        - 📅 **캘린더 습관 표시**: 사용자 실제 행동 시에만 캘린더 표시 (일상/운동과 동일한 UX)
                        - 🔄 **목표 재설정 API**: 간편한 습관 목표 변경 기능
                        - 📊 **습관 기간 정보 API**: 현재 습관 진행 상황 및 기간 정보 조회
                        - 🔔 **자동 알림 시스템**: 매일 오후 7시 스마트 알림 자동 발송
                        
                        ### 🔐 인증 방식 
                        - JWT 기반 Bearer 토큰 인증
                        - 액세스 토큰 (1시간) + 리프레시 토큰 (30일) 구조
                        - 소셜 로그인 후 자체 JWT 토큰 발급
                        
                        ### 📋 API 사용 가이드
                        1. **소셜 로그인** → JWT 토큰 획득
                        2. **온보딩 완료** → 자동 목표 생성 (기록 타입, 목표일수)
                        3. **기록 생성** → 자동 목표 진행률 업데이트
                        4. **목표 조회** → 현재 진행률, 나무 단계, 달성 이력 확인
                        5. **목표 기간 만료** → 매일 자정 스케줄러가 자동 완료 처리
                        6. **User 정보 동기화** → 목표 완료 시 사용자 정보 자동 초기화
                        7. **목표 완료/포기** → 새로운 목표 생성 가능
                        8. **FCM 토큰 등록** → 푸시 알림 수신을 위한 토큰 등록
                        9. **알림 설정** → 개인화된 알림 수신 설정 관리
                        
                        ### 🔗 관련 링크
                        - **프론트엔드 iOS** 📱: [HabitLog iOS App](https://github.com/Record-Management/IOS)
                        - **프론트엔드 Android** 🤖: [HabitLog Android App](https://github.com/Record-Management/Android)
                        - **문의사항**: 💬Discord - jws0602
                        """)
                .version("v1.4.1")
                .contact(new Contact()
                        .name("전우선 (Jeon Woo Seon)")
                        .email("wooxexn@gmail.com")
                        .url("https://github.com/wooxexn")
                )
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")
                );
    }

    /**
     * 서버 환경별 URL 설정
     */
    private List<Server> serverList() {
        return List.of(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("로컬 개발 서버"),
                new Server()
                        .url("http://54.180.82.163:8082")
                        .description("개발 서버 (EC2)")
        );
    }

    /**
     * JWT 보안 스키마 정의
     */
    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("""
                                ### JWT Bearer 토큰 인증
                                
                                #### 사용 방법
                                1. 소셜 로그인 API 호출하여 액세스 토큰 획득
                                2. Authorization 헤더에 'Bearer {accessToken}' 형태로 포함
                                3. 토큰 만료 시 리프레시 토큰으로 갱신
                                
                                #### 토큰 형식
                                ```
                                Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                                ```
                                
                                #### 주의사항
                                - 토큰 앞에 'Bearer ' 접두사 필수
                                - 액세스 토큰 유효시간: 1시간
                                - 토큰 탈취 방지를 위한 HTTPS 사용 필수
                                """)
                );
    }

    /**
     * 보안 요구사항 설정
     */
    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("bearerAuth");
    }
}