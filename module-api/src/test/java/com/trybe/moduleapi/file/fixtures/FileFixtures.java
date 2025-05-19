package com.trybe.moduleapi.file.fixtures;

import com.trybe.moduleapi.file.dto.FileResponse;
import com.trybe.moduleapi.file.dto.FileWithIdResponse;
import com.trybe.moduleapi.proof.fixtures.ProofHistoryFixtures;
import com.trybe.modulecore.file.entity.File;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.UUID;

public class FileFixtures {
    public static final Long 파일_ID = 1L;
    public static final String 파일_원본_이름 = "originalName.jpeg";
    public static final String 파일_저장_이름 = UUID.randomUUID().toString() + "-" + 파일_원본_이름;
    public static final String 파일_경로 = "/filePath/" + 파일_저장_이름;
    public static final String 파일_타입 = "image/jpeg";
    public static final Long 파일_사이즈 = 10240L;

    public static final String 파일_저장소_경로 = "http://trybe.test/file";
    public static final String 파일_전체_경로 = 파일_저장소_경로 + 파일_경로;

    /* Request */
    public static MockMultipartFile 파일_요청_생성(String parameterName) {
        return new MockMultipartFile(
                parameterName,
                파일_원본_이름,
                파일_타입,
                "dummy content".getBytes()
        );
    }

    /* Entity */
    public static final File 파일 = File.builder()
            .originalName(파일_원본_이름)
            .savedName(파일_저장_이름)
            .filePath(파일_경로)
            .fileType(파일_타입)
            .fileSize(파일_사이즈)
            .build();

    /* Response DTO */
    public static final FileResponse 파일_응답 = new FileResponse(파일_원본_이름, 파일_전체_경로);
    public static final FileWithIdResponse 아이디_포함_파일_응답 = FileWithIdResponse.from(ProofHistoryFixtures.인증_기록_ID, 파일, 파일_전체_경로);
    public static final List<FileWithIdResponse> 아이디_포함_파일_목록_응답 = List.of(아이디_포함_파일_응답);
}
