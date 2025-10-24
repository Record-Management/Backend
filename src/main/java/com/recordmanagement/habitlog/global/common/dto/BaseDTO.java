package com.recordmanagement.habitlog.global.common.dto;

/**
 * DTO 클래스들을 위한 공통 기능 및 패턴 정의
 * 
 * 프로젝트 내 모든 DTO 클래스들의 일관성을 보장하고
 * 표준 패턴을 제공하기 위한 공통 컴포넌트입니다.
 * 
 * 주요 기능:
 * - 정적 팩토리 메서드 패턴 인터페이스 제공
 * - DTO 변환 패턴 표준화
 * - 코드 일관성 및 가독성 향상
 * 
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
public final class BaseDTO {

    /**
     * 인스턴스 생성 방지를 위한 private 생성자
     */
    private BaseDTO() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 변환 가능한 DTO를 위한 인터페이스
     * 
     * from() 정적 팩토리 메서드 패턴을 강제하여
     * 도메인 모델 → DTO 변환의 일관성을 보장합니다.
     * 
     * @param <S> 변환 대상(Source) 타입
     * @param <T> 변환 결과(Target) DTO 타입
     */
    public interface Convertible<S, T> {
        /**
         * 도메인 모델에서 DTO로 변환
         * 
         * 구현 예시:
         * ```java
         * public static UserResponse from(User user) {
         *     return UserResponse.builder()
         *         .id(user.getId())
         *         .name(user.getName())
         *         .build();
         * }
         * ```
         * 
         * @param source 변환할 도메인 모델
         * @return 변환된 DTO 객체
         */
        static <T> T from(Object source) {
            throw new UnsupportedOperationException("from() 메서드를 구현해주세요");
        }
    }

    /**
     * 생성 가능한 DTO를 위한 인터페이스
     * 
     * of() 정적 팩토리 메서드 패턴을 강제하여
     * 파라미터 → DTO 생성의 일관성을 보장합니다.
     * 
     * @param <T> 생성할 DTO 타입
     */
    public interface Creatable<T> {
        /**
         * 파라미터들로부터 DTO 생성
         * 
         * 구현 예시:
         * ```java
         * public static RefreshTokenCommand of(String refreshToken) {
         *     return new RefreshTokenCommand(refreshToken);
         * }
         * ```
         * 
         * @param args 생성에 필요한 파라미터들
         * @return 생성된 DTO 객체
         */
        static <T> T of(Object... args) {
            throw new UnsupportedOperationException("of() 메서드를 구현해주세요");
        }
    }

    /**
     * 빌더 패턴을 지원하는 DTO를 위한 인터페이스
     * 
     * 복잡한 DTO 생성 시 빌더 패턴의 일관성을 보장합니다.
     * 
     * @param <T> 빌더로 생성할 DTO 타입
     */
    public interface Buildable<T> {
        /**
         * DTO 빌더 생성
         * 
         * 구현 예시:
         * ```java
         * public static UserResponseBuilder builder() {
         *     return new UserResponseBuilder();
         * }
         * ```
         * 
         * @return DTO 빌더 객체
         */
        static Object builder() {
            throw new UnsupportedOperationException("builder() 메서드를 구현해주세요");
        }
    }

    /**
     * 유효성 검증이 가능한 DTO를 위한 인터페이스
     * 
     * DTO 생성 시 유효성 검증 로직의 일관성을 보장합니다.
     */
    public interface Validatable {
        /**
         * DTO 유효성 검증
         * 
         * @return true: 유효, false: 무효
         */
        default boolean isValid() {
            return true;
        }

        /**
         * 유효성 검증 실패 시 예외 발생
         * 
         * @throws IllegalArgumentException 유효성 검증 실패 시
         */
        default void validate() {
            if (!isValid()) {
                throw new IllegalArgumentException("DTO 유효성 검증에 실패했습니다");
            }
        }
    }
}
