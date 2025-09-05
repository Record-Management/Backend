package com.recordmanagement.habitlog.application.user.dto;

import com.recordmanagement.habitlog.domain.user.model.User;
import com.recordmanagement.habitlog.domain.user.model.SocialType;
import com.recordmanagement.habitlog.domain.record.model.RecordType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * 사용자 정보를 API 응답용으로 변환하는 DTO
 * 도메인 모델(User)의 내부 구조를 감추고
 * 필요한 정보만 선별하여 클라이언트에 제공
 * 불변 객체로 설계되어 데이터 안정성 보장
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
@Builder
@Schema(
    description = """
        ## 사용자 정보 응답 DTO
        
        로그인 성공 시 또는 사용자 정보 조회 시 반환되는 사용자 기본 정보입니다.
        
        ### 포함 정보
        - 사용자 식별자 및 기본 프로필
        - 소셜 로그인 플랫폼 정보
        - 계정 생성 시간
        
        ### 개인정보 보호
        - 민감한 정보(비밀번호, 내부 ID 등)는 제외
        - 클라이언트에 필요한 최소한의 정보만 포함
        """,
    example = """
        {
          "id": "user_123456",
          "name": "홍길동",
          "email": "hong@example.com", 
          "socialType": "KAKAO",
          "createdAt": "2024-05-01T12:34:56"
        }
        """
)
public class UserResponse {

    @Schema(
        description = """
            사용자 고유 식별자
            
            ### 특징
            - 시스템 내에서 사용자를 유일하게 식별
            - 외부 노출 가능한 안전한 형태의 ID
            - URL이나 API 호출 시 사용자 지정에 활용
            
            ### 형태
            - 일반적으로 'user_' 접두사 + 고유번호
            - 추측하기 어려운 형태로 생성
            """,
        example = "user_123456",
        required = true
    )
    private final String id;

    @Schema(
        description = """
            사용자 이름 또는 닉네임
            
            ### 출처
            - 소셜 로그인 시 플랫폼에서 제공받은 이름
            - 사용자가 직접 설정한 닉네임
            
            ### 활용
            - 환영 메시지나 프로필 표시용
            - 화면 상단 사용자 표시
            - 댓글이나 게시물 작성자 표시
            """,
        example = "홍길동",
        required = true,
        maxLength = 50
    )
    private final String name;

    @Schema(
        description = """
            사용자 이메일 주소
            
            ### 용도
            - 계정 식별 및 연락 수단
            - 중요 알림 발송
            - 비밀번호 재설정 등 보안 기능
            
            ### 개인정보 처리
            - 사용자 동의 하에 수집
            - 필요시에만 노출 (마스킹 처리 고려)
            - 마케팅 목적 사용 시 별도 동의 필요
            """,
        example = "hong@example.com", 
        format = "email",
        nullable = true,
        maxLength = 255
    )
    private final String email;

    @Schema(
        description = """
            소셜 로그인 플랫폼 타입
            
            ### 지원 플랫폼
            - **KAKAO**: 카카오 로그인
            - **APPLE**: 애플 로그인
            - **GOOGLE**: 구글 로그인
            
            ### 활용
            - 사용자별 로그인 방식 표시
            - 플랫폼별 특화 기능 제공
            - 연동 해제 기능 구현 시 참조
            """,
        example = "KAKAO",
        implementation = SocialType.class,
        required = true
    )
    private final SocialType socialType;

    @Schema(
        description = """
            계정 생성 시간
            
            ### 표준
            - ISO 8601 형식 (yyyy-MM-dd'T'HH:mm:ss)
            - 서버 시간대 기준 (UTC 권장)
            
            ### 활용
            - 가입 기념일 알림
            - 계정 연혁 표시
            - 오래된 계정 식별
            - 데이터 분석 및 통계
            """,
        example = "2024-05-01T12:34:56",
        type = "string",
        format = "date-time",
        required = true
    )
    private final LocalDateTime createdAt;

    @Schema(description = "온보딩 완료 여부", example = "false")
    private final boolean onboardingCompleted;

    @Schema(description = "사용자 닉네임", example = "홍길동")
    private final String nickname;

    @Schema(description = "메인 기록 타입", example = "EXERCISE")
    private final RecordType mainRecordType;

    @Schema(description = "생년월일", example = "1998-06-02")
    private final LocalDate birthDate;

    @Schema(description = "목표 일수", example = "20")
    private final Integer goalDays;

    @Schema(description = "알림 허용 여부", example = "true")
    private final Boolean notificationEnabled;

    /**
     * 도메인 User 객체를 UserResponse DTO로 변환
     * VO 타입들을 기본 타입으로 변환 후 안전하게 매핑
     * 클라이언트에 불필요하거나 민감한 정보는 제외
     *
     * @param user 도메인 사용자 객체
     * @return UserResponse DTO
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId().getValue())
                .name(user.getName())
                .email(user.getEmail() != null ? user.getEmail().getValue() : null)
                .socialType(user.getSocialType())
                .createdAt(user.getCreatedAt())
                .onboardingCompleted(user.isOnboardingCompleted())
                .nickname(user.getNickname())
                .mainRecordType(user.getMainRecordType())
                .birthDate(user.getBirthDate())
                .goalDays(user.getGoalDays())
                .notificationEnabled(user.getNotificationEnabled())
                .build();
    }

    /**
     * 직접 필드 값으로 UserResponse 객체 생성
     *
     * @param id 사용자 ID
     * @param name 사용자 이름
     * @param email 이메일 주소
     * @param socialType 소셜 타입
     * @param createdAt 계정 생성 시간
     * @return UserResponse DTO
     */
    public static UserResponse of(String id, String name, String email,
                                  SocialType socialType, LocalDateTime createdAt) {
        return UserResponse.builder()
                .id(id)
                .name(name)
                .email(email)
                .socialType(socialType)
                .createdAt(createdAt)
                .build();
    }
}