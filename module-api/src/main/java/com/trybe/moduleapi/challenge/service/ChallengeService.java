package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.dto.ChallengeRequest;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChallengeService {
    private final ChallengeRepository challengeRepository;

    public ChallengeService(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    /**
     * TODO: JWT 설정 완료 후 Authentication 객체에 저장된 유저 정보를 가져오도록 수정
     * save, updateContent, updateProof, delete 메소드의 파라미터
      */

    @Transactional
    public ChallengeResponse.Detail save(ChallengeRequest.Create request, Long userId) {
        // TODO: Challenge participation 에 대한 로직 추가

        Challenge savedChallenge = challengeRepository.save(request.toEntity());
        return ChallengeResponse.Detail.from(savedChallenge);
    }

    public ChallengeResponse.Detail find(Long id) {
        /**
         * TODO: Challenge participation 정보 추가 반환 (참여자 정보)
         * TODO: Challenge bookmark 정보 추가 반환 (북마크 여부)
          */

        Challenge challenge = getChallenge(id);

        return ChallengeResponse.Detail.from(challenge);
    }

    public List<ChallengeResponse.Summary> findAll(ChallengeRequest.Read request) {
        // TODO: Challenge bookmark 정보 추가 반환 (북마크 여부)
        List<Challenge> challenges = challengeRepository.findAllByStatusInAndCategoryIn(request.statuses(), request.categories());

        return challenges.stream().map(ChallengeResponse.Summary::from).collect(Collectors.toList());
    }

    @Transactional
    public ChallengeResponse.Detail updateContent(Long id, ChallengeRequest.UpdateContent request, Long userId) {
        // TODO: Challenge 의 수정 권한 확인
        Challenge challenge = getChallenge(id);

        challenge.updateContent(request.title(), request.description(), request.startDate(), request.endDate(), request.capacity(), request.category());

        return ChallengeResponse.Detail.from(challenge);
    }

    @Transactional
    public ChallengeResponse.Detail updateProof(Long id, ChallengeRequest.UpdateProof request, Long userId) {
        // TODO: Challenge 의 수정 권한 확인
        Challenge challenge = getChallenge(id);

        challenge.updateProof(request.proofWay(), request.proofCount());

        return ChallengeResponse.Detail.from(challenge);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        // TODO: Challenge 의 삭제 권한 확인
        Challenge challenge = getChallenge(id);

        challengeRepository.delete(challenge);
    }

    private Challenge getChallenge(Long id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new NotFoundChallengeException());

        return challenge;
    }
}
