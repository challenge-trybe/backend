package com.trybe.moduleapi.post.fixtures;

import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.post.dto.PostRequest;
import com.trybe.moduleapi.post.dto.PostResponse;
import com.trybe.moduleapi.post.service.event.PostEvent;
import com.trybe.moduleapi.post.service.event.PostEventType;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.post.enums.PostCategory;
import com.trybe.modulecore.post.enums.PostOrder;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PostFixtures {
    public static final Long id = 1L;
    public static final String 제목 = "테스트 게시글 제목입니다.";
    public static final String 내용 = "테스트 게시글 내용입니다.";
    public static final PostCategory 카테고리 = PostCategory.PROMOTION;
    public static final Set<Long> 챌린지_Ids = Set.of(1L, 2L, 3L);
    public static final Set<Long> 게시글_아이디_목록 = new LinkedHashSet<>(List.of(1L, 2L, 4L, 3L, 5L));
    public static final LocalDateTime 작성일 = LocalDateTime.of(2025,01,01,20,10,58);

    public static final String 수정_제목 = "테스트 게시글 수정 제목입니다.";
    public static final String 수정_내용 = "테스트 게시글 수정 내용입니다.";
    public static final PostCategory 수정_카테고리 = PostCategory.QNA;
    public static final Set<Long> 수정_챌린지_Ids = Set.of(1L, 2L);

    public static final String 검색_키워드 = "키워드";
    public static final List<PostCategory> 게시글_카테고리 = List.of(PostCategory.PROMOTION);
    public static final PostOrder 정렬 = PostOrder.LATEST;


    public static final Post 게시글 = Post.builder()
                                       .user(UserFixtures.회원)
                                       .title(제목)
                                       .content(내용)
                                       .category(카테고리)
                                       .build();

    public static Post createPost(User user){
        return Post.builder()
                    .user(user)
                    .title(제목)
                    .content(내용)
                    .category(카테고리)
                    .build();
    }

    public static final PostRequest.Create 게시글_생성 = new PostRequest.Create(제목, 내용, 카테고리, 챌린지_Ids);
    public static final PostRequest.Create 잘못된_게시글_생성 = new PostRequest.Create("", "", null, null);
    public static final PostRequest.Update 게시글_수정 = new PostRequest.Update(수정_제목, 수정_내용, 수정_카테고리, 수정_챌린지_Ids);
    public static final PostRequest.Update 잘못된_게시글_수정 = new PostRequest.Update("", "", null, null);
    public static final PostRequest.Read 게시글_필터링_조회 = new PostRequest.Read(검색_키워드, 게시글_카테고리,정렬);
    public static final PostResponse.Detail 게시글_상세_응답 = PostResponse.Detail.from(게시글, ChallengeFixtures.챌린지_목록, 1);
    public static final PostResponse.Detail 컨트롤러_테스트_게시글_상세_응답 = new PostResponse.Detail(id, 제목, 내용, 카테고리, UserFixtures.요약_회원_응답, 작성일,1, ChallengeFixtures.챌린지_목록_응답);
    public static final PostResponse.Summary 게시글_요약_응답 = new PostResponse.Summary(id, 제목, 카테고리, UserFixtures.요약_회원_응답, 작성일);

    public static Pageable 페이지_요청 = PageRequest.of(0, 10);
    public static List<Post> 게시글_목록 = List.of(게시글,게시글,게시글,게시글,게시글,게시글,게시글);
    public static Page<Post> 페이지_응답 = new PageImpl<>(게시글_목록, 페이지_요청, 게시글_목록.size());
    public static final PageResponse<PostResponse.Summary> 게시글_페이지_응답 = new PageResponse<>(페이지_응답.map(PostResponse.Summary::from));

    public static List<Post> 컨트롤러_게시글_목록 = List.of(게시글);
    public static Page<Post> 컨트롤러_페이지_응답 = new PageImpl<>(컨트롤러_게시글_목록, 페이지_요청, 게시글_목록.size());
    public static final PageResponse<PostResponse.Summary> 컨트롤러_게시글_페이지_응답 = new PageResponse<>(
            컨트롤러_페이지_응답.map(post -> new PostResponse.Summary(
                    post.getId(),
                    post.getTitle(),
                    post.getCategory(),
                    UserFixtures.요약_회원_응답,
                    작성일
            ))
    );

    public static final List<Post> ID_존재하는_게시글_목록 = List.of(1L, 2L, 3L, 4L, 5L).stream()
                                       .map(id -> {
                                           Post post = Post.builder()
                                                           .user(UserFixtures.회원)
                                                           .title(제목)
                                                           .content(내용)
                                                           .category(카테고리)
                                                           .build();  // 기본 생성자 사용
                                           setIdUsingReflection(post, id);  // 리플렉션을 이용하여 id 값을 설정
                                           return post;
                                       })
                                       .collect(Collectors.toList());

    private static void setIdUsingReflection(Post post, Long id) {
        try {
            Field idField = Post.class.getDeclaredField("id");  // id 필드를 리플렉션으로 가져오기
            idField.setAccessible(true);  // private 필드에 접근 가능하게 설정
            idField.set(post, id);  // 해당 post 객체에 id 값을 설정
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PostEvent 게시글_생성_이벤트(Long postId){
        return PostEvent.from(postId, PostEventType.POST_CREATED);
    }

    public static PostEvent 게시글_삭제_이벤트(Long postId){
        return PostEvent.from(postId, PostEventType.POST_DELETED);
    }

    public static PostEvent 게시글_조회_이벤트(Long postId) {
        return PostEvent.from(postId, PostEventType.POST_VIEW);
    }
    public static List<PostResponse.Summary> 인기_게시글 = 게시글_목록.stream().map(PostResponse.Summary::from).toList();

}
