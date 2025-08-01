package com.recordmanagement.habitlog.common.response;

import com.recordmanagement.habitlog.config.exception.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 모든 API 응답을 감싸는 공통 응답 포맷
 *
 * 성공 여부, 응답 코드, 메시지, 실제 데이터를 포함하여
 * 프론트엔드에서 일관된 응답 구조로 처리할 수 있도록 합니다.
 *
 * @param <T> 응답 데이터 타입
 * @author 전우선
 * @since 2025.07.30
 * @version 1.0.0
 */
@Getter
@Schema(
    description = """
        ## 공통 API 응답 포맷
        
        모든 API 엔드포인트는 이 형태의 일관된 응답을 반환합니다.
        
        ### 응답 구조
        - **success**: 요청 성공 여부 (boolean)
        - **code**: 상세 응답 코드 (string)  
        - **message**: 사용자 친화적 메시지 (string)
        - **data**: 실제 응답 데이터 (generic type)
        
        ### 코드 체계
        - **S200**: 일반적인 성공
        - **S201**: 리소스 생성 성공
        - **E400XX**: 클라이언트 요청 오류
        - **E500XX**: 서버 내부 오류
        """,
    example = """
        {
          "success": true,
          "code": "S200",
          "message": "요청이 성공적으로 처리되었습니다.",
          "data": {
            "id": "123",
            "name": "example"
          }
        }
        """
)
public class ApiResponse<T> {

    @Schema(
        description = """
            요청 처리 성공 여부
            
            - **true**: 요청이 성공적으로 처리됨
            - **false**: 요청 처리 중 오류 발생
            
            클라이언트는 이 값을 먼저 확인하여 성공/실패를 판단해야 합니다.
            """,
        example = "true",
        required = true
    )
    private final boolean success;

    @Schema(
        description = """
            상세 응답 코드
            
            ### 성공 코드 (S로 시작)
            - **S200**: 일반적인 성공
            - **S201**: 리소스 생성 성공
            - **S204**: 성공 (응답 데이터 없음)
            
            ### 오류 코드 (E로 시작)  
            - **E40001~E40099**: 인증/인가 오류
            - **E40101~E40199**: 요청 데이터 오류
            - **E40201~E40299**: 비즈니스 로직 오류
            - **E50001~E50099**: 서버 내부 오류
            """,
        example = "S200",
        required = true
    )
    private final String code;

    @Schema(
        description = """
            사용자 친화적 응답 메시지
            
            ### 용도
            - 사용자에게 직접 표시 가능한 메시지
            - Toast 메시지나 알림창에 활용
            - 다국어 지원 고려하여 작성
            
            ### 메시지 유형
            - **성공**: "요청이 성공적으로 처리되었습니다."
            - **오류**: "입력값을 확인해주세요.", "서버 오류가 발생했습니다."
            """,
        example = "요청이 성공적으로 처리되었습니다.",
        required = true
    )
    private final String message;

    @Schema(
        description = """
            실제 응답 데이터
            
            ### 데이터 형태
            - **객체**: 단일 리소스 응답
            - **배열**: 목록 형태 응답  
            - **null**: 데이터가 없는 성공 응답 또는 오류 응답
            
            ### 타입별 예시
            - **User 객체**: {"id": "123", "name": "홍길동"}
            - **목록**: [{"id": "1"}, {"id": "2"}]
            - **빈 응답**: null
            """,
        nullable = true
    )
    private final T data;

    /**
     * 생성자 (Lombok @Builder 사용)
     *
     * @param success 성공 여부
     * @param code 응답 코드
     * @param message 응답 메시지
     * @param data 응답 데이터
     */
    @Builder
    public ApiResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 기본 성공 응답 생성
     *
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return 성공 ApiResponse 객체
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "S200", "요청이 성공적으로 처리되었습니다.", data);
    }

    /**
     * 데이터 없는 성공 응답 생성
     *
     * @return 성공 ApiResponse 객체
     */
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, "S200", "요청이 성공적으로 처리되었습니다.", null);
    }

    /**
     * 커스텀 메시지 포함 성공 응답 생성
     *
     * @param message 응답 메시지
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return 성공 ApiResponse 객체
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "S200", message, data);
    }

    /**
     * 실패 응답 생성
     *
     * @param code 실패 코드
     * @param message 실패 메시지
     * @param <T> 데이터 타입
     * @return 실패 ApiResponse 객체
     */
    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    /**
     * ErrorCode 객체 기반 실패 응답 생성
     *
     * @param errorCode 에러 코드 객체
     * @param <T> 데이터 타입
     * @return 실패 ApiResponse 객체
     */
    public static <T> ApiResponse<T> from(ErrorCode errorCode) {
        return failure(errorCode.getCode(), errorCode.getMessage());
    }
}