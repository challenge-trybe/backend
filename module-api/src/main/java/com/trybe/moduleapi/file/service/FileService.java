package com.trybe.moduleapi.file.service;

import com.trybe.moduleapi.file.dto.FileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    List<FileResponse> uploadImages(List<MultipartFile> files, String basePath);

    String getImageUrl(String filePath);

    List<FileResponse> updateImages(List<String> filePaths, List<MultipartFile> newImages, String basePath);

    void deleteImage(String filePath);
}
