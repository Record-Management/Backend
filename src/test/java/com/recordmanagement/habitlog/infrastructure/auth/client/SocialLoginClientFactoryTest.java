package com.recordmanagement.habitlog.infrastructure.auth.client;

import com.recordmanagement.habitlog.config.exception.SocialLoginException;
import com.recordmanagement.habitlog.domain.user.model.SocialType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

/**
 * SocialLoginClientFactory 단위 테스트
 * 
 * 소셜 로그인 클라이언트 팩토리의 클라이언트 생성 로직을 테스트합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("소셜 로그인 클라이언트 팩토리 테스트")
class SocialLoginClientFactoryTest {

    @Mock
    private KakaoLoginClient kakaoLoginClient;

    @Mock
    private AppleLoginClient appleLoginClient;

    private SocialLoginClientFactory factory;

    @BeforeEach
    void setUp() {
        factory = new SocialLoginClientFactory(kakaoLoginClient, appleLoginClient);
    }

    @Test
    @DisplayName("카카오 타입으로 클라이언트 조회 테스트")
    void getClient_withKakaoType_shouldReturnKakaoClient() {
        // When
        SocialLoginClient client = factory.getClient(SocialType.KAKAO);

        // Then
        assertThat(client).isNotNull();
        assertThat(client).isEqualTo(kakaoLoginClient);
        assertThat(client).isInstanceOf(KakaoLoginClient.class);
    }

    @Test
    @DisplayName("애플 타입으로 클라이언트 조회 테스트")
    void getClient_withAppleType_shouldReturnAppleClient() {
        // When
        SocialLoginClient client = factory.getClient(SocialType.APPLE);

        // Then
        assertThat(client).isNotNull();
        assertThat(client).isEqualTo(appleLoginClient);
        assertThat(client).isInstanceOf(AppleLoginClient.class);
    }

    @Test
    @DisplayName("지원하지 않는 소셜 타입으로 클라이언트 조회 시 예외 발생")
    void getClient_withUnsupportedType_shouldThrowSocialLoginException() {
        // Given - 새로운 SocialType이 추가되었다고 가정
        // 실제로는 enum에 추가해야 하지만, 테스트를 위해 null로 대체
        SocialType unsupportedType = null;

        // When & Then
        assertThatThrownBy(() -> factory.getClient(unsupportedType))
                .isInstanceOf(SocialLoginException.class);
    }

    @Test
    @DisplayName("null 소셜 타입으로 클라이언트 조회 시 예외 발생")
    void getClient_withNullType_shouldThrowSocialLoginException() {
        // When & Then
        assertThatThrownBy(() -> factory.getClient(null))
                .isInstanceOf(SocialLoginException.class);
    }

    @Test
    @DisplayName("팩토리 생성자에 null 카카오 클라이언트 전달 시 정상 작동")
    void constructor_withNullKakaoClient_shouldStillWork() {
        // When
        SocialLoginClientFactory factoryWithNullKakao = 
            new SocialLoginClientFactory(null, appleLoginClient);

        // Then - 애플 클라이언트는 여전히 작동해야 함
        assertThat(factoryWithNullKakao.getClient(SocialType.APPLE))
                .isEqualTo(appleLoginClient);

        // 카카오 클라이언트 조회 시에는 예외가 발생할 수 있음
        assertThatThrownBy(() -> factoryWithNullKakao.getClient(SocialType.KAKAO))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("팩토리 생성자에 null 애플 클라이언트 전달 시 정상 작동")
    void constructor_withNullAppleClient_shouldStillWork() {
        // When
        SocialLoginClientFactory factoryWithNullApple = 
            new SocialLoginClientFactory(kakaoLoginClient, null);

        // Then - 카카오 클라이언트는 여전히 작동해야 함
        assertThat(factoryWithNullApple.getClient(SocialType.KAKAO))
                .isEqualTo(kakaoLoginClient);

        // 애플 클라이언트 조회 시에는 예외가 발생할 수 있음
        assertThatThrownBy(() -> factoryWithNullApple.getClient(SocialType.APPLE))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("같은 타입으로 여러 번 호출해도 같은 클라이언트 반환")
    void getClient_calledMultipleTimes_shouldReturnSameClient() {
        // When
        SocialLoginClient client1 = factory.getClient(SocialType.KAKAO);
        SocialLoginClient client2 = factory.getClient(SocialType.KAKAO);
        SocialLoginClient client3 = factory.getClient(SocialType.KAKAO);

        // Then
        assertThat(client1).isSameAs(client2);
        assertThat(client2).isSameAs(client3);
        assertThat(client1).isEqualTo(kakaoLoginClient);
    }

    @Test
    @DisplayName("다른 타입들로 호출하면 다른 클라이언트 반환")
    void getClient_withDifferentTypes_shouldReturnDifferentClients() {
        // When
        SocialLoginClient kakaoClient = factory.getClient(SocialType.KAKAO);
        SocialLoginClient appleClient = factory.getClient(SocialType.APPLE);

        // Then
        assertThat(kakaoClient).isNotEqualTo(appleClient);
        assertThat(kakaoClient).isEqualTo(kakaoLoginClient);
        assertThat(appleClient).isEqualTo(appleLoginClient);
    }

    @Test
    @DisplayName("팩토리가 모든 소셜 타입을 지원하는지 확인")
    void factory_shouldSupportAllDefinedSocialTypes() {
        // When & Then - 정의된 모든 SocialType에 대해 클라이언트 조회 가능해야 함
        for (SocialType socialType : SocialType.values()) {
            assertThatNoException().isThrownBy(() -> {
                SocialLoginClient client = factory.getClient(socialType);
                assertThat(client).isNotNull();
            });
        }
    }
}
