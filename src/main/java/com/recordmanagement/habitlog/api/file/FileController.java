package com.recordmanagement.habitlog.api.file;

import com.recordmanagement.habitlog.common.response.ApiResponse;
import com.recordmanagement.habitlog.infrastructure.file.service.S3FileService;
import io.swagger.v3.oas.annotations.Operation;
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
            security = @SecurityRequirement(name = "Bearer Authentication"))
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
            @RequestParam("files") List<MultipartFile> files,
            Authentication authentication) {
        
        String userId = authentication.getName();
        log.info("파일 업로드 요청: userId=[{}], count=[{}]", userId, files.size());
        
        List<String> fileUrls = s3FileService.uploadMultipleFiles(files);
        FileUploadResponse response = new FileUploadResponse(fileUrls);
        
        log.info("파일 업로드 완료: userId=[{}], count=[{}]", userId, fileUrls.size());
        
        return ResponseEntity.ok(ApiResponse.success("파일이 성공적으로 업로드되었습니다", response));
    }
    
    public record FileUploadResponse(List<String> fileUrls) {}
}