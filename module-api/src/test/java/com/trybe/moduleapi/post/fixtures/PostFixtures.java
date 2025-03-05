package com.trybe.moduleapi.post.fixtures;

import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.post.dto.PostRequest;
import com.trybe.moduleapi.post.dto.PostResponse;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.post.enums.PostCategory;
import com.trybe.modulecore.post.enums.PostOrder;
import com.trybe.modulecore.user.entity.User;
import jdk.jfr.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class PostFixtures {
    public static final Long id = 1L;
    public static final String 제목 = "테스트 게시글 제목입니다.";
    public static final String 내용 = "테스트 게시글 내용입니다.";
    public static final PostCategory 카테고리 = PostCategory.PROMOTION;
    public static final Set<Long> 챌린지_Ids = Set.of(1L, 2L, 3L);
    public static final LocalDateTime 작성일 = LocalDateTime.of(2025,01,01,20,10,58);

    public static final String 수정_제목 = "테스트 게시글 수정 제목입니다.";
    public static final String 수정_내용 = "테스트 게시글 수정 내용입니다.";
    public static final PostCategory 수정_카테고리 = PostCategory.PROMOTION;
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
    public static final PostResponse.Detail 게시글_상세_응답 = PostResponse.Detail.from(게시글, List.of(ChallengeFixtures.챌린지_요약_응답));
    public static final PostResponse.Detail 컨트롤러_테스트_게시글_상세_응답 = new PostResponse.Detail(id, 제목, 내용, 카테고리, UserFixtures.요약_회원_응답, 작성일, ChallengeFixtures.챌린지_목록_응답);
    public static final PostResponse.Summary 게시글_요약_응답 = new PostResponse.Summary(id, 제목, 내용, 카테고리, UserFixtures.요약_회원_응답, 작성일);

    public static Pageable 페이지_요청 = PageRequest.of(0, 10);
    public static List<Post> 포스트_목록 = List.of(게시글,게시글,게시글,게시글,게시글,게시글,게시글);
    public static Page<Post> 페이지_응답 = new PageImpl<>(포스트_목록, 페이지_요청, 포스트_목록.size());
    public static final PageResponse<PostResponse.Summary> 포스트_페이지_응답 = new PageResponse<>(페이지_응답.map(PostResponse.Summary::from));

    public static List<Post> 컨트롤러_포스트_목록 = List.of(게시글);
    public static Page<Post> 컨트롤러_페이지_응답 = new PageImpl<>(컨트롤러_포스트_목록, 페이지_요청, 포스트_목록.size());
    public static final PageResponse<PostResponse.Summary> 컨트롤러_포스트_페이지_응답 = new PageResponse<>(
            컨트롤러_페이지_응답.map(post -> new PostResponse.Summary(
                    post.getId(),
                    post.getTitle(),
                    post.getContent(),
                    post.getCategory(),
                    UserFixtures.요약_회원_응답,
                    작성일
            ))
    );


}
