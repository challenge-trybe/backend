package com.trybe.modulecore.file.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File {
    @Builder
    public File(String originalName, String savedName, String filePath, String fileType, Long fileSize) {
        this.originalName = originalName;
        this.savedName = savedName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "original_name", nullable = false, updatable = false)
    private String originalName;

    @Column(name = "saved_name", nullable = false, updatable = false)
    private String savedName;

    @Column(name = "file_path", nullable = false, updatable = false)
    private String filePath;

    @Column(name = "file_type", nullable = false, updatable = false)
    private String fileType;

    @Column(name = "file_size", nullable = false, updatable = false)
    private Long fileSize;
}
