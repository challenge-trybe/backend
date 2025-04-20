package com.trybe.modulebatch.challenge.job.processor;

import com.trybe.modulebatch.challenge.job.dto.ChallengeBookmarkDto;
import com.trybe.modulecore.challenge.entity.ChallengeBookmark;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class ChallengeBookmarkProcessor implements ItemProcessor<ChallengeBookmarkDto, ChallengeBookmark> {
    @Override
    public ChallengeBookmark process(ChallengeBookmarkDto item) throws Exception {
        return new ChallengeBookmark(item.getChallengeId(), item.getUserId());
    }
}
