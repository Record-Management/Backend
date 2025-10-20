package com.recordmanagement.habitlog.config.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 전역 에러 코드 정의 Enum
 *
 * 서비스 전반에서 발생할 수 있는 모든 예외 상황을 통합 관리하기 위한 에러 코드 집합입니다.
 * HTTP 상태 코드, 프론트엔드 분기용 에러 코드, 사용자 친화적 메시지를 함께 포함하여
 * 일관된 에러 처리 및 응답 구조를 제공합니다.
 *
 * 설계 원칙
 * HTTP 상태 코드 별로 에러를 그룹화하여 관리
 * 프론트엔드에서 에러를 쉽게 분기 처리할 수 있도록 고유한 에러 코드 제공
 * 최종 사용자에게 친근하고 이해하기 쉬운 한국어 메시지 제공
 * 에러 모니터링 및 로그 추적을 위해 유니크한 코드 부여
 *
 * 사용 예시
 * // 비즈니스 로직에서 예외 발생 시
 * throw new CustomException(ErrorCode.USER_NOT_FOUND);
 *
 * // 글로벌 예외 처리 핸들러에서 HTTP 상태 및 메시지 사용
 * return ResponseEntity.status(errorCode.getStatus())
 *                      .body(ErrorResponse.of(errorCode));
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
@Schema(description = "API 전역 에러 코드 및 메시지 정의")
public enum ErrorCode {

    // ============================================================================
    // 400 BAD REQUEST - 클라이언트 요청 오류
    // ============================================================================

    @Schema(description = "잘못된 입력 값")
    INVALID_INPUT_VALUE(400, "E40001", "잘못된 입력 값입니다."),

    @Schema(description = "입력값 검증 실패")
    VALIDATION_FAIL(400, "E40002", "입력값 검증에 실패했습니다."),

    @Schema(description = "필수 요청 파라미터 누락")
    MISSING_REQUEST_PARAMETER(400, "E40003", "필수 요청 파라미터가 누락되었습니다."),

    @Schema(description = "잘못된 데이터 타입")
    INVALID_TYPE_VALUE(400, "E40004", "잘못된 데이터 타입입니다."),

    @Schema(description = "JSON 형식 오류")
    JSON_PARSE_ERROR(400, "E40005", "JSON 형식이 올바르지 않습니다."),

    // ------------------------------------------------------------------------
    // 소셜 로그인 관련 에러 (E40010 ~ E40019)
    // ------------------------------------------------------------------------

    @Schema(description = "소셜 로그인 실패")
    SOCIAL_LOGIN_FAILED(400, "E40010", "소셜 로그인에 실패했습니다."),

    @Schema(description = "지원하지 않는 소셜 로그인 제공자")
    SOCIAL_PROVIDER_NOT_SUPPORTED(400, "E40011", "지원하지 않는 소셜 로그인 제공자입니다."),

    @Schema(description = "소셜 로그인 액세스 토큰 유효하지 않음")
    SOCIAL_ACCESS_TOKEN_INVALID(400, "E40012", "소셜 로그인 액세스 토큰이 유효하지 않습니다."),

    @Schema(description = "소셜 로그인 사용자 정보 조회 실패")
    SOCIAL_USER_INFO_FETCH_FAILED(400, "E40013", "소셜 로그인 사용자 정보를 가져오지 못했습니다."),

    @Schema(description = "소셜 ID null 또는 빈 값")
    SOCIAL_ID_NULL_OR_EMPTY(400, "E40014", "소셜 ID는 null이거나 비어있을 수 없습니다."),

    @Schema(description = "소셜 계정 이름 null 또는 빈 값")
    SOCIAL_NAME_NULL_OR_EMPTY(400, "E40015", "이름은 null이거나 비어있을 수 없습니다."),

    @Schema(description = "지원하지 않는 소셜 타입")
    SOCIAL_CLIENT_NOT_SUPPORTED(400, "E40016", "지원하지 않는 소셜 타입입니다."),

    @Schema(description = "Apple 로그인 미구현")
    APPLE_LOGIN_NOT_IMPLEMENTED(400, "E40017", "Apple 로그인 구현이 아직 완료되지 않았습니다."),

    @Schema(description = "Apple ID 토큰 유효하지 않음")
    APPLE_ID_TOKEN_INVALID(400, "E40018", "Apple ID 토큰이 유효하지 않습니다."),

