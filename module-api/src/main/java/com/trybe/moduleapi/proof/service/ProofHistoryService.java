package com.trybe.moduleapi.proof.service;

import com.trybe.moduleapi.challenge.exception.participation.InvalidParticipationStatusActionException;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.file.dto.FileWithIdResponse;
import com.trybe.moduleapi.file.service.FileManager;
import com.trybe.moduleapi.proof.dto.request.ProofHistoryRequest;
import com.trybe.moduleapi.proof.dto.response.ProofHistoryResponse;
import com.trybe.moduleapi.proof.exception.*;
import com.trybe.moduleapi.proof.exception.history.DuplicatedProofHistoryException;
import com.trybe.moduleapi.proof.exception.history.ForbiddenProofHistoryException;
import com.trybe.moduleapi.proof.exception.history.InvalidProofHistoryStatusException;
import com.trybe.moduleapi.proof.exception.history.NotFoundProofHistoryException;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.file.entity.File;
import com.trybe.modulecore.proof.entity.Proof;
import com.trybe.modulecore.proof.entity.ProofHistory;
import com.trybe.modulecore.proof.entity.ProofHistoryFile;
import com.trybe.modulecore.proof.enums.ProofHistoryStatus;
import com.trybe.modulecore.proof.repository.ProofHistoryFileRepository;
import com.trybe.modulecore.proof.repository.ProofHistoryRepository;
import com.trybe.modulecore.proof.repository.ProofRepository;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProofHistoryService {
    private final ProofHistoryRepository proofHistoryRepository;
    private final ProofHistoryFileRepository proofHistoryFileRepository;
    private final ProofRepository proofRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final FileManager fileManager;

    public ProofHistoryService(ProofHistoryRepository proofHistoryRepository, ProofHistoryFileRepository proofHistoryFileRepository, ProofRepository proofRepository, ChallengeParticipationRepository challengeParticipationRepository, FileManager fileManager) {
        this.proofHistoryRepository = proofHistoryRepository;
        this.proofHistoryFileRepository = proofHistoryFileRepository;
        this.proofRepository = proofRepository;
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.fileManager = fileManager;
    }

    private static final String FILE_BASE_PATH_FORMAT = "/challenge/%d/proofhistory/%d";

    @Transactional
    public ProofHistoryResponse.Summary save(User user, Long proofId, List<MultipartFile> files, ProofHistoryRequest.Create request) {
        Proof proof = getProof(proofId);
        Long challengeId = proof.getChallenge().getId();

        validateMemberParticipation(user.getId(), challengeId, "멤버만 인증 기록을 등록할 수 있습니다.");
        validateDate(proof);
        validateDuplicateProofHistory(proof.getId(), user.getId());

        ProofHistory savedProofHistory = proofHistoryRepository.save(request.toEntity(proof, user, request.content()));
        List<ProofHistoryFile> savedFiles = saveFiles(savedProofHistory, files);

        return ProofHistoryResponse.Summary.from(savedProofHistory, toFileResponses(savedFiles));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProofHistoryResponse.Summary> findAll(User user, Long proofId, Pageable pageable) {
        Proof proof = getProof(proofId);

        validateMemberParticipation(user.getId(), proof.getChallenge().getId(), "멤버만 인증 기록을 조회할 수 있습니다.");

        Page<ProofHistory> proofHistories = proofHistoryRepository.findAllByProofId(proofId, pageable);

        Page<ProofHistoryResponse.Summary> responses = proofHistories.map(proofHistory -> {
            List<ProofHistoryFile> files = getFiles(proofHistory.getId());
            List<FileWithIdResponse> fileResponses = toFileResponses(files);
            return ProofHistoryResponse.Summary.from(proofHistory, fileResponses);
        });

        return new PageResponse<>(responses);
    }

    @Transactional
    public ProofHistoryResponse.Summary update(User user, Long proofHistoryId, List<MultipartFile> files, ProofHistoryRequest.Update request) {
        ProofHistory proofHistory = getProofHistory(proofHistoryId);

        validateProofHistoryOwner(user, true, proofHistory, "인증 기록의 작성자만 수정할 수 있습니다.");
        validateProofHistoryStatus(proofHistory, ProofHistoryStatus.PENDING, "이미 처리된 인증 기록은 수정할 수 없습니다.");

        proofHistory.updateContent(request.content());
        List<ProofHistoryFile> savedFiles = updateFiles(proofHistory, request.fileOrder(), files);

        return ProofHistoryResponse.Summary.from(proofHistory, toFileResponses(savedFiles));
    }

    @Transactional
    public void delete(User user, Long proofHistoryId) {
        ProofHistory proofHistory = getProofHistory(proofHistoryId);

        validateProofHistoryOwner(user, true, proofHistory, "인증 기록의 작성자만 삭제할 수 있습니다.");

        proofHistoryRepository.delete(proofHistory);
    }

    private Proof getProof(Long proofId) {
        return proofRepository.findById(proofId)
                .orElseThrow(NotFoundProofException::new);
    }

    private ProofHistory getProofHistory(Long proofHistoryId) {
        return proofHistoryRepository.findById(proofHistoryId)
                .orElseThrow(NotFoundProofHistoryException::new);
    }

    private String getBasePath(ProofHistory proofHistory) {
        Long challengeId = proofHistory.getProof().getChallenge().getId();
        Long proofHistoryId = proofHistory.getId();

        return String.format(FILE_BASE_PATH_FORMAT, challengeId, proofHistoryId);
    }

    private List<ProofHistoryFile> getFiles(Long proofHistoryId) {
        return proofHistoryFileRepository.findAllByProofHistoryIdOrderByFileOrder(proofHistoryId);
    }

    private void validateMemberParticipation(Long userId, Long challengeId, String message) {
        if (!challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(userId, challengeId, ParticipationStatus.ACCEPTED)) {
            throw new InvalidParticipationStatusActionException(message);
        }
    }

    private void validateProofHistoryOwner(User user, boolean shouldBe, ProofHistory proofHistory, String message) {
        if ((user.getId() != proofHistory.getUser().getId()) == shouldBe) {
            throw new ForbiddenProofHistoryException(message);
        }
    }

    private void validateDate(Proof proof) {
        if (!proof.getDate().equals(LocalDate.now())) {
            throw new InvalidProofDateException();
        }
    }

    private void validateProofHistoryStatus(ProofHistory proofHistory, ProofHistoryStatus status, String message) {
        if (proofHistory.getStatus().isNot(status)) {
            throw new InvalidProofHistoryStatusException(message);
        }
    }

    private void validateDuplicateProofHistory(Long proofId, Long userId) {
        if (proofHistoryRepository.existsByProofIdAndUserId(proofId, userId)) {
            throw new DuplicatedProofHistoryException();
        }
    }

    private List<ProofHistoryFile> saveFiles(ProofHistory proofHistory, List<MultipartFile> files) {
        List<File> uploadedFiles = fileManager.uploadFiles(files, getBasePath(proofHistory));
        List<ProofHistoryFile> fileEntities = new ArrayList<>();

        int order = 1;

        for (File file : uploadedFiles) {
            fileEntities.add(new ProofHistoryFile(proofHistory, file, order++));
        }

        return proofHistoryFileRepository.saveAll(fileEntities);
    }

    private List<ProofHistoryFile> updateFiles(ProofHistory proofHistory, List<Long> fileOrder, List<MultipartFile> newFiles) {
        List<ProofHistoryFile> files = getFiles(proofHistory.getId());
        Map<Long, ProofHistoryFile> existingFile = files.stream()
                .collect(Collectors.toMap(ProofHistoryFile::getId, file -> file));

        Set<Long> remainingIds = fileOrder.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (ProofHistoryFile proofHistoryFile: new ArrayList<>(files)) {
            if (!remainingIds.contains(proofHistoryFile.getId())) {
                File file = proofHistoryFile.getFile();
                proofHistoryFileRepository.delete(proofHistoryFile);
                fileManager.deleteFile(file);
            }
        }

        List<File> uploadedFiles = fileManager.uploadFiles(newFiles, getBasePath(proofHistory));
        Iterator<File> iterator = uploadedFiles.iterator();

        List<ProofHistoryFile> fileEntities = new ArrayList<>();
        int order = 1;

        for (Long fileId : fileOrder) {
            if (fileId != null) {
                ProofHistoryFile file = existingFile.get(fileId);
                if (file != null && file.getFileOrder() != order) {
                    file.updateFileOrder(order++);
                }
                fileEntities.add(file);
            } else {
                if (iterator.hasNext()) {
                    File file = iterator.next();
                    ProofHistoryFile proofHistoryFile =proofHistoryFileRepository.save(new ProofHistoryFile(proofHistory, file, order++));
                    fileEntities.add(proofHistoryFile);
                }
            }
        }

        return fileEntities;
    }

    private List<FileWithIdResponse> toFileResponses(List<ProofHistoryFile> files) {
        return files.stream()
                .map(proofHistoryFile -> {
                    File file = proofHistoryFile.getFile();
                    return FileWithIdResponse.from(proofHistoryFile.getId(), file, fileManager.getFileUrl(file.getFilePath()));
                })
                .toList();
    }
}
