package com.trybe.moduleapi.file.service;

import com.trybe.moduleapi.file.dto.FileMeta;
import com.trybe.modulecore.file.entity.File;
import com.trybe.modulecore.file.repository.FileRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class FileManager {
    private final FileService fileService;
    private final FileRepository fileRepository;

    public FileManager(FileService fileService, FileRepository fileRepository) {
        this.fileService = fileService;
        this.fileRepository = fileRepository;
    }

    public List<File> uploadFiles(List<MultipartFile> files, String basePath) {
        List<File> fileEntities = files.stream().map(
                file -> {
                    FileMeta meta = fileService.uploadFile(file, basePath);
                    return toFileEntity(meta, file);
                })
                .toList();

        return fileRepository.saveAll(fileEntities);
    }

    public File uploadFile(MultipartFile file, String basePath) {
        FileMeta meta = fileService.uploadFile(file, basePath);
        File fileEntity = toFileEntity(meta, file);

        return fileRepository.save(fileEntity);
    }

    public String getFileUrl(String filePath) {
        return fileService.getFileUrl(filePath);
    }

    public List<File> updateFiles(List<File> files, List<MultipartFile> newFiles, String basePath) {
        deleteFiles(files);
        return uploadFiles(newFiles, basePath);
    }

    public File updateFile(File file, MultipartFile newFile, String basePath) {
        deleteFile(file);
        return uploadFile(newFile, basePath);
    }

    public void deleteFiles(List<File> files) {
        for (File file : files) {
            fileService.deleteFile(file.getFilePath());
        }
        fileRepository.deleteAll(files);
    }

    public void deleteFile(File file) {
        fileService.deleteFile(file.getFilePath());
        fileRepository.delete(file);
    }

    private File toFileEntity(FileMeta fileMeta, MultipartFile file) {
        return File.builder()
                .originalName(fileMeta.originalName())
                .savedName(fileMeta.savedName())
                .filePath(fileMeta.filePath())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .build();
    }
}
