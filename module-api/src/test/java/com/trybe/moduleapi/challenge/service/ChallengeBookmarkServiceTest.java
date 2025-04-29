package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.dto.ChallengeResponseAssembler;
import com.trybe.moduleapi.challenge.event.ChallengeEvent;
import com.trybe.moduleapi.challenge.event.pub.ChallengeEventPublisher;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkCache;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static com.trybe.moduleapi.challenge.fixtures.ChallengeBookmarkFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeBookmarkServiceTest {
    @InjectMocks
    private ChallengeBookmarkService challengeBookmarkService;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private ChallengeBookmarkCache challengeBookmarkCache;

    @Mock
    private ChallengeEventPublisher challengeEventPublisher;

    @Mock
    private ChallengeResponseAssembler challengeResponseAssembler;

    @Test
    @DisplayName("챌린지 북마크 추가 시 북마크 정보를 반환한다.")
    void 챌린지_북마크_추가_시_북마크_정보를_반환한다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        Long userId = UserFixtures.회원_PK;
        User user = spy(UserFixtures.회원);

        when(user.getId()).thenReturn(userId);
        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.ofNullable(ChallengeFixtures.진행중인_챌린지));
        when(challengeBookmarkCache.getBookmarkCount(challengeId))
                .thenReturn(북마크_수);
        when(challengeBookmarkCache.isBookmarked(userId, challengeId))
                .thenReturn(false);

        /* when */
        ChallengeResponse.Bookmark result = challengeBookmarkService.addBookmark(user, challengeId);

        /* then */
        verify(challengeBookmarkCache, times(1)).addBookmark(userId, challengeId);
        verify(challengeEventPublisher, times(1)).publish(any(ChallengeEvent.class));

        assertEquals(북마크_수 + 1, result.bookmarkCount());
        assertEquals(북마크_여부_참, result.bookmarked());
    }

    @Test
    @DisplayName("챌린지 북마크 추가 시 이미 북마크 한 경우 북마크 정보를 반환한다.")
    void 챌린지_북마크_추가_시_이미_북마크_한_경우_북마크_정보를_반환한다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        Long userId = UserFixtures.회원_PK;
        User user = spy(UserFixtures.회원);

        when(user.getId()).thenReturn(userId);
        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.ofNullable(ChallengeFixtures.진행중인_챌린지));
        when(challengeBookmarkCache.getBookmarkCount(challengeId))
                .thenReturn(북마크_수);
        when(challengeBookmarkCache.isBookmarked(userId, challengeId))
                .thenReturn(true);

        /* when */
        ChallengeResponse.Bookmark result = challengeBookmarkService.addBookmark(user, challengeId);

        /* then */
        verify(challengeBookmarkCache, never()).addBookmark(userId, challengeId);
        verify(challengeEventPublisher, never()).publish(any(ChallengeEvent.class));

        assertEquals(북마크_수, result.bookmarkCount());
        assertEquals(북마크_여부_참, result.bookmarked());
    }

    @Test
    @DisplayName("챌린지 북마크 추가 시 존재하지 않는 챌린지인 경우 예외를 던진다.")
    void 챌린지_북마크_추가_시_존재하지_않는_챌린지인_경우_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.잘못된_챌린지_ID;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundChallengeException.class, () -> challengeBookmarkService.addBookmark(UserFixtures.회원, challengeId));
    }

    @Test
    @DisplayName("챌린지 북마크 삭제 시 북마크 정보를 반환한다.")
    void 챌린지_북마크_삭제_시_북마크_정보를_반환한다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        Long userId = UserFixtures.회원_PK;
        User user = spy(UserFixtures.회원);

        when(user.getId()).thenReturn(userId);
        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.ofNullable(ChallengeFixtures.진행중인_챌린지));
        when(challengeBookmarkCache.getBookmarkCount(challengeId))
                .thenReturn(북마크_수);
        when(challengeBookmarkCache.isBookmarked(userId, challengeId))
                .thenReturn(true);

        /* when */
        ChallengeResponse.Bookmark result = challengeBookmarkService.removeBookmark(user, challengeId);

        /* then */
        verify(challengeBookmarkCache, times(1)).removeBookmark(userId, challengeId);
        verify(challengeEventPublisher, times(1)).publish(any(ChallengeEvent.class));

        assertEquals(북마크_수 - 1, result.bookmarkCount());
        assertEquals(북마크_여부_거짓, result.bookmarked());
    }

    @Test
    @DisplayName("챌린지 북마크 삭제 시 북마크 하지 않은 경우 북마크 정보를 반환한다.")
    void 챌린지_북마크_삭제_시_북마크_하지_않은_경우_북마크_정보를_반환한다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        Long userId = UserFixtures.회원_PK;
        User user = spy(UserFixtures.회원);

        when(user.getId()).thenReturn(userId);
        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.ofNullable(ChallengeFixtures.진행중인_챌린지));
        when(challengeBookmarkCache.getBookmarkCount(challengeId))
                .thenReturn(북마크_수);
        when(challengeBookmarkCache.isBookmarked(userId, challengeId))
                .thenReturn(false);

        /* when */
        ChallengeResponse.Bookmark result = challengeBookmarkService.removeBookmark(user, challengeId);

        /* then */
        verify(challengeBookmarkCache, never()).removeBookmark(userId, challengeId);
        verify(challengeEventPublisher, never()).publish(any(ChallengeEvent.class));

        assertEquals(북마크_수, result.bookmarkCount());
        assertEquals(북마크_여부_거짓, result.bookmarked());
    }

    @Test
    @DisplayName("챌린지 북마크 삭제 시 존재하지 않는 챌린지인 경우 예외를 던진다.")
    void 챌린지_북마크_삭제_시_존재하지_않는_챌린지인_경우_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.잘못된_챌린지_ID;

        when(challengeRepository.findById(challengeId))
                .thenReturn(Optional.empty());

        /* when */
        /* then */
        assertThrows(NotFoundChallengeException.class, () -> challengeBookmarkService.removeBookmark(UserFixtures.회원, challengeId));
    }

    @Test
    @DisplayName("나의 북마크 챌린지 목록 조회 시 북마크된 챌린지 목록을 반환한다.")
    void 나의_북마크_챌린지_목록_조회_시_북마크된_챌린지_목록을_반환한다 () {
        /* given */
        Long userId = UserFixtures.회원_PK;
        User user = spy(UserFixtures.회원);
        Set<Long> challengeIds = 북마크된_챌린지_ID_목록;
        Iterator<Long> iterator = challengeIds.iterator();

        List<Challenge> challenges = List.of(spy(ChallengeFixtures.챌린지()), spy(ChallengeFixtures.챌린지()), spy(ChallengeFixtures.챌린지()));

        for(int i = 0; i < challenges.size(); i++) {
            when(challenges.get(i).getId()).thenReturn((long) i + 1);
        }

        when(user.getId()).thenReturn(userId);
        when(challengeBookmarkCache.getBookmarkedChallenges(eq(userId), any(Integer.class), any(Integer.class)))
                .thenReturn(challengeIds);
        when(challengeRepository.findAllByIdIn(any(Set.class)))
                .thenReturn(challenges);
        when(challengeBookmarkCache.getUserBookmarkCount(any(Long.class)))
                .thenReturn(challengeIds.size());
        when(challengeResponseAssembler.toPreview(any(Challenge.class), any(Long.class)))
                .thenReturn(ChallengeFixtures.챌린지_미리보기_응답_생성(iterator.next()))
                .thenReturn(ChallengeFixtures.챌린지_미리보기_응답_생성(iterator.next()))
                .thenReturn(ChallengeFixtures.챌린지_미리보기_응답_생성(iterator.next()));

        /* when */
        PageResponse<ChallengeResponse.Preview> result = challengeBookmarkService.getMyBookmarkedChallenges(user, ChallengeFixtures.페이지_요청);

        /* then */
        List<Long> expectedOrder = new ArrayList<>(challengeIds);
        List<Long> actualOrder = result.content().stream()
                .map(ChallengeResponse.Preview::id)
                .collect(Collectors.toList());

        assertEquals(expectedOrder, actualOrder);

        assertEquals(challengeIds.size(), result.content().size());
        assertEquals(challengeIds.size(), result.totalElements());
    }

    @Test
    @DisplayName("나의 북마크 챌린지 목록 조회 시 북마크된 챌린지가 없는 경우 빈 목록을 반환한다.")
    void 나의_북마크_챌린지_목록_조회_시_북마크된_챌린지가_없는_경우_빈_목록을_반환한다 () {
        /* given */
        Long userId = UserFixtures.회원_PK;
        User user = spy(UserFixtures.회원);

        when(user.getId()).thenReturn(userId);
        when(challengeBookmarkCache.getBookmarkedChallenges(eq(userId), any(Integer.class), any(Integer.class)))
                .thenReturn(빈_북마크된_챌린지_ID_목록);
        when(challengeBookmarkCache.getUserBookmarkCount(userId))
                .thenReturn(0);

        /* when */
        PageResponse<ChallengeResponse.Preview> result = challengeBookmarkService.getMyBookmarkedChallenges(user, ChallengeFixtures.페이지_요청);

        /* then */
        verify(challengeRepository, never()).findAllByIdIn(any(Set.class));
        verify(challengeResponseAssembler, never()).toPreview(any(Challenge.class), any(Long.class));
        verify(challengeBookmarkCache, never()).getBookmarkCount(any(Long.class));

        assertEquals(0, result.content().size());
        assertEquals(0, result.totalElements());
    }
}