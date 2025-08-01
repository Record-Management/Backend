package com.recordmanagement.habitlog.application.auth.dto;

import com.recordmanagement.habitlog.domain.user.model.SocialType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * SocialLoginCommand DTO 단위 테스트
 * 
 * 소셜 로그인 명령 객체의 생성과 빌더 패턴을 테스트합니다.
 */
@DisplayName("소셜 로그인 명령 DTO 테스트")
class SocialLoginCommandTest {

    @Test
    @DisplayName("생성자로 SocialLoginCommand 생성 테스트")
    void createSocialLoginCommand_withConstructor_shouldCreateSuccessfully() {
        // Given
        SocialType socialType = SocialType.KAKAO;
        String accessToken = "kakao-access-token-123";

        // When
        SocialLoginCommand command = new SocialLoginCommand(socialType, accessToken);

        // Then
        assertThat(command).isNotNull();
        assertThat(command.getSocialType()).isEqualTo(socialType);
        assertThat(command.getAccessToken()).isEqualTo(accessToken);
    }

    @Test
    @DisplayName("빌더 패턴으로 SocialLoginCommand 생성 테스트")
    void createSocialLoginCommand_withBuilder_shouldCreateSuccessfully() {
        // Given
        SocialType socialType = SocialType.APPLE;
        String accessToken = "apple-id-token-456";

        // When
        SocialLoginCommand command = SocialLoginCommand.builder()
                .socialType(socialType)
                .accessToken(accessToken)
                .build();

        // Then
        assertThat(command).isNotNull();
        assertThat(command.getSocialType()).isEqualTo(socialType);
        assertThat(command.getAccessToken()).isEqualTo(accessToken);
    }

    @Test
    @DisplayName("정적 팩토리 메서드로 SocialLoginCommand 생성 테스트")
    void createSocialLoginCommand_withStaticFactory_shouldCreateSuccessfully() {
        // Given
        SocialType socialType = SocialType.KAKAO;
        String accessToken = "test-token-789";

        // When
        SocialLoginCommand command = SocialLoginCommand.of(socialType, accessToken);

        // Then
        assertThat(command).isNotNull();
        assertThat(command.getSocialType()).isEqualTo(socialType);
        assertThat(command.getAccessToken()).isEqualTo(accessToken);
    }

    @Test
    @DisplayName("다양한 소셜 타입으로 생성 테스트")
    void createSocialLoginCommand_withDifferentSocialTypes_shouldCreateSuccessfully() {
        // Given
        String accessToken = "test-token";

        // When
        SocialLoginCommand kakaoCommand = SocialLoginCommand.of(SocialType.KAKAO, accessToken);
        SocialLoginCommand appleCommand = SocialLoginCommand.of(SocialType.APPLE, accessToken);

        // Then
        assertThat(kakaoCommand.getSocialType()).isEqualTo(SocialType.KAKAO);
        assertThat(appleCommand.getSocialType()).isEqualTo(SocialType.APPLE);
        assertThat(kakaoCommand.getAccessToken()).isEqualTo(accessToken);
        assertThat(appleCommand.getAccessToken()).isEqualTo(accessToken);
    }

    @Test
    @DisplayName("null 값들로 생성 시에도 객체는 생성되어야 함 (Validation은 별도 처리)")
    void createSocialLoginCommand_withNullValues_shouldCreateObjectButWithNullFields() {
        // Given
        SocialType nullSocialType = null;
        String nullAccessToken = null;

        // When
        SocialLoginCommand command = SocialLoginCommand.of(nullSocialType, nullAccessToken);

        // Then
        assertThat(command).isNotNull();
        assertThat(command.getSocialType()).isNull();
        assertThat(command.getAccessToken()).isNull();
    }

    @Test
    @DisplayName("빈 문자열 액세스 토큰으로 생성 테스트")
    void createSocialLoginCommand_withEmptyAccessToken_shouldCreateSuccessfully() {
        // Given
        SocialType socialType = SocialType.KAKAO;
        String emptyAccessToken = "";

        // When
        SocialLoginCommand command = SocialLoginCommand.of(socialType, emptyAccessToken);

        // Then
        assertThat(command).isNotNull();
        assertThat(command.getSocialType()).isEqualTo(socialType);
        assertThat(command.getAccessToken()).isEqualTo(emptyAccessToken);
    }

    @Test
    @DisplayName("긴 액세스 토큰으로 생성 테스트")
    void createSocialLoginCommand_withLongAccessToken_shouldCreateSuccessfully() {
        // Given
        SocialType socialType = SocialType.APPLE;
        String longAccessToken = "a".repeat(1000); // 1000자 토큰

        // When
        SocialLoginCommand command = SocialLoginCommand.of(socialType, longAccessToken);

        // Then
        assertThat(command).isNotNull();
        assertThat(command.getSocialType()).isEqualTo(socialType);
        assertThat(command.getAccessToken()).isEqualTo(longAccessToken);
        assertThat(command.getAccessToken()).hasSize(1000);
    }

    @Test
    @DisplayName("같은 값으로 생성한 명령 객체들의 필드값 비교")
    void createSocialLoginCommand_withSameValues_shouldHaveSameFieldValues() {
        // Given
        SocialType socialType = SocialType.KAKAO;
        String accessToken = "same-token";

        // When
        SocialLoginCommand command1 = SocialLoginCommand.of(socialType, accessToken);
        SocialLoginCommand command2 = SocialLoginCommand.builder()
                .socialType(socialType)
                .accessToken(accessToken)
                .build();

        // Then
        assertThat(command1.getSocialType()).isEqualTo(command2.getSocialType());
        assertThat(command1.getAccessToken()).isEqualTo(command2.getAccessToken());
    }
}
