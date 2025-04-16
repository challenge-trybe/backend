package com.trybe.modulecore.post.repository;

import com.trybe.modulecore.post.entity.PostLike;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostLikeCustomRepository {
    void bulkInsert(List<PostLike> postLikes);
    void bulkDelete(List<PostLike> postLikes);
}