    @Schema(description = "Apple 토큰 만료됨")
    APPLE_TOKEN_EXPIRED(400, "E40019", "Apple 토큰이 만료되었습니다."),

    // ------------------------------------------------------------------------
    // 리프레시 토큰 관련 에러 (E40020 ~ E40029)
    // ------------------------------------------------------------------------

    @Schema(description = "리프레시 토큰 null 또는 빈 값")
    REFRESH_TOKEN_NULL_OR_EMPTY(400, "E40020", "리프레시 토큰은 null이거나 비어있을 수 없습니다."),

    @Schema(description = "리프레시 토큰 사용자 ID null 또는 빈 값")
    REFRESH_TOKEN_USER_ID_NULL_OR_EMPTY(400, "E40021", "사용자 ID는 null이거나 비어있을 수 없습니다."),

    @Schema(description = "리프레시 토큰 만료 시간 null")
    REFRESH_TOKEN_EXPIRES_AT_NULL(400, "E40022", "만료 시간은 null일 수 없습니다."),

    // ------------------------------------------------------------------------
    // 사용자 관련 에러 (E40021 ~ E40030)
    // ------------------------------------------------------------------------

    @Schema(description = "이메일 중복 가입 시도")
    USER_EMAIL_DUPLICATE(400, "E40021", "이미 사용 중인 이메일입니다."),

    @Schema(description = "사용자 중복 가입 시도")
    USER_ALREADY_EXISTS(400, "E40022", "이미 가입된 사용자입니다."),

    @Schema(description = "이메일 형식 오류")
    INVALID_EMAIL_FORMAT(400, "E40023", "이메일 형식이 올바르지 않습니다."),

    @Schema(description = "비밀번호 형식 오류")
    INVALID_PASSWORD_FORMAT(400, "E40024", "비밀번호 형식이 올바르지 않습니다."),

    @Schema(description = "사용자 ID null 또는 빈 값")
    USER_ID_NULL_OR_EMPTY(400, "E40025", "사용자 ID는 null이거나 비어있을 수 없습니다."),

    @Schema(description = "알 수 없는 소셜 타입")
    SOCIAL_TYPE_UNKNOWN(400, "E40026", "알 수 없는 소셜 타입입니다."),

    @Schema(description = "JWT 사용자 ID 형식 오류")
    JWT_USER_ID_INVALID_FORMAT(400, "E40027", "토큰의 사용자 ID가 유효한 Long 타입이 아닙니다."),

    @Schema(description = "회원탈퇴 실패")
    USER_WITHDRAWAL_FAILED(400, "E40028", "회원탈퇴 처리 중 오류가 발생했습니다."),

    @Schema(description = "이미 온보딩 완료된 사용자")
    USER_ONBOARDING_ALREADY_COMPLETED(400, "E40029", "이미 온보딩이 완료된 사용자입니다."),

    @Schema(description = "소셜 ID 이미 존재")
    SOCIAL_ID_ALREADY_EXISTS(400, "E40030", "이미 사용 중인 소셜 ID입니다."),

    // ------------------------------------------------------------------------
    // 파일 업로드 관련 에러 (E40070 ~ E40079)
    // ------------------------------------------------------------------------

    @Schema(description = "파일이 비어있음")
    FILE_EMPTY(400, "E40070", "업로드할 파일이 비어있습니다."),

    @Schema(description = "지원하지 않는 파일 형식")
    UNSUPPORTED_FILE_FORMAT(400, "E40071", "지원하지 않는 파일 형식입니다."),

    @Schema(description = "파일 크기 초과")
    FILE_SIZE_EXCEEDED(400, "E40072", "파일 크기가 제한을 초과했습니다."),

    @Schema(description = "파일 개수 초과")
    FILE_COUNT_EXCEEDED(400, "E40073", "파일 개수가 최대 3개를 초과했습니다."),

    @Schema(description = "파일 업로드 실패")
    FILE_UPLOAD_FAILED(500, "E50006", "파일 업로드에 실패했습니다."),

    @Schema(description = "파일 삭제 실패")
    FILE_DELETE_FAILED(500, "E50007", "파일 삭제에 실패했습니다."),

    @Schema(description = "S3 연결 오류")
    S3_CONNECTION_ERROR(500, "E50008", "S3 서비스 연결에 실패했습니다."),

    // ============================================================================
    // 401 UNAUTHORIZED - 인증 관련 오류
    // ============================================================================

