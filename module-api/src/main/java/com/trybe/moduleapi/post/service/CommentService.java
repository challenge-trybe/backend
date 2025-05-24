package com.trybe.moduleapi.post.service;

import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.notification.service.NotificationProducerService;
import com.trybe.moduleapi.post.dto.CommentRequest;
import com.trybe.moduleapi.post.dto.CommentResponse;
import com.trybe.moduleapi.post.exception.ForbiddenCommentException;
import com.trybe.moduleapi.post.exception.NotFoundCommentException;
import com.trybe.moduleapi.post.exception.NotFoundPostException;
import com.trybe.moduleapi.post.service.event.PostEvent;
import com.trybe.moduleapi.post.service.event.PostEventType;
import com.trybe.moduleapi.post.service.event.pub.PostEventPublisher;
import com.trybe.modulecore.notification.entity.Notification;
import com.trybe.modulecore.notification.enums.NotificationType;
import com.trybe.modulecore.post.entity.Comment;
import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.post.repository.CommentRepository;
import com.trybe.modulecore.post.repository.PostRepository;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final PostEventPublisher eventPublisher;
    private final NotificationProducerService notificationProducerService;

    private static final String COMMENT_TITLE = "새로운 댓글이 달렸습니다.";
    private static final String COMMENT_NOTICE_MESSAGE_FORMAT = "[%s] 님이 게시글에 댓글을 달았습니다.";
    public CommentService(CommentRepository commentRepository, PostRepository postRepository, PostEventPublisher eventPublisher, NotificationProducerService notificationProducerService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.eventPublisher = eventPublisher;
        this.notificationProducerService = notificationProducerService;
    }

    @Transactional
    public CommentResponse.Summary enroll(User user, Long postId, CommentRequest.Enroll request){
        Post post = getPostById(postId);
        Comment comment = request.toEntity(user, post, request.content());
        commentRepository.save(comment);

        String message = createNotifyMessage(user.getNickname());
        Notification notification = new Notification(user.getId(), NotificationType.POST_COMMENT, postId, COMMENT_TITLE, message);
        notificationProducerService.publishPostCommentNotification(post.getUser().getUuid(), notification);

        eventPublisher.publish(PostEvent.from(post.getId(), PostEventType.COMMENT_CREATED));
        return CommentResponse.Summary.from(comment);
    }

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse.Detail> findAllByUser(User user, Pageable pageable){
        Page<Comment> comments = commentRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        return new PageResponse<>(comments.map(CommentResponse.Detail::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse.Summary> findAllByPost(Long postId, Pageable pageable){
        Post post = getPostById(postId);
        Page<Comment> comments = commentRepository.findAllByPostIdOrderByCreatedAtAsc(post.getId(), pageable);
        return new PageResponse<>(comments.map(CommentResponse.Summary::from));
    }

    @Transactional
    public CommentResponse.Summary update(User user, Long commentId, CommentRequest.Update request) {
        Comment comment = getCommentById(commentId);
        checkLoginUserAndCommentWriter(user, comment, "본인이 작성한 댓글만 수정할 수 있습니다.");
        comment.updateComment(request.content());
        return CommentResponse.Summary.from(comment);
    }

    @Transactional
    public void delete(User user, Long commentId){
        Comment comment = getCommentById(commentId);
        checkLoginUserAndCommentWriter(user, comment, "본인이 작성한 댓글만 삭제할 수 있습니다.");
        commentRepository.deleteById(comment.getId());
        eventPublisher.publish(PostEvent.from(comment.getPost().getId(), PostEventType.COMMENT_DELETED));
    }

    private void checkLoginUserAndCommentWriter(User loginUser, Comment comment, String message) {
        if (comment.getUser().getId() != loginUser.getId()){
            throw  new ForbiddenCommentException(message);
        }
    }

    private Comment getCommentById(Long id){
        return commentRepository.findById(id).orElseThrow(() -> new NotFoundCommentException());
    }

    private Post getPostById(Long id){
        return postRepository.findById(id).orElseThrow(() -> new NotFoundPostException());
    }

    private String createNotifyMessage(String nickName) {
        return String.format(COMMENT_NOTICE_MESSAGE_FORMAT, nickName);
    }

}
