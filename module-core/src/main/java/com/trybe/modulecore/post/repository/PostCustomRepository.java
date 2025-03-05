package com.trybe.modulecore.post.repository;

import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.post.enums.PostCategory;
import com.trybe.modulecore.post.enums.PostOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostCustomRepository {
    Page<Post> findAllByKeywordAndCategories(String keyword, List<PostCategory> categories, PostOrder order, Pageable pageable);

}
