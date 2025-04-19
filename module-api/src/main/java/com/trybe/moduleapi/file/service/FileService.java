package com.trybe.moduleapi.file.service;

import com.trybe.moduleapi.file.dto.S3FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    List<S3FileInfo> uploadImages(List<MultipartFile> files, String basePath);

    String getImageUrl(String filePath);

    List<S3FileInfo> updateImages(List<String> filePaths, List<MultipartFile> newImages, String basePath);

    void deleteImage(String filePath);
}
