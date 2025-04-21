package com.trybe.moduleapi.file.service;

import com.trybe.moduleapi.file.dto.FileMeta;
import com.trybe.moduleapi.file.exception.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Profile("dev")
public class LocalFileService implements FileService {
    @Value("${file-storage.location}")
    private String fileStorageLocation;

    @Override
    public FileMeta uploadFile(MultipartFile file, String basePath) {
        createDirIfNotExists(fileStorageLocation + basePath);

        String originalName = file.getOriginalFilename();
        String savedName = generateFileName(file);
        String filePath = basePath + "/" + savedName;

        try {
            file.transferTo(new java.io.File(getFileUrl(filePath)));
            return new FileMeta(originalName, savedName, filePath);
        } catch (Exception e) {
            throw new FileUploadException();
        }
    }

    @Override
    public String getFileUrl(String filePath) {
        return fileStorageLocation + filePath;
    }

    @Override
    public void deleteFile(String filePath) {
        java.io.File file = new java.io.File(getFileUrl(filePath));

        if (file.exists()) {
            boolean isDeleted = file.delete();
            if (!isDeleted) {
                // TODO: 단순 출력에서 로깅으로 변경
                System.out.println("파일 삭제 실패: " + filePath);
            }
        } else {
            System.out.println("파일이 존재하지 않음: " + filePath);
        }
    }

    private String generateFileName(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalName);
        return UUID.randomUUID() + "-" + originalName + "." + extension;
    }

    private void createDirIfNotExists(String dirPath) {
        java.io.File file = new java.io.File(dirPath);

        if (!file.exists()) {
            boolean isCreated = file.mkdirs();
            if (!isCreated) {
                // TODO: 단순 출력에서 로깅으로 변경
                System.out.println("디렉토리 생성 실패: " + dirPath);
            }
        }
    }
}
