package com.trybe.moduleapi.post.fixtures;

import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.post.entity.PostChallenge;

import java.util.List;

public class PostChallengeFixtures {

    public static PostChallenge 포스트_첼린지 = new PostChallenge(PostFixtures.게시글, ChallengeFixtures.챌린지());
    public static List<PostChallenge> 포스트_챌린지_목록 = List.of(포스트_첼린지);

}
