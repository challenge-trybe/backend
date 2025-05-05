package com.trybe.moduleapi.challenge.client;

import com.trybe.modulecore.challenge.entity.Challenge;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "challengeRecommendation", url = "${spring.cloud.openfeign.client.challengeRecommendation.url}")
public interface ChallengeRecommendationClient {
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    List<Challenge> getChallengeRecommendations(
            @RequestParam Long userId
    );
}
