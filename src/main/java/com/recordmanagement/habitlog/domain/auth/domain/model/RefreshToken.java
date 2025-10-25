package com.recordmanagement.habitlog.domain.auth.domain.model;

import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JWT 리프레시 토큰 도메인 모델
 *
 * 리프레시 토큰의 데이터와 비즈니스 로직을 캡슐화합니다.
 * 액세스 토큰 갱신을 위한 장기 유효 토큰으로 사용되며,
 * 사용자 세션 유지 및 보안 강화를 목적으로 합니다.
 *
 * 비즈니스 규칙
 * - 하나의 사용자당 하나의 리프레시 토큰만 존재 가능
 * - 토큰 사용 시 만료 여부를 반드시 확인
 * - 만료된 토큰은 즉시 무효 처리
 *
 * 보안 고려사항
 * - 리프레시 토큰 유효기간은 보통 30일
 * - 토큰 재사용 방지를 위한 리프레시 토큰 회전(RTR) 적용 권장
 * - 이상 활동 감지 시 즉시 폐기
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "JWT 리프레시 토큰 도메인 모델")
public class RefreshToken {

    @Schema(description = "JWT 리프레시 토큰 문자열", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "토큰 소유자 사용자 ID", example = "user123")
    private String userId;

    @Schema(description = "토큰 만료 시간", example = "2025-08-29T13:45:30")
    private LocalDateTime expiresAt;

    /**
     *  JPA 등에서 사용하기 위한 생성 시간 설정자.
     *
     * @param createdAt 생성 시간
     */
    @Setter
    @Schema(description = "토큰 생성 시간", example = "2025-07-30T13:45:30")
    private LocalDateTime createdAt;

    /**
     * 빌더 대신 사용할 정적 팩토리 메서드.
     * 유효성 검증을 수행하여 잘못된 값 입력 시 예외 발생.
     *
     * @param token JWT 리프레시 토큰 문자열
     * @param userId 사용자 ID
     * @param expiresAt 토큰 만료 시간
     * @return 검증된 RefreshToken 인스턴스
     * @throws CustomException 검증 실패 시 발생
     */
    public static RefreshToken of(String token, String userId, LocalDateTime expiresAt) {
        if (token == null || token.trim().isEmpty()) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NULL_OR_EMPTY);
        }

        if (userId == null || userId.trim().isEmpty()) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_USER_ID_NULL_OR_EMPTY);
        }

        if (expiresAt == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRES_AT_NULL);
        }

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.token = token;
        refreshToken.userId = userId;
        refreshToken.expiresAt = expiresAt;
        refreshToken.createdAt = LocalDateTime.now();

        return refreshToken;
    }

    /**
     * 토큰이 만료되었는지 확인합니다.
     *
     * @return true: 만료됨, false: 유효함
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 토큰 유효성을 검증합니다.
     *
     * @return true: 유효함, false: 만료됨
     */
    public boolean isValid() {
        return !isExpired();
    }
}