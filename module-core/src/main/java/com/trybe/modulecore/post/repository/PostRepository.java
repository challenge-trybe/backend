package com.trybe.modulecore.post.repository;

import com.trybe.modulecore.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostCustomRepository {
    List<Post> findAllByIdIn(Set<Long> postIds);
}
