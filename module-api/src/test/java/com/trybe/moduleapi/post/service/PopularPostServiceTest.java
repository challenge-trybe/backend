package com.trybe.moduleapi.post.service;

import com.trybe.moduleapi.post.dto.PostResponse;
import com.trybe.moduleapi.post.fixtures.PostFixtures;
import com.trybe.moduleapi.post.service.event.handler.PostEventHandler;
import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.post.repository.PopularPostCache;
import com.trybe.modulecore.post.repository.PostCreatedAtCache;
import com.trybe.modulecore.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PopularPostServiceTest {
    @Mock
    PostRepository postRepository;
    @Mock
    PostCreatedAtCache postCreatedAtCache;
    @Mock
    PopularPostCache popularPostCache;
    @Mock
    List<PostEventHandler> handlers;

    @InjectMocks
    PopularPostService popularPostService;

    @Test
    @DisplayName("인기 게시글 조회 시 어제 생성된 게시글 중 인기 게시글 10개를 반환한다.")
    void 인기_게시글_조회_시_어제_생성된_게시글_중_인기_게시글_10개를_반환한다 () {
        /* given */
        LocalDate time = LocalDate.now().minusDays(1);
        Set<Long> 인기_게시글_목록 = PostFixtures.게시글_아이디_목록;
        List<Post> 인기_게시글 = List.of(spy(PostFixtures.게시글), spy(PostFixtures.게시글),spy(PostFixtures.게시글),spy(PostFixtures.게시글),spy(PostFixtures.게시글));

        for(int i = 0; i < 인기_게시글.size(); i++) {
            when(인기_게시글.get(i).getId()).thenReturn((long) i + 1);
        }

        when(popularPostCache.findPopularPostIds(time)).thenReturn(인기_게시글_목록);
        when(postRepository.findAllByIdIn(인기_게시글_목록)).thenReturn(인기_게시글);

        /* when */
        List<PostResponse.Summary> top10Posts = popularPostService.findTop10Posts();

        /* then */
        List<Long> expectedOrder = new ArrayList<>(인기_게시글_목록);
        List<Long> actualOrder = top10Posts.stream()
                .map(PostResponse.Summary::id)
                .collect(Collectors.toList());

        assertEquals(expectedOrder, actualOrder);
    }
}