    @Schema(description = "인증 필요")
    UNAUTHORIZED(401, "E40101", "인증이 필요합니다."),

    @Schema(description = "유효하지 않은 토큰")
    INVALID_TOKEN(401, "E40102", "유효하지 않은 토큰입니다."),

    @Schema(description = "토큰 만료됨")
    EXPIRED_TOKEN(401, "E40103", "토큰이 만료되었습니다."),

    @Schema(description = "잘못된 토큰 형식")
    TOKEN_MALFORMED(401, "E40104", "잘못된 형식의 토큰입니다."),

    @Schema(description = "토큰 서명 검증 실패")
    TOKEN_SIGNATURE_INVALID(401, "E40105", "토큰 서명이 유효하지 않습니다."),

    @Schema(description = "리프레시 토큰 만료됨")
    REFRESH_TOKEN_EXPIRED(401, "E40106", "리프레시 토큰이 만료되었습니다."),

    @Schema(description = "유효하지 않은 리프레시 토큰")
    REFRESH_TOKEN_INVALID(401, "E40107", "유효하지 않은 리프레시 토큰입니다."),

    @Schema(description = "로그인 필요 상태")
    LOGIN_REQUIRED(401, "E40108", "로그인이 필요합니다."),

    // ============================================================================
    // 403 FORBIDDEN - 권한 관련 오류
    // ============================================================================

    @Schema(description = "접근 거부됨")
    FORBIDDEN(403, "E40301", "접근이 거부되었습니다."),

    @Schema(description = "리소스 접근 권한 없음")
    ACCESS_DENIED(403, "E40302", "해당 리소스에 접근할 권한이 없습니다."),

    @Schema(description = "권한 부족")
    INSUFFICIENT_PERMISSION(403, "E40303", "권한이 부족합니다."),

    @Schema(description = "특정 데이터 접근 권한 없음")
    RESOURCE_ACCESS_DENIED(403, "E40304", "해당 데이터에 접근할 권한이 없습니다."),

    @Schema(description = "기록 접근 권한 없음")
    RECORD_ACCESS_DENIED(403, "E40305", "해당 기록에 접근할 권한이 없습니다."),

    // ============================================================================
    // 404 NOT FOUND - 리소스 없음
    // ============================================================================

    @Schema(description = "요청 자원 없음")
    NOT_FOUND(404, "E40401", "요청한 자원을 찾을 수 없습니다."),

    @Schema(description = "사용자 정보 없음")
    USER_NOT_FOUND(404, "E40402", "존재하지 않는 사용자입니다."),

    @Schema(description = "이미 탈퇴하지 않은 사용자")
    USER_NOT_WITHDRAWN(400, "E40403", "탈퇴하지 않은 사용자입니다."),

    @Schema(description = "영구 삭제된 사용자")
    USER_PERMANENTLY_DELETED(400, "E40404", "영구 삭제된 사용자로 복구할 수 없습니다."),



    @Schema(description = "API 엔드포인트 없음")
    API_ENDPOINT_NOT_FOUND(404, "E40405", "존재하지 않는 API 엔드포인트입니다."),

    @Schema(description = "기록 정보 없음")
    RECORD_NOT_FOUND(404, "E40406", "존재하지 않는 기록입니다."),

    @Schema(description = "일일 기록 등록 제한 초과")
    DAILY_RECORD_LIMIT_EXCEEDED(400, "E40407", "하루에 등록할 수 있는 일상 기록은 최대 1개입니다."),
    
    @Schema(description = "운동 기록 등록 제한 초과")
    EXERCISE_RECORD_LIMIT_EXCEEDED(400, "E40408", "하루에 등록할 수 있는 운동 기록은 최대 1개입니다."),
    
    @Schema(description = "습관 기록 등록 제한 초과") 
    HABIT_RECORD_LIMIT_EXCEEDED(400, "E40409", "하루에 등록할 수 있는 습관 기록은 최대 1개입니다."),
    
    @Schema(description = "기록 종류 등록 제한 초과")
    RECORD_TYPE_LIMIT_EXCEEDED(400, "E40410", "하루에 등록할 수 있는 기록 종류는 최대 2가지입니다."),

    // ============================================================================
    // 405 METHOD NOT ALLOWED - HTTP 메서드 오류
    // ============================================================================

