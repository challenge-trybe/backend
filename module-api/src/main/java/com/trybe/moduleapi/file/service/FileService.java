package com.trybe.moduleapi.file.service;

import com.trybe.moduleapi.file.dto.FileMeta;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileMeta uploadFile(MultipartFile file, String basePath);
    String getFileUrl(String filePath);
    void deleteFile(String filePath);
}
