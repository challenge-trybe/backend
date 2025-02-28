package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.dto.ChallengeRequest;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.entity.ChallengeParticipation;
import com.trybe.modulecore.challenge.enums.ChallengeRole;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;

    public ChallengeService(ChallengeRepository challengeRepository, ChallengeParticipationRepository challengeParticipationRepository) {
        this.challengeRepository = challengeRepository;
        this.challengeParticipationRepository = challengeParticipationRepository;
    }

    @Transactional
    public ChallengeResponse.Detail save(User user, ChallengeRequest.Create request) {
        Challenge challenge = request.toEntity();
        ChallengeParticipation participation = new ChallengeParticipation(user, challenge, ChallengeRole.LEADER, ParticipationStatus.ACCEPTED);

        challengeParticipationRepository.save(participation);
        Challenge savedChallenge = challengeRepository.save(challenge);

        return ChallengeResponse.Detail.from(savedChallenge);
    }

    public ChallengeResponse.Detail find(Long id) {
        // TODO: Challenge bookmark 정보 추가 반환 (북마크 여부)
        Challenge challenge = getChallenge(id);

        return ChallengeResponse.Detail.from(challenge);
    }

    public List<ChallengeResponse.Summary> findAll(ChallengeRequest.Read request) {
        // TODO: Challenge bookmark 정보 추가 반환 (북마크 여부)
        List<Challenge> challenges = challengeRepository.findAllByStatusInAndCategoryIn(request.statuses(), request.categories());

        return challenges.stream().map(ChallengeResponse.Summary::from).collect(Collectors.toList());
    }

    @Transactional
    public ChallengeResponse.Detail updateContent(User user, Long id, ChallengeRequest.UpdateContent request) {
        // TODO: Challenge 의 수정 권한 확인
        Challenge challenge = getChallenge(id);

        challenge.updateContent(request.title(), request.description(), request.startDate(), request.endDate(), request.capacity(), request.category());

        return ChallengeResponse.Detail.from(challenge);
    }

    @Transactional
    public ChallengeResponse.Detail updateProof(User user, Long id, ChallengeRequest.UpdateProof request) {
        // TODO: Challenge 의 수정 권한 확인
        Challenge challenge = getChallenge(id);

        challenge.updateProof(request.proofWay(), request.proofCount());

        return ChallengeResponse.Detail.from(challenge);
    }

    @Transactional
    public void delete(User user, Long id) {
        // TODO: Challenge 의 삭제 권한 확인
        Challenge challenge = getChallenge(id);

        challengeRepository.delete(challenge);
    }

    private Challenge getChallenge(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new NotFoundChallengeException());
    }
}
