package com.trybe.moduleapi.file.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.trybe.moduleapi.file.dto.S3FileInfo;
import com.trybe.moduleapi.file.exception.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class S3Service implements FileService {
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final String S3baseUrl = "https://%s.s3.%s.amazonaws.com/%s";

    public S3Service(AmazonS3Client amazonS3Client) {
        this.amazonS3Client = amazonS3Client;
    }

    // 이미지 저장
    @Override
    public List<S3FileInfo> uploadImages(List<MultipartFile> files, String basePath) {
        List<S3FileInfo> S3fileInfos = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = generateFileName(file);
            String filePath = basePath + "/" + fileName;

            try {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(file.getSize());
                metadata.setContentType(file.getContentType());
                amazonS3Client.putObject(bucket, filePath, file.getInputStream(), metadata);
                S3FileInfo s3FileInfo = S3FileInfo.from(file.getOriginalFilename(), fileName, filePath);
                S3fileInfos.add(s3FileInfo);
            } catch (IOException e) {
                throw new FileUploadException();
            }
        }
        return S3fileInfos;
    }

    // 이미지 URL 조회
    @Override
    public String getImageUrl(String filePath) {
        // filePath = basePath + "/" + savedName
        return String.format(S3baseUrl, bucket, amazonS3Client.getRegionName(), filePath);
    }

    @Override
    public List<S3FileInfo> updateImages(List<String> filePaths, List<MultipartFile> newImages, String basePath) {
        for (String filePath : filePaths) {
            deleteImage(filePath);
        }
        return uploadImages(newImages, basePath);
    }

    // 이미지 삭제
    @Override
    public void deleteImage(String filePath) {
        amazonS3Client.deleteObject(bucket, filePath);
    }

    private String generateFileName(MultipartFile file) {
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        return UUID.randomUUID() + "." + extension;
    }
}
