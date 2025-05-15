package com.trybe.moduleapi.file.dto;

import com.trybe.modulecore.file.entity.File;

public record FileWithIdResponse (
        Long id,
        String originalName,
        String filePath
) {
    public static FileWithIdResponse from(File file, String path) {
        return new FileWithIdResponse(file.getId(), file.getOriginalName(), path);
    }
}