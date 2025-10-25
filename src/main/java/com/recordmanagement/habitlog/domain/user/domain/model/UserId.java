package com.recordmanagement.habitlog.domain.user.domain.model;

import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.util.UUID;

/**
 * 사용자 ID 값 객체 (Value Object)
 *
 * - 사용자의 고유 식별자를 타입 안전하게 관리하기 위한 도메인 객체
 * - 단순 문자열 대신 의미 있는 타입으로 도메인 표현력을 높임
 * - UUID 기반 ID 생성 및 유효성 검증 로직 캡슐화
 *
 * 주요 기능:
 * - UUID 자동 생성 및 기존 값 복원 기능 제공
 * - 불변 객체 설계로 ID 변경 불가 보장
 * - null, 빈 문자열 등 잘못된 값 생성 방지
 *
 * 동등성 비교 기준은 내부 값(value)으로 함
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Value
@Schema(description = "사용자 고유 식별자 값 객체")
public class UserId {

    @Schema(description = "UUID 형식의 사용자 고유 ID 값", example = "550e8400-e29b-41d4-a716-446655440000")
    String value;

    /**
     * 생성자 (내부 사용용)
     *
     * - 값 검증 수행 (null, 빈 문자열 불가)
     * - 외부 직접 호출 금지, 생성 메서드 사용 권장
     *
     * @param value UUID 형식의 사용자 ID 문자열
     * @throws CustomException 유효하지 않은 값일 경우
     */
    private UserId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new CustomException(ErrorCode.USER_ID_NULL_OR_EMPTY);
        }
        this.value = value;
    }

    /**
     * 새로운 UUID 기반 사용자 ID 생성
     *
     * - UUID.randomUUID() 활용 고유성 보장
     * - 분산 환경에서도 중복 위험 최소화
     *
     * @return 신규 생성된 UserId 객체
     */
    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }

    /**
     * 기존 문자열 값으로부터 UserId 객체 생성
     *
     * - DB 조회 결과 등으로 복원 시 사용
     * - 입력 값 검증 포함
     *
     * @param value 기존 사용자 ID 문자열
     * @return 검증된 UserId 객체
     * @throws CustomException 유효하지 않은 값일 경우
     */
    public static UserId of(String value) {
        return new UserId(value);
    }

    /**
     * 기존 문자열 값으로부터 UserId 객체 생성 (of 메서드와 동일)
     *
     * - 코드 일관성을 위한 alias 메서드
     * - DB 조회 결과 등으로 복원 시 사용
     *
     * @param value 기존 사용자 ID 문자열
     * @return 검증된 UserId 객체
     * @throws CustomException 유효하지 않은 값일 경우
     */
    public static UserId from(String value) {
        return new UserId(value);
    }

    /**
     * 사용자 ID 문자열 반환
     *
     * @return 내부 UUID 문자열 값
     */
    @Override
    public String toString() {
        return value;
    }
}