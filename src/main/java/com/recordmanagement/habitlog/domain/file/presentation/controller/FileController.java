package com.recordmanagement.habitlog.domain.file.presentation.controller;

import com.recordmanagement.habitlog.global.common.response.ApiResponse;
import com.recordmanagement.habitlog.domain.file.infrastructure.service.S3FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File", description = "파일 업로드 관련 API")
public class FileController {
    
    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    
    private final S3FileService s3FileService;
    
    public FileController(S3FileService s3FileService) {
        this.s3FileService = s3FileService;
    }
    
    @Operation(summary = "파일 업로드", description = "이미지 파일을 S3에 업로드합니다 (1~3개)",
            security = @SecurityRequirement(name = "bearerAuth"))
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "파일 업로드 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = """
                {
                    "statusCode": 200,
                    "code": "S200", 
                    "message": "파일이 성공적으로 업로드되었습니다",
                    "data": {
                        "fileUrls": [
                            "https://bucket.s3.ap-northeast-2.amazonaws.com/records/images/uuid1.jpg",
                            "https://bucket.s3.ap-northeast-2.amazonaws.com/records/images/uuid2.jpg"
                        ]
                    }
                }
                """)
        )
    )
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFiles(
            @Parameter(description = "업로드할 이미지 파일들 (1-3개)", 
                      content = @Content(mediaType = "multipart/form-data"))
            @RequestParam("files") List<MultipartFile> files,
            Authentication authentication) {
        
        String userId = authentication.getName();
        log.info("파일 업로드 요청: userId=[{}], count=[{}]", userId, files != null ? files.size() : 0);
        
        // 추가 로깅 for 디버깅
        if (files != null) {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                log.info("파일[{}]: name=[{}], originalFilename=[{}], size=[{}], contentType=[{}], empty=[{}]", 
                    i, file.getName(), file.getOriginalFilename(), file.getSize(), 
                    file.getContentType(), file.isEmpty());
            }
        } else {
            log.warn("files 파라미터가 null입니다");
        }
        
        try {
            List<String> fileUrls = s3FileService.uploadMultipleFiles(files);
            FileUploadResponse response = new FileUploadResponse(fileUrls);
            
            log.info("파일 업로드 완료: userId=[{}], count=[{}], urls=[{}]", userId, fileUrls.size(), fileUrls);
            
            return ResponseEntity.ok(ApiResponse.success("파일이 성공적으로 업로드되었습니다", response));
        } catch (Exception e) {
            log.error("파일 업로드 중 예외 발생: userId=[{}]", userId, e);
            throw e;
        }
    }
    
    public record FileUploadResponse(List<String> fileUrls) {}
}