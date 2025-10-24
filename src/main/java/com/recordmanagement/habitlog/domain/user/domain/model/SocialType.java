package com.recordmanagement.habitlog.domain.user.domain.model;

import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 소셜 로그인 타입 열거형
 *
 * - 지원하는 소셜 로그인 플랫폼을 타입 안전하게 관리
 * - API 통신 시 사용하는 문자열 값과 매핑
 * - 미지원 타입에 대한 예외 처리 명확화
 * - 신규 소셜 로그인 플랫폼 확장 용이
 *
 * 지원 플랫폼:
 * - KAKAO : 카카오 로그인
 * - APPLE : 애플 로그인
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
@Schema(
    description = """
        ## 소셜 로그인 플랫폼 타입
        
        지원하는 소셜 로그인 플랫폼을 정의하는 열거형입니다.
        
        ### 플랫폼별 특징
        
        #### 🔸 KAKAO (카카오)
        - 국내 최대 소셜 플랫폼
        - 빠른 로그인 처리
        - 프로필 정보 풍부
        - 상태: **서비스 중**
        
        #### 🔸 APPLE (애플)
        - iOS 기기 최적화
        - 높은 보안성
        - 이메일 숨기기 기능 지원
        - 상태: **서비스 중**
        
        ### API 사용법
        클라이언트에서는 문자열 값(`kakao`, `apple`)을 사용하며,  
        서버에서 해당 enum 상수로 안전하게 변환됩니다.
        """,
    enumAsRef = true,
    allowableValues = {"KAKAO", "APPLE"}
)
public enum SocialType {

    @Schema(
        description = """
            카카오 로그인
            
            ### 특징
            - 카카오톡 사용자 기반
            - 국내 사용자 친화적
            - 빠른 OAuth 처리
            
            ### 제공 정보
            - 닉네임 (필수)
            - 이메일 (선택)
            - 프로필 이미지 (선택)
            """,
        example = "kakao"
    )
    KAKAO("kakao"),

    @Schema(
        description = """
            애플 로그인 (Sign in with Apple)
            
            ### 특징
            - iOS/macOS 네이티브 지원
            - 강화된 개인정보 보호
            - 이메일 주소 숨기기 지원
            
            ### 제공 정보
            - 이름 (최초 로그인 시)
            - 이메일 (실제 또는 릴레이)
            - 신뢰도 높은 인증
            """,
        example = "apple"
    )
    APPLE("apple");

    /**
     * API에서 사용하는 소셜 타입 문자열 값
     */
    @Schema(hidden = true)
    private final String value;

    /**
     * 문자열로부터 SocialType 객체 변환
     *
     * - API 요청 파라미터로 받은 문자열을 해당 Enum으로 안전하게 변환
     * - 미지원 소셜 타입일 경우 CustomException 발생
     *
     * @param value 소셜 타입 문자열 (kakao, apple)
     * @return SocialType enum
     * @throws CustomException 지원하지 않는 소셜 타입인 경우 발생
     */
    public static SocialType fromValue(String value) {
        for (SocialType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new CustomException(ErrorCode.SOCIAL_TYPE_UNKNOWN);
    }

    /**
     * 사용자 화면에 표시할 소셜 플랫폼 명칭 반환
     *
     * @return 표시용 플랫폼 이름
     */
    public String getDisplayName() {
        return switch (this) {
            case KAKAO -> "카카오";
            case APPLE -> "Apple";
        };
    }

    /**
     * 소셜 로그인 버튼에 표시할 아이콘 이름 반환
     *
     * @return 아이콘 파일명
     */
    public String getIconName() {
        return switch (this) {
            case KAKAO -> "kakao-icon";
            case APPLE -> "apple-icon";
        };
    }

    /**
     * 소셜 로그인 버튼 색상 반환
     *
     * @return HEX 색상 코드
     */
    public String getBrandColor() {
        return switch (this) {
            case KAKAO -> "#FEE500";
            case APPLE -> "#000000";
        };
    }
}