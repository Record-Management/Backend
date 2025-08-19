package com.recordmanagement.habitlog.api.file;

import com.recordmanagement.habitlog.infrastructure.file.S3FileUploadService;
import com.recordmanagement.habitlog.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드 관련 API 컨트롤러
 * 
 * AWS S3를 활용한 파일 업로드 기능을 제공합니다.
 * 주로 일상 기록의 이미지 첨부 용도로 사용됩니다.
 * 
 * 주요 기능:
 * - 이미지 파일 S3 업로드
 * - 업로드된 파일의 공개 URL 반환
 * - 파일 형식 및 크기 검증
 * 
 * 지원하는 파일 형식:
 * - JPEG (.jpg, .jpeg)
 * - PNG (.png)
 * - GIF (.gif)
 * - WebP (.webp)
 * 
 * 파일 제한사항:
 * - 최대 파일 크기: 10MB
 * - 이미지 파일만 업로드 가능
 * - 파일명은 UUID로 자동 생성하여 중복 방지
 * 
 * S3 설정:
 * - 버킷: application.yml에서 설정
 * - 리전: ap-northeast-2 (서울)
 * - 공개 읽기 권한으로 업로드
 * 
 * 보안 고려사항:
 * - 파일 확장자 검증으로 악성 파일 차단
 * - 파일 크기 제한으로 서버 자원 보호
 * - 원본 파일명 숨김으로 보안 강화
 * 
 * @author 전우선
 * @since 2025.08.19
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "파일 업로드", description = "파일 업로드 관련 API")
public class FileController {

    private final S3FileUploadService s3FileUploadService;

    @Operation(summary = "이미지 파일 업로드", description = "이미지 파일을 S3에 업로드하고 URL을 반환합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "업로드 성공",
                    content = @Content(schema = @Schema(implementation = FileUploadResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", 
                    description = "잘못된 파일 형식"
            )
    })
    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileUploadResponse> uploadImage(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestParam("file") MultipartFile file) {
        
        log.info("이미지 파일 업로드 요청: {}", file.getOriginalFilename());
        
        String fileUrl = s3FileUploadService.uploadImageFile(file);
        
        FileUploadResponse response = FileUploadResponse.builder()
                .fileUrl(fileUrl)
                .originalFilename(file.getOriginalFilename())
                .fileSize(file.getSize())
                .build();
                
        return ApiResponse.success(response);
    }

    @Schema(description = "파일 업로드 응답")
    public static class FileUploadResponse {
        @Schema(description = "업로드된 파일 URL")
        private final String fileUrl;
        
        @Schema(description = "원본 파일명")
        private final String originalFilename;
        
        @Schema(description = "파일 크기")
        private final Long fileSize;

        @lombok.Builder
        public FileUploadResponse(String fileUrl, String originalFilename, Long fileSize) {
            this.fileUrl = fileUrl;
            this.originalFilename = originalFilename;
            this.fileSize = fileSize;
        }

        public String getFileUrl() {
            return fileUrl;
        }

        public String getOriginalFilename() {
            return originalFilename;
        }

        public Long getFileSize() {
            return fileSize;
        }
    }
}