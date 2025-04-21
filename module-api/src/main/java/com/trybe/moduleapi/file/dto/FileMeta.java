package com.trybe.moduleapi.file.dto;

public record FileMeta(
    String originalName,
    String savedName,
    String filePath
) {
}