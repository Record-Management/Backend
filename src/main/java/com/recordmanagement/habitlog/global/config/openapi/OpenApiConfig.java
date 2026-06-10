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
                        ## HabitLog 백엔드 API 명세서

                        습관 기록 및 관리를 위한 모바일 앱의 백엔드 API입니다.

                        ### v1.5.1 업데이트 (2026.06.03)
                        - **캘린더 일정 표시 개선**: ScheduleSummary 필드 변경으로 클라이언트 사용성 향상
                          * 변경 전: `size` (전체 일정 개수) - 클라이언트에서 size - 1 계산 필요
                          * 변경 후: `extraScheduleCount` (추가 일정 개수) - 서버에서 계산하여 직접 사용 가능
                          * 일정 1개: extraScheduleCount = null
                          * 일정 2개: extraScheduleCount = 1 ("+1" 표시)
                          * 일정 3개: extraScheduleCount = 2 ("+2" 표시)
                        - **반복 일정 기능 개선**: 반복 타입(DAY/WEEK/MONTH/YEAR) 일정이 정확하게 표시됩니다
                          * 캘린더 조회: 반복 타입별 정확한 날짜 계산 (매주 → 같은 요일만, 매월 → 같은 날짜만)
                          * 일일 기록 조회: 반복 일정이 해당 날짜에 정확히 표시됨
                          * 알림 발송: 반복 일정이 반복될 때마다 알림 정상 발송
                          * repeatEndsOn(반복 종료일) 정확히 적용

                        ### v1.5.0 업데이트 (2026.05.16)
                        - **일정 기록 기능**: 일정 CRUD 기능 추가 (제목, 기간, 알림, 반복, 색상)
                        - **일정 알림 시스템**: CUSTOM 알림 타입에 분(minutes) 단위 설정 추가 (0-59)
                        - **캘린더 API 개선**: 일정 정보를 schedules 필드로 분리 (ScheduleSummary: title, extraScheduleCount, color)
                        - **일일 기록 API 개선**: 일정 정보를 schedules 배열로 분리 (ScheduleDetail: 상세 정보)
                        - **기록 제한 정책 변경**: 타입별 2개 제한 → 전체 합산 2개 제한으로 통합
                          * 변경 전: DAILY 2개, EXERCISE 2개, HABIT 2개 (각각)
                          * 변경 후: DAILY + EXERCISE + HABIT 합쳐서 2개 (전체)
                          * 유지: 하루 최대 2가지 타입 제한
                        - **일정 생성 제한**: 오늘 생성할 수 있는 일정은 최대 2개 (createdAt 기준)
                          * 일정의 startDate와 무관하게 오늘 생성한 일정 개수로 제한
                        - **생성 제한 조회 API**: 기록/일정 생성 가능 여부 확인 API 추가
                          * GET /api/daily-records/creation-limits?date={date}
                          * 응답: {canCreateRecord: boolean, canCreateSchedule: boolean}

                        ### v1.4.4 업데이트 (2025.11.14)
                        - **목표 달성 보고서 정렬 개선**: recentHistory를 종료일 기준으로 정렬
                        - **프론트엔드 UX 향상**: 가장 최근 완료된 목표를 정확히 식별 가능
                        - **API 일관성 개선**: 목표 종료 시점 기준으로 정확한 데이터 제공
                        
                        ### v1.4.1 업데이트 (2025.11.13)
                        - **자동 목표 완료**: 매일 자정 스케줄러가 만료된 목표를 자동 완료 처리
                        - **User-Goal 동기화**: 목표 완료 시 사용자 정보 자동 동기화 (mainRecordType, goalDays → null)
                        - **기간 기반 목표**: 사용자가 기록 안 써도 목표 기간은 달력에 따라 진행
                        
                        ### v1.4.0 업데이트 (2025.11.04)
                        - **목표 달성 시스템**: 4단계 나무 성장과 함께하는 목표 관리
                        - **실시간 진행률 추적**: 기록 생성 시 자동 목표 진행률 업데이트
                        - **나무 성장 단계**: 완료일수에 따른 4단계 시각적 피드백
                        - **달성 보고서**: 현재 목표와 누적 달성 이력 조회
                        - **목표 생성/포기**: 진행중인 목표가 없을 때만 새 목표 생성 가능
                        
                        ### v1.3.0 업데이트 (2025.11.01)
                        - **습관 목표 기간 시스템 개선**: 온보딩 시점부터 목표 달성까지 체계적 관리
                        - **캘린더 습관 표시**: 사용자 실제 행동 시에만 캘린더 표시 (일상/운동과 동일한 UX)
                        - **목표 재설정 API**: 간편한 습관 목표 변경 기능
                        - **습관 기간 정보 API**: 현재 습관 진행 상황 및 기간 정보 조회
                        - **자동 알림 시스템**: 매일 오후 7시 스마트 알림 자동 발송
                        
                        ### 인증 방식 
                        - JWT 기반 Bearer 토큰 인증
                        - 액세스 토큰 (1시간) + 리프레시 토큰 (30일) 구조
                        - 소셜 로그인 후 자체 JWT 토큰 발급
                        
                        ### API 사용 가이드
                        1. **소셜 로그인** → JWT 토큰 획득
                        2. **온보딩 완료** → 자동 목표 생성 (기록 타입, 목표일수)
                        3. **기록 생성** → 자동 목표 진행률 업데이트
                        4. **목표 조회** → 현재 진행률, 나무 단계, 달성 이력 확인
                        5. **목표 기간 만료** → 매일 자정 스케줄러가 자동 완료 처리
                        6. **User 정보 동기화** → 목표 완료 시 사용자 정보 자동 초기화
                        7. **목표 완료/포기** → 새로운 목표 생성 가능
                        8. **FCM 토큰 등록** → 푸시 알림 수신을 위한 토큰 등록
                        9. **알림 설정** → 개인화된 알림 수신 설정 관리
                        
                        ### 관련 링크
                        - **프론트엔드 iOS**: [HabitLog iOS App](https://github.com/Record-Management/IOS)
                        - **프론트엔드 Android**: [HabitLog Android App](https://github.com/Record-Management/Android)
                        - **문의사항**: Discord - jws0602
                        """)
                .version("v1.5.0")
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
                        .url("http://54.180.106.131:8082")
                        .description("Production 서버 (EC2)"),
                new Server()
                        .url("http://54.180.106.131:8083")
                        .description("QA 서버 (EC2)")
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