    @Schema(description = "지원하지 않는 HTTP 메서드")
    METHOD_NOT_ALLOWED(405, "E40501", "허용되지 않는 HTTP 메서드입니다."),

    // ============================================================================
    // 409 CONFLICT - 리소스 충돌
    // ============================================================================

    @Schema(description = "리소스 충돌 발생")
    CONFLICT(409, "E40901", "리소스 충돌이 발생했습니다."),

    @Schema(description = "데이터 무결성 제약 위반")
    DATA_INTEGRITY_VIOLATION(409, "E40902", "데이터 무결성 제약 조건을 위반했습니다."),

    @Schema(description = "중복된 리소스")
    DUPLICATE_RESOURCE(409, "E40903", "중복된 리소스입니다."),

    // ============================================================================
    // 415 UNSUPPORTED MEDIA TYPE - 미디어 타입 오류
    // ============================================================================

    @Schema(description = "지원하지 않는 미디어 타입")
    UNSUPPORTED_MEDIA_TYPE(415, "E41501", "지원하지 않는 미디어 타입입니다."),

    // ============================================================================
    // 429 TOO MANY REQUESTS - 요청 제한
    // ============================================================================

    @Schema(description = "요청 제한 초과")
    TOO_MANY_REQUESTS(429, "E42901", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),

    @Schema(description = "Rate Limit 초과")
    RATE_LIMIT_EXCEEDED(429, "E42902", "요청 한도를 초과했습니다."),

    // ============================================================================
    // 500 INTERNAL SERVER ERROR - 서버 내부 오류
    // ============================================================================

    @Schema(description = "서버 내부 오류")
    INTERNAL_SERVER_ERROR(500, "E50001", "서버 내부 오류가 발생했습니다."),

    @Schema(description = "데이터베이스 오류")
    DATABASE_ERROR(500, "E50002", "데이터베이스 오류가 발생했습니다."),

    @Schema(description = "외부 API 호출 오류")
    EXTERNAL_API_ERROR(500, "E50003", "외부 API 호출 중 오류가 발생했습니다."),

    @Schema(description = "파일 처리 오류")
    FILE_PROCESSING_ERROR(500, "E50004", "파일 처리 중 오류가 발생했습니다."),

    @Schema(description = "암호화/복호화 오류")
    ENCRYPTION_ERROR(500, "E50005", "암호화 처리 중 오류가 발생했습니다."),

    // ============================================================================
    // 502 BAD GATEWAY - 게이트웨이 오류
    // ============================================================================

    @Schema(description = "외부 서버 응답 오류")
    BAD_GATEWAY(502, "E50201", "외부 서버로부터 잘못된 응답을 받았습니다."),

    @Schema(description = "소셜 로그인 서비스 장애")
    SOCIAL_API_UNAVAILABLE(502, "E50202", "소셜 로그인 서비스가 일시적으로 사용할 수 없습니다."),

    // ============================================================================
    // 503 SERVICE UNAVAILABLE - 서비스 사용 불가
    // ============================================================================

    @Schema(description = "서비스 일시 중단")
    SERVICE_UNAVAILABLE(503, "E50301", "서비스를 일시적으로 사용할 수 없습니다."),

    @Schema(description = "시스템 점검 중")
    MAINTENANCE_MODE(503, "E50302", "서비스 점검 중입니다."),

    // ============================================================================
    // 504 GATEWAY TIMEOUT - 타임아웃
    // ============================================================================

    @Schema(description = "요청 처리 시간 초과")
    GATEWAY_TIMEOUT(504, "E50401", "요청 처리 시간이 초과되었습니다."),

    @Schema(description = "외부 API 응답 시간 초과")
    EXTERNAL_API_TIMEOUT(504, "E50402", "외부 API 응답 시간이 초과되었습니다.");

    // ============================================================================
    // 필드 정의
    // ============================================================================

    @Schema(description = "HTTP 상태 코드", example = "400")
    private final int status;

    @Schema(description = "프론트엔드 분기용 에러 코드", example = "E40001")
    private final String code;

    @Schema(description = "사용자에게 보여줄 에러 메시지", example = "잘못된 입력 값입니다.")
    private final String message;

    /**
     * 생성자 - enum 상수 초기화용
     *
     * @param status HTTP 상태 코드
     * @param code 프론트엔드 분기 처리용 에러 코드
     * @param message 사용자 친화적 메시지
     */
    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
