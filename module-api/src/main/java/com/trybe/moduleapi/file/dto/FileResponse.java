package com.trybe.moduleapi.file.dto;

public record FileResponse(
        String originalName,
        String filePath
) {
    public static FileResponse from(String originalName, String filePath){
        return new FileResponse(originalName, filePath);
    }
}
