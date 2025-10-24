package com.recordmanagement.habitlog.global.config.swagger;

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
 * OpenApiConfig - Swagger UI 및 OpenAPI 문서화 설정 클래스
 *
 * SpringDoc(OpenAPI 3)를 활용하여 API 문서 자동 생성 및 커스터마이징을 담당합니다.
 *
 * 주요 기능:
 * - API 기본 정보(제목, 설명, 버전, 연락처) 설정
 * - JWT 인증 스키마 정의
 * - 서버 환경별 URL 설정
 * - Swagger UI에서 명확한 API 명세 제공
 *
 * 사용법:
 * - 해당 빈을 통해 OpenAPI 스펙을 구성하며,
 *   스프링 부트 실행 시 Swagger UI(http://localhost:포트/swagger-ui.html)에서 확인 가능
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
                        
                        ### 🔐 인증 방식 
                        - JWT 기반 Bearer 토큰 인증
                        - 액세스 토큰 (1시간) + 리프레시 토큰 (30일) 구조
                        - 소셜 로그인 후 자체 JWT 토큰 발급
                        
                        ### 📋 API 사용 가이드
                        1. **소셜 로그인** → JWT 토큰 획득
                        2. **인증이 필요한 API** → Authorization 헤더에 Bearer 토큰 포함
                        3. **토큰 만료** → 리프레시 토큰으로 액세스 토큰 갱신
                        
                        ### 🔗 관련 링크
                        - **프론트엔드 iOS** 📱: [HabitLog iOS App](https://github.com/Record-Management/IOS)
                        - **프론트엔드 Android** 🤖: [HabitLog Android App](https://github.com/Record-Management/Android)
                        - **문의사항**: 💬Discord - jws0602
                        """)
                .version("v1.2.0")
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