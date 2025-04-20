package com.trybe.moduleapi.file.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class FileUploadException extends BusinessException {
    public FileUploadException() {
        super("파일 업로드 실패", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
