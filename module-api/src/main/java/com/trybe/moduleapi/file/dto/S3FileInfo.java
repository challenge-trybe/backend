package com.trybe.moduleapi.file.dto;

public record S3FileInfo(
        String originalName,
        String savedName,
        String filePath
) {
    public static S3FileInfo from(String originalName, String savedName, String filePath){
        return new S3FileInfo(originalName, savedName, filePath);
    }
}
