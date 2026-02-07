package com.recordmanagement.habitlog.domain.file.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

// 개발/테스트 전용 컨트롤러 - 운영 환경에서는 비활성화
// @RestController
// @RequestMapping("/api/files")
public class FileTestController {

    private final S3Client s3Client;

    public FileTestController(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    // @GetMapping("/test-connection")
    public String testS3Connection() {
        try {
            // S3 버킷 목록 조회로 연결 테스트
            ListBucketsResponse response = s3Client.listBuckets();
            return "✅ S3 연결 성공! 버킷 개수: " + response.buckets().size();
        } catch (Exception e) {
            return "❌ S3 연결 실패: " + e.getMessage();
        }
    }
}