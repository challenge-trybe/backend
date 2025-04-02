package com.trybe.moduleapi.challenge.client;

import com.trybe.moduleapi.challenge.dto.ChallengeRecommendationRequest;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "challengeRecommendation", url = "${spring.cloud.openfeign.client.challengeRecommendation.url}")
public interface ChallengeRecommendationClient {
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    List<ChallengeResponse.Preview> getChallengeRecommendations(
            @RequestBody ChallengeRecommendationRequest request
    );
}
