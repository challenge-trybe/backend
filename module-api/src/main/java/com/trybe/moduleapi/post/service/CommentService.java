package com.trybe.moduleapi.post.service;

import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.post.dto.CommentRequest;
import com.trybe.moduleapi.post.dto.CommentResponse;
import com.trybe.moduleapi.post.exception.ForbiddenCommentException;
import com.trybe.moduleapi.post.exception.NotFoundCommentException;
import com.trybe.moduleapi.post.exception.NotFoundPostException;
import com.trybe.modulecore.post.entity.Comment;
import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.post.repository.CommentRepository;
import com.trybe.modulecore.post.repository.PostRepository;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public CommentResponse.Detail enroll(User user, Long postId, CommentRequest.Enroll request){
        Post post = getPostById(postId);
        Comment comment = request.toEntity(user, post, request.content());
        commentRepository.save(comment);
        return CommentResponse.Detail.from(comment);
    }

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse.Detail> findByMyComments(User user, Pageable pageable){
        Page<Comment> comments = commentRepository.findAllByUserId(user.getId(), pageable);
        return new PageResponse<>(comments.map(CommentResponse.Detail::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse.Summary> findAll(Long postId, Pageable pageable){
        Post post = getPostById(postId);
        Page<Comment> comments = commentRepository.findAllByPostId(post.getId(), pageable);
        return new PageResponse<>(comments.map(CommentResponse.Summary::from));
    }

    @Transactional
    public CommentResponse.Detail update(User user, Long commentId, CommentRequest.Update request) {
        Comment comment = getCommentById(commentId);
        checkLoginUserAndCommentWriter(user, comment);
        comment.updateComment(request.content());
        return CommentResponse.Detail.from(comment);
    }

    @Transactional
    public void delete(User user, Long commentId){
        Comment comment = getCommentById(commentId);
        checkLoginUserAndCommentWriter(user, comment);
        commentRepository.deleteById(comment.getId());
    }

    private void checkLoginUserAndCommentWriter(User loginUser, Comment comment) {
        if (comment.getUser().getId() != loginUser.getId()){
            throw  new ForbiddenCommentException();
        }
    }

    private Comment getCommentById(Long id){
        return commentRepository.findById(id).orElseThrow(() -> new NotFoundCommentException());
    }

    private Post getPostById(Long id){
        return postRepository.findById(id).orElseThrow(() -> new NotFoundPostException());
    }

}
