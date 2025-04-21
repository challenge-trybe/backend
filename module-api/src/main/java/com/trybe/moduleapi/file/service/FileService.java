package com.trybe.moduleapi.file.service;

import com.trybe.moduleapi.file.dto.FileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    List<FileResponse> uploadFiles(List<MultipartFile> files, String basePath);

    String getFileUrl(String filePath);

    List<FileResponse> updateFiles(List<String> filePaths, List<MultipartFile> newFiles, String basePath);

    void deleteFile(String filePath);
}
