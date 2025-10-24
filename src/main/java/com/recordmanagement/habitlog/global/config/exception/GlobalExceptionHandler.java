package com.recordmanagement.habitlog.global.config.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.recordmanagement.habitlog.global.common.response.ApiResponse;
import com.recordmanagement.habitlog.domain.auth.exception.JwtAuthenticationException;
import com.recordmanagement.habitlog.domain.auth.exception.SocialLoginException;
import com.recordmanagement.habitlog.domain.user.exception.UserException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

/**
 * 전역 예외 처리 핸들러
 *
 * - 애플리케이션 전반의 예외를 통합 처리하여 일관된 에러 응답 제공
 * - 클라이언트와 서버 에러 구분, 보안 고려 최소한의 정보만 노출
 * - 로그 레벨 분리(WARN: 클라이언트 오류, ERROR: 서버 오류 및 예상치 못한 예외)
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ======= 비즈니스 예외 =======

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex) {
        ErrorCode code = ex.getErrorCode();
        log.warn("[비즈니스 예외] [{}] {}", code.getCode(), code.getMessage());
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.from(code, code.getStatus()));
    }

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtAuthenticationException(JwtAuthenticationException ex) {
        ErrorCode code = ex.getErrorCode();
        log.warn("[JWT 인증 예외] [{}] {}", code.getCode(), code.getMessage());
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.from(code, code.getStatus()));
    }

    @ExceptionHandler(SocialLoginException.class)
    public ResponseEntity<ApiResponse<Void>> handleSocialLoginException(SocialLoginException ex) {
        ErrorCode code = ex.getErrorCode();
        log.warn("[소셜 로그인 예외] [{}] {}", code.getCode(), code.getMessage());
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.from(code, code.getStatus()));
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserException(UserException ex) {
        ErrorCode code = ex.getErrorCode();
        log.warn("[사용자 예외] [{}] {}", code.getCode(), code.getMessage());
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.from(code, code.getStatus()));
    }


    // ======= 입력값 유효성 검사 예외 =======

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("입력값 검증에 실패했습니다.");
        log.warn("[유효성 검사 실패] {}", msg);
        
        ApiResponse<Void> response = ApiResponse.failure(ErrorCode.VALIDATION_FAIL.getCode(), 400, msg);
        log.warn("[유효성 검사 실패 응답] {}", response);
        
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException ex) {
        String msg = String.format("필수 파라미터 '%s'이(가) 누락되었습니다.", ex.getParameterName());
        log.warn("[필수 파라미터 누락] {}", msg);
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ErrorCode.MISSING_REQUEST_PARAMETER.getCode(), 400, msg));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String msg = String.format("파라미터 '%s' 값 '%s'을(를) %s 타입으로 변환할 수 없습니다.",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());
        log.warn("[타입 변환 실패] {}", msg);
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ErrorCode.INVALID_TYPE_VALUE.getCode(), 400, msg));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleJsonParseError(HttpMessageNotReadableException ex) {
        log.warn("[JSON 파싱 오류] {}", ex.getMessage());
        log.warn("[JSON 파싱 오류 상세] 원인: {}", ex.getCause() != null ? ex.getCause().getMessage() : "알 수 없음");
        
        String message = "잘못된 JSON 형식입니다.";
        if (ex.getMessage().contains("Required request body is missing")) {
            message = "요청 본문이 필요합니다.";
        }
        
        ApiResponse<Void> response = ApiResponse.failure(ErrorCode.JSON_PARSE_ERROR.getCode(), 400, message);
        log.warn("[JSON 파싱 오류 응답] {}", response);
        
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ApiResponse<Void>> handleJsonProcessingException(JsonProcessingException ex) {
        log.error("[JSON 처리 오류] {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.from(ErrorCode.JSON_PARSE_ERROR, 400));
    }

    // ======= 인증/인가 관련 예외 =======

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("[인증 실패] {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.from(ErrorCode.UNAUTHORIZED, 401));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("[잘못된 자격 증명] {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failure(ErrorCode.UNAUTHORIZED.getCode(), 401, "아이디 또는 비밀번호가 올바르지 않습니다."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("[접근 거부] {}", ex.getMessage());
        return ResponseEntity.status(ErrorCode.FORBIDDEN.getStatus())
                .body(ApiResponse.from(ErrorCode.ACCESS_DENIED, 403));
    }

    // ======= JWT 관련 예외 =======

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtException(JwtException ex) {
        log.warn("[JWT 예외] {}", ex.getMessage());

        if (ex instanceof MalformedJwtException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.from(ErrorCode.TOKEN_MALFORMED, 401));
        } else if (ex instanceof SignatureException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.from(ErrorCode.TOKEN_SIGNATURE_INVALID, 401));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.from(ErrorCode.INVALID_TOKEN, 401));
        }
    }

    // ======= HTTP 관련 예외 =======

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String msg = String.format("'%s' 메서드는 지원되지 않습니다. 지원되는 메서드: %s",
                ex.getMethod(), String.join(", ", ex.getSupportedMethods()));
        log.warn("[지원하지 않는 HTTP 메서드] {}", msg);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.failure(ErrorCode.METHOD_NOT_ALLOWED.getCode(), 405, msg));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        String msg = String.format("지원하지 않는 미디어 타입입니다: %s", ex.getContentType());
        log.warn("[지원하지 않는 미디어 타입] {}", msg);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.failure(ErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode(), 415, msg));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoHandlerFoundException ex) {
        String msg = String.format("요청한 경로를 찾을 수 없습니다: %s %s", ex.getHttpMethod(), ex.getRequestURL());
        log.warn("[API 엔드포인트 없음] {}", msg);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(ErrorCode.API_ENDPOINT_NOT_FOUND.getCode(), 404, msg));
    }

    // ======= 데이터베이스 관련 예외 =======

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("[데이터 무결성 위반] {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.from(ErrorCode.DATA_INTEGRITY_VIOLATION, 409));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateKey(DuplicateKeyException ex) {
        log.warn("[중복 키 오류] {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.from(ErrorCode.DUPLICATE_RESOURCE, 409));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiResponse<Void>> handleSQLException(SQLException ex) {
        log.error("[SQL 예외] {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.from(ErrorCode.DATABASE_ERROR, 500));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(DataAccessException ex) {
        log.error("[데이터 액세스 예외] {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.from(ErrorCode.DATABASE_ERROR, 500));
    }

    // ======= 외부 API 관련 예외 =======

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiResponse<Void>> handleRestClientException(RestClientException ex) {
        log.error("[외부 API 호출 실패] {}", ex.getMessage(), ex);

        if (ex instanceof ResourceAccessException) {
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                    .body(ApiResponse.from(ErrorCode.EXTERNAL_API_TIMEOUT, 504));
        }

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.from(ErrorCode.EXTERNAL_API_ERROR, 502));
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ApiResponse<Void>> handleTimeoutException(TimeoutException ex) {
        log.error("[요청 타임아웃] {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(ApiResponse.from(ErrorCode.GATEWAY_TIMEOUT, 504));
    }

    // ======= 일반 런타임 예외 =======

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("[잘못된 인수] {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ErrorCode.INVALID_INPUT_VALUE.getCode(), 400, ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        log.error("[잘못된 상태] {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), 500, "현재 요청을 처리할 수 없는 상태입니다."));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Void>> handleNullPointer(NullPointerException ex, HttpServletRequest req) {
        String uri = req.getRequestURI();
        log.error("[Null Pointer 예외] 요청 URI: {}", uri, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.from(ErrorCode.INTERNAL_SERVER_ERROR, 500));
    }

    // ======= 최종 전역 예외 처리 =======

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex, HttpServletRequest req) {
        String uri = req.getRequestURI();
        log.error("[예상치 못한 서버 오류] 요청 URI: {} | 예외: {}", uri, ex.getClass().getSimpleName(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.from(ErrorCode.INTERNAL_SERVER_ERROR, 500));
    }
}
