package com.trybe.modulecore.challenge.repository.preference;

import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ChallengeCategory;

import java.util.List;

public interface ChallengePreferenceCache {
    void addPreference(Long userId, Challenge challenge, int score);
    void removePreference(Long userId, Challenge challenge, int score);
    void removePreferencesByUser(Long userId);
    List<ChallengeCategory> getPreferenceCategories(Long userId, int count);
    List<String> getPreferenceKeywords(Long userId, int count);
}
