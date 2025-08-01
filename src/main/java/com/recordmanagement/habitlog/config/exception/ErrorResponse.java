package com.recordmanagement.habitlog.config.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 예외 발생 시 클라이언트에 반환되는 에러 응답 DTO
 *
 * HTTP 상태 코드, 에러 코드, 메시지, 발생 시간 정보를 포함합니다.
 * 불변(immutable) 객체로 설계되어 동시성 문제를 방지하며,
 * 정적 팩토리 메서드 {@code of()}를 통해 인스턴스를 생성합니다.
 *
 * Swagger(OpenAPI) 자동 문서화를 위해 {@link Schema} 어노테이션을 활용합니다.
 *
 * <pre>
 * {@code
 * // 예시: ErrorCode를 통해 에러 응답 생성
 * return ResponseEntity.status(errorCode.getStatus())
 *                      .body(ErrorResponse.of(errorCode));
 * }
 * </pre>
 *
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
@Schema(
    description = """
        ## API 에러 응답 포맷
        
        API 호출 중 오류 발생 시 반환되는 표준 에러 응답입니다.
        
        ### 응답 구조
        - **status**: HTTP 상태 코드
        - **code**: 상세 에러 코드
        - **message**: 사용자 친화적 에러 메시지
        - **timestamp**: 오류 발생 시각
        
        ### 에러 코드 체계
        - **E400XX**: 클라이언트 요청 오류 (잘못된 파라미터, 유효성 검증 실패 등)
        - **E401XX**: 인증 오류 (토큰 만료, 권한 없음 등)
        - **E404XX**: 리소스 없음 (존재하지 않는 사용자, 잘못된 URL 등)
        - **E500XX**: 서버 내부 오류 (데이터베이스 오류, 외부 API 오류 등)
        """,
    example = """
        {
          "status": 400,
          "code": "E40001",
          "message": "입력값이 유효하지 않습니다.",
          "timestamp": "2024-07-30T13:45:30.123"
        }
        """
)
public class ErrorResponse {

    @Schema(
        description = """
            HTTP 상태 코드
            
            ### 주요 상태 코드
            - **400**: Bad Request (잘못된 요청)
            - **401**: Unauthorized (인증 실패)
            - **403**: Forbidden (권한 없음)
            - **404**: Not Found (리소스 없음)
            - **409**: Conflict (데이터 충돌)
            - **422**: Unprocessable Entity (처리 불가능한 요청)
            - **500**: Internal Server Error (서버 내부 오류)
            - **502**: Bad Gateway (외부 서비스 오류)
            - **503**: Service Unavailable (서비스 일시 중단)
            """,
        example = "400",
        minimum = "400",
        maximum = "599"
    )
    private final int status;

    @Schema(
        description = """
            상세 에러 코드
            
            ### 코드 형식
            - E + HTTP상태코드 + 순번 (예: E40001, E50102)
            
            ### 카테고리별 코드
            
            #### 인증 관련 (E401XX)
            - **E40101**: 액세스 토큰 만료
            - **E40102**: 리프레시 토큰 만료
            - **E40103**: 유효하지 않은 토큰
            
            #### 요청 데이터 오류 (E400XX)
            - **E40001**: 필수 파라미터 누락
            - **E40002**: 잘못된 데이터 형식
            - **E40003**: 지원하지 않는 소셜 로그인 타입
            
            #### 서버 오류 (E500XX)
            - **E50001**: 데이터베이스 연결 오류
            - **E50002**: 외부 API 호출 실패
            """,
        example = "E40001",
        pattern = "^E[45]\\d{4}$"
    )
    private final String code;

    @Schema(
        description = """
            사용자 친화적 에러 메시지
            
            ### 메시지 특징
            - 사용자에게 직접 표시 가능한 내용
            - 기술적 세부사항은 제외
            - 해결 방법이나 다음 액션 가이드 포함
            
            ### 메시지 예시
            - **성공적인 안내**: "입력값을 확인해주세요."
            - **인증 오류**: "로그인이 필요합니다."
            - **권한 오류**: "접근 권한이 없습니다."
            - **서버 오류**: "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            """,
        example = "입력값이 유효하지 않습니다.",
        maxLength = 500
    )
    private final String message;

    @Schema(
        description = """
            에러 발생 시각 (서버 시간 기준)
            
            ### 시간 형식
            - ISO 8601 표준 (yyyy-MM-dd'T'HH:mm:ss.SSS)
            - 서버 타임존 기준 (일반적으로 UTC 또는 KST)
            
            ### 활용 방안
            - 에러 로그 추적 및 디버깅
            - 사용자 문의 시 정확한 오류 발생 시점 확인
            - 에러 패턴 분석 및 모니터링
            """,
        example = "2024-07-30T13:45:30.123",
        type = "string",
        format = "date-time"
    )
    private final LocalDateTime timestamp;

    /**
     * private 생성자 - 인스턴스 생성을 정적 팩토리 메서드로 제한하여 통제된 생성 보장
     *
     * @param status HTTP 상태 코드
     * @param code 에러 코드 문자열
     * @param message 사용자에게 전달할 에러 메시지
     * @param timestamp 에러 발생 시간 (서버 시간)
     */
    private ErrorResponse(int status, String code, String message, LocalDateTime timestamp) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
    }

    /**
     * ErrorCode enum 기반으로 ErrorResponse 생성
     *
     * @param errorCode ErrorCode 열거형
     * @return 에러 응답 객체
     */
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getStatus(),
                errorCode.getCode(),
                errorCode.getMessage(),
                LocalDateTime.now()
        );
    }

    /**
     * ErrorCode와 커스텀 메시지를 이용하여 ErrorResponse 생성
     *
     * @param errorCode ErrorCode 열거형
     * @param message 사용자 정의 에러 메시지
     * @return 에러 응답 객체
     */
    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(
                errorCode.getStatus(),
                errorCode.getCode(),
                message,
                LocalDateTime.now()
        );
    }
}