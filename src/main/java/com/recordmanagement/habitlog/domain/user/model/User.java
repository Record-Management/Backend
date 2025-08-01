package com.recordmanagement.habitlog.domain.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 사용자 도메인 핵심 엔터티 (Aggregate Root)
 *
 * - 사용자 식별, 기본 정보 및 소셜 로그인 관련 데이터 보유
 * - 도메인 내 사용자 관련 비즈니스 로직 캡슐화
 * - JPA 엔티티로 활용 가능하며 생성 및 변경 시점 관리
 *
 * 주요 필드:
 * - id: 사용자 고유 식별자 (UserId 타입, 생성 시 자동 할당)
 * - name: 사용자 이름 또는 닉네임
 * - email: Email 값 객체로 이메일 형식 검증 포함
 * - socialType: 로그인 소셜 플랫폼 타입 (카카오, 애플 등)
 * - socialId: 소셜 플랫폼 내 고유 사용자 식별자
 * - createdAt: 계정 생성 시간
 * - updatedAt: 마지막 프로필 수정 시간
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA 기본 생성자 용도, 직접 호출 금지
@Schema(description = "사용자 도메인 핵심 엔터티")
public class User {

    @Schema(description = "사용자 고유 식별자", example = "user-1234-uuid")
    private UserId id;

    @Schema(description = "사용자 이름 또는 닉네임", example = "홍길동")
    private String name;

    @Schema(description = "사용자 이메일 정보")
    private Email email;

    @Schema(description = "사용자 로그인 소셜 플랫폼 타입", example = "kakao")
    private SocialType socialType;

    @Schema(description = "소셜 플랫폼 고유 사용자 식별자", example = "1234567890")
    private String socialId;

    @Schema(description = "계정 생성 시간", example = "2025-07-30T12:34:56")
    private LocalDateTime createdAt;

    @Schema(description = "마지막 프로필 수정 시간", example = "2025-07-31T09:00:00")
    private LocalDateTime updatedAt;

    /**
     * 신규 사용자 생성 생성자
     *
     * - UserId는 도메인 내에서 생성 (UUID 등)
     * - createdAt과 updatedAt은 생성 시 현재 시간으로 자동 세팅
     *
     * @param name 사용자 이름
     * @param email 이메일 값 객체
     * @param socialType 소셜 로그인 플랫폼 타입
     * @param socialId 소셜 플랫폼 내 고유 사용자 ID
     */
    public User(String name, Email email, SocialType socialType, String socialId) {
        this.id = UserId.generate();
        this.name = name;
        this.email = email;
        this.socialType = socialType;
        this.socialId = socialId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 사용자 프로필 정보 수정
     *
     * - 이름과 이메일 변경 가능
     * - 변경 시 updatedAt 자동 갱신
     *
     * @param name 변경할 이름
     * @param email 변경할 이메일 (Email VO)
     */
    public void updateProfile(String name, Email email) {
        this.name = name;
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 동일 사용자 여부 확인
     *
     * - 비교 기준은 UserId 값
     *
     * @param userId 비교할 사용자 ID
     * @return true 동일 사용자, false 다른 사용자
     */
    public boolean isSameUser(UserId userId) {
        return this.id.equals(userId);
    }
}