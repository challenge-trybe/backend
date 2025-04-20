package com.trybe.moduleapi.file.dto;

public record FileResponse(
        String originalName,
        String savedName,
        String filePath
) {
    public static FileResponse from(String originalName, String savedName, String filePath){
        return new FileResponse(originalName, savedName, filePath);
    }
}
