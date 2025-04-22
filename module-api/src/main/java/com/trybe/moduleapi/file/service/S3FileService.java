package com.trybe.moduleapi.file.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.trybe.moduleapi.file.dto.FileMeta;
import com.trybe.moduleapi.file.exception.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Profile("prod")
public class S3FileService implements FileService {
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final String S3baseUrl = "https://%s.s3.%s.amazonaws.com/%s";

    public S3FileService(AmazonS3Client amazonS3Client) {
        this.amazonS3Client = amazonS3Client;
    }

    @Override
    public FileMeta uploadFile(MultipartFile file, String basePath) {
        String fileName = generateFileName(file);
        String filePath = basePath + "/" + fileName;

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            amazonS3Client.putObject(bucket, filePath, file.getInputStream(), metadata);
            return new FileMeta(file.getOriginalFilename(), fileName, filePath);
        } catch (IOException e) {
            throw new FileUploadException();
        }
    }

    @Override
    public String getFileUrl(String filePath) {
        return String.format(S3baseUrl, bucket, amazonS3Client.getRegionName(), filePath);
    }

    @Override
    public void deleteFile(String filePath) {
        amazonS3Client.deleteObject(bucket, filePath);
    }

    private String generateFileName(MultipartFile file) {
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        return UUID.randomUUID() + "." + extension;
    }
}
