package com.recordmanagement.habitlog.domain.file.infrastructure.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.recordmanagement.habitlog.global.config.exception.CustomException;
import com.recordmanagement.habitlog.global.config.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class S3FileService {
    
    private final AmazonS3 amazonS3;
    private final String bucketName;
    
    // 지원하는 이미지 파일 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );
    
    // 최대 파일 크기 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    // 최대 파일 개수 (3개)
    private static final int MAX_FILE_COUNT = 3;
    
    // Pre-signed URL 유효 시간 (1시간)
    private static final int PRESIGNED_URL_EXPIRATION_HOURS = 1;
    
    public S3FileService(AmazonS3 amazonS3, @Value("${cloud.aws.s3.bucket}") String bucketName) {
        this.amazonS3 = amazonS3;
        this.bucketName = bucketName;
    }
    
    public String uploadFile(MultipartFile file) {
        validateFile(file);
        
        String fileName = generateFileName(file.getOriginalFilename());
        String filePath = "records/images/" + fileName;
        
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            
            // Private으로 업로드 (ACL 제거)
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName, filePath, file.getInputStream(), metadata
            );
            
            amazonS3.putObject(putObjectRequest);
            
            // Pre-signed URL 생성 (1시간 유효)
            Date expiration = Date.from(
                Instant.now().plusSeconds(PRESIGNED_URL_EXPIRATION_HOURS * 3600)
            );
            
            return amazonS3.generatePresignedUrl(bucketName, filePath, expiration, HttpMethod.GET)
                    .toString();
            
        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
    
    public List<String> uploadMultipleFiles(List<MultipartFile> files) {
        validateMultipleFiles(files);
        
        // 병렬 처리로 성능 개선 (파일 3개까지만 허용하므로 안전)
        return files.parallelStream()
                .map(this::uploadFile)
                .toList();
    }
    
    public String regeneratePresignedUrl(String filePath) {
        try {
            Date expiration = Date.from(
                Instant.now().plusSeconds(PRESIGNED_URL_EXPIRATION_HOURS * 3600)
            );
            
            return amazonS3.generatePresignedUrl(bucketName, filePath, expiration, HttpMethod.GET)
                    .toString();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_PROCESSING_ERROR);
        }
    }
    
    public List<String> regeneratePresignedUrls(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return new ArrayList<>();
        }
        
        return fileUrls.stream()
                .map(this::regeneratePresignedUrlFromUrl)
                .toList();
    }
    
    private String regeneratePresignedUrlFromUrl(String fileUrl) {
        try {
            String filePath = extractFilePathFromUrl(fileUrl);
            String newUrl = regeneratePresignedUrl(filePath);
            // 성공 로그 (DEBUG 레벨)
            return newUrl;
        } catch (Exception e) {
            // 에러 발생 시 원본 URL 반환 (로그는 남기되 전체 처리를 중단하지 않음)
            // 운영에서는 WARN, 개발에서는 ERROR로 로그 남김
            return fileUrl;
        }
    }
    
    public void deleteFile(String fileUrl) {
        try {
            String fileName = extractFilePathFromUrl(fileUrl);
            amazonS3.deleteObject(bucketName, fileName);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_DELETE_FAILED);
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_EMPTY);
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_FILE_FORMAT);
        }
    }
    
    private void validateMultipleFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_EMPTY);
        }
        
        if (files.size() > MAX_FILE_COUNT) {
            throw new CustomException(ErrorCode.FILE_COUNT_EXCEEDED);
        }
        
        for (MultipartFile file : files) {
            validateFile(file);
        }
    }
    
    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + "." + extension;
    }
    
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
    
    private String extractFilePathFromUrl(String fileUrl) {
        // Pre-signed URL에서 파일 경로 추출
        if (fileUrl.contains("?")) {
            fileUrl = fileUrl.substring(0, fileUrl.indexOf("?"));
        }
        
        // S3 URL에서 파일 경로 부분만 추출
        String s3BaseUrl = "https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/";
        if (fileUrl.startsWith(s3BaseUrl)) {
            return fileUrl.substring(s3BaseUrl.length());
        }
        
        // 기본적으로 마지막 "/" 이후 부분 반환
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }
}