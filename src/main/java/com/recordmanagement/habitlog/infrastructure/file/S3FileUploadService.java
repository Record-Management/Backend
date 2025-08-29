package com.recordmanagement.habitlog.infrastructure.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

//@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileUploadService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * 이미지 파일을 S3에 업로드
     */
    public String uploadImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
        }

        // 이미지 파일 확장자 검증
        String fileExtension = getFileExtension(originalFilename);
        if (!isImageFile(fileExtension)) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        String fileName = generateFileName(fileExtension);
        
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(bucketName, fileName, file.getInputStream(), metadata);
            
            String fileUrl = amazonS3.getUrl(bucketName, fileName).toString();
            log.info("파일 업로드 성공: {}", fileUrl);
            
            return fileUrl;
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", e.getMessage());
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * 이미지 파일인지 검증
     */
    private boolean isImageFile(String extension) {
        return extension.matches("(?i)\\.(jpg|jpeg|png|gif|bmp|webp)$");
    }

    /**
     * 고유한 파일명 생성
     */
    private String generateFileName(String extension) {
        return "images/" + UUID.randomUUID().toString() + extension;
    }

    /**
     * S3에서 파일 삭제
     */
    public void deleteFile(String fileUrl) {
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            amazonS3.deleteObject(bucketName, fileName);
            log.info("파일 삭제 성공: {}", fileName);
        } catch (Exception e) {
            log.error("파일 삭제 실패: {}", e.getMessage());
        }
    }

    /**
     * URL에서 파일명 추출
     */
    private String extractFileNameFromUrl(String fileUrl) {
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }
}