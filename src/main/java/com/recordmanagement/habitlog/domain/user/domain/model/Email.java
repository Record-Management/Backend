package com.recordmanagement.habitlog.domain.user.domain.model;

import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.regex.Pattern;

/**
 * 이메일 값 객체 (Value Object)
 *
 * 목적:
 * - 이메일 주소의 형식 검증 및 타입 안전성 제공
 * - 단순 String 대신 의미 있는 타입으로 도메인 표현력 향상
 * - 이메일 관련 비즈니스 규칙을 캡슐화
 *
 * 검증 규칙:
 * - RFC 5322 표준을 기반으로 한 정규 표현식 검증
 * - 로컬 부분: 영문자, 숫자, +, _, ., - 허용
 * - 도메인 부분: 영문자, 숫자, ., - 허용, 최소 2자리 TLD 필수
 *
 * Value Object 특징:
 * - 불변 객체: 생성 후 값 변경 불가
 * - 동등성 비교: 이메일 값이 같으면 같은 객체로 취급
 * - 자체 검증: 유효하지 않은 이메일로 객체 생성 방지
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
@EqualsAndHashCode
@Schema(description = "이메일 값 객체")
public class Email {

    @Schema(description = "이메일 주소", example = "user@example.com")
    private final String value;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    private Email(String value) {
        if (value == null || !EMAIL_PATTERN.matcher(value).matches()) {
            throw new CustomException(ErrorCode.INVALID_EMAIL_FORMAT);
        }
        this.value = value;
    }

    public static Email of(String value) {
        if (value == null) {
            return null;
        }
        return new Email(value);
    }

    @Override
    public String toString() {
        return value;
    }
}