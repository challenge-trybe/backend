package com.trybe.moduleapi.post.fixtures;

import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.post.entity.PostChallenge;

import java.util.List;

public class PostChallengeFixtures {

    public static PostChallenge 게시글_챌린지 = new PostChallenge(PostFixtures.게시글, ChallengeFixtures.챌린지());
    public static List<PostChallenge> 게시글_챌린지_목록 = List.of(게시글_챌린지);

}
