package com.recordmanagement.habitlog.api.file;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 개발/테스트 전용 컨트롤러 - 운영 환경에서는 비활성화
// @RestController
// @RequestMapping("/api/files")
public class FileTestController {
    
    private final AmazonS3 amazonS3;
    
    public FileTestController(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }
    
    // @GetMapping("/test-connection")
    public String testS3Connection() {
        try {
            // S3 버킷 목록 조회로 연결 테스트
            var buckets = amazonS3.listBuckets();
            return "✅ S3 연결 성공! 버킷 개수: " + buckets.size();
        } catch (Exception e) {
            return "❌ S3 연결 실패: " + e.getMessage();
        }
    }
}