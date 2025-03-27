package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    private ChallengeParticipationRepository challengeParticipationRepository;

    @Mock
    private RedisTemplate<String, Long> redisTemplate;

    @BeforeEach
    void setUpd() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(mock());
        lenient().when(redisTemplate.opsForSet()).thenReturn(mock());
        lenient().when(redisTemplate.opsForZSet()).thenReturn(mock());
    }

    @Test
    @DisplayName("챌린지 북마크 추가 시 북마크 정보를 반환한다.")
    void 챌린지_북마크_추가_시_북마크_정보를_반환한다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        Long userId = UserFixtures.회원_PK;
        User user = spy(UserFixtures.회원);

        when(user.getId()).thenReturn(userId);
        when(challengeRepository.existsById(challengeId))
                .thenReturn(true);
        when(redisTemplate.opsForValue().get(any(String.class)))
                .thenReturn((long) 북마크_수);
        when(redisTemplate.opsForSet().isMember(any(String.class), any(Long.class)))
                .thenReturn(false);

        /* when */
        ChallengeResponse.Bookmark result = challengeBookmarkService.addBookmark(user, challengeId);

        /* then */
        verify(redisTemplate.opsForZSet(), times(1)).add(any(String.class), any(Long.class), any(Double.class));
        verify(redisTemplate.opsForSet(), times(1)).add(any(String.class), any(Long.class));
        verify(redisTemplate.opsForSet(), times(1)).remove(any(String.class), any(Long.class));
        verify(redisTemplate.opsForValue(), times(1)).increment(any(String.class), any(Long.class));

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
        when(challengeRepository.existsById(challengeId))
                .thenReturn(true);
        when(redisTemplate.opsForValue().get(any(String.class)))
                .thenReturn((long) 북마크_수);
        when(redisTemplate.opsForSet().isMember(any(String.class), any(Long.class)))
                .thenReturn(true);

        /* when */
        ChallengeResponse.Bookmark result = challengeBookmarkService.addBookmark(user, challengeId);

        /* then */
        verify(redisTemplate.opsForZSet(), never()).add(any(String.class), any(Long.class), any(Double.class));
        verify(redisTemplate.opsForSet(), never()).add(any(String.class), any(Long.class));
        verify(redisTemplate.opsForSet(), never()).remove(any(String.class), any(Long.class));
        verify(redisTemplate.opsForValue(), never()).increment(any(String.class), any(Long.class));

        assertEquals(북마크_수, result.bookmarkCount());
        assertEquals(북마크_여부_참, result.bookmarked());
    }

    @Test
    @DisplayName("챌린지 북마크 추가 시 존재하지 않는 챌린지인 경우 예외를 던진다.")
    void 챌린지_북마크_추가_시_존재하지_않는_챌린지인_경우_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.잘못된_챌린지_ID;

        when(challengeRepository.existsById(challengeId))
                .thenReturn(false);

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
        when(challengeRepository.existsById(challengeId))
                .thenReturn(true);
        when(redisTemplate.opsForValue().get(any(String.class)))
                .thenReturn((long) 북마크_수);
        when(redisTemplate.opsForSet().isMember(any(String.class), any(Long.class)))
                .thenReturn(true);

        /* when */
        ChallengeResponse.Bookmark result = challengeBookmarkService.removeBookmark(user, challengeId);

        /* then */
        verify(redisTemplate.opsForZSet(), times(1)).remove(any(String.class), any(Long.class));
        verify(redisTemplate.opsForSet(), times(1)).remove(any(String.class), any(Long.class));
        verify(redisTemplate.opsForSet(), times(1)).add(any(String.class), any(Long.class));
        verify(redisTemplate.opsForValue(), times(1)).decrement(any(String.class), any(Long.class));

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
        when(challengeRepository.existsById(challengeId))
                .thenReturn(true);
        when(redisTemplate.opsForValue().get(any(String.class)))
                .thenReturn((long) 북마크_수);
        when(redisTemplate.opsForSet().isMember(any(String.class), any(Long.class)))
                .thenReturn(false);

        /* when */
        ChallengeResponse.Bookmark result = challengeBookmarkService.removeBookmark(user, challengeId);

        /* then */
        verify(redisTemplate.opsForZSet(), never()).remove(any(String.class), any(Long.class));
        verify(redisTemplate.opsForSet(), never()).remove(any(String.class), any(Long.class));
        verify(redisTemplate.opsForSet(), never()).add(any(String.class), any(Long.class));
        verify(redisTemplate.opsForValue(), never()).decrement(any(String.class), any(Long.class));

        assertEquals(북마크_수, result.bookmarkCount());
        assertEquals(북마크_여부_거짓, result.bookmarked());
    }

    @Test
    @DisplayName("챌린지 북마크 삭제 시 존재하지 않는 챌린지인 경우 예외를 던진다.")
    void 챌린지_북마크_삭제_시_존재하지_않는_챌린지인_경우_예외를_던진다 () {
        /* given */
        Long challengeId = ChallengeFixtures.잘못된_챌린지_ID;

        when(challengeRepository.existsById(challengeId))
                .thenReturn(false);

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

        List<Challenge> challenges = List.of(spy(ChallengeFixtures.챌린지()), spy(ChallengeFixtures.챌린지()), spy(ChallengeFixtures.챌린지()));

        for(int i = 0; i < challenges.size(); i++) {
            when(challenges.get(i).getId()).thenReturn((long) i + 1);
        }

        when(user.getId()).thenReturn(userId);
        when(redisTemplate.opsForZSet().reverseRange(any(String.class), any(Long.class), any(Long.class)))
                .thenReturn(challengeIds);
        when(challengeRepository.findAllByIdIn(any(Set.class)))
                .thenReturn(challenges);
        when(challengeParticipationRepository.countByChallengeIdAndStatus(any(Long.class), eq(ParticipationStatus.ACCEPTED)))
                .thenReturn(ChallengeFixtures.참여자_수);
        when(redisTemplate.opsForValue().get(any(String.class)))
                .thenReturn((long) 북마크_수);
        when(redisTemplate.opsForZSet().size(any(String.class)))
                .thenReturn((long) challengeIds.size());

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
        when(redisTemplate.opsForZSet().reverseRange(any(String.class), any(Long.class), any(Long.class)))
                .thenReturn(빈_북마크된_챌린지_ID_목록);
        when(redisTemplate.opsForZSet().size(any(String.class)))
                .thenReturn(0L);

        /* when */
        PageResponse<ChallengeResponse.Preview> result = challengeBookmarkService.getMyBookmarkedChallenges(user, ChallengeFixtures.페이지_요청);

        /* then */
        verify(challengeRepository, never()).findAllByIdIn(any(Set.class));
        verify(challengeParticipationRepository, never()).countByChallengeIdAndStatus(any(Long.class), eq(ParticipationStatus.ACCEPTED));
        verify(redisTemplate.opsForValue(), never()).get(any(String.class));

        assertEquals(0, result.content().size());
        assertEquals(0, result.totalElements());
    }

    @Test
    @DisplayName("챌린지 북마크 여부 조회 시 북마크 여부를 반환한다.")
    void 챌린지_북마크_여부_조회_시_북마크_여부를_반환한다 () {
        /* given */
        Long userId = UserFixtures.회원_PK;
        Long challengeId = ChallengeFixtures.챌린지_ID;
        boolean bookmarked = true;

        when(redisTemplate.opsForSet().isMember(any(String.class), any(Long.class)))
                .thenReturn(bookmarked);

        /* when */
        boolean result = challengeBookmarkService.isBookmarked(userId, challengeId);

        /* then */
        assertEquals(bookmarked, result);
    }

    @Test
    @DisplayName("챌린지 북마크 수 조회 시 북마크 수를 반환한다.")
    void 챌린지_북마크_수_조회_시_북마크_수를_반환한다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        int bookmarkCount = 북마크_수;

        when(redisTemplate.opsForValue().get(any(String.class)))
                .thenReturn((long) bookmarkCount);

        /* when */
        int result = challengeBookmarkService.getChallengeBookmarkCount(challengeId);

        /* then */
        assertEquals(bookmarkCount, result);
    }

    @Test
    @DisplayName("챌린지에 대한 모든 북마크를 삭제한다.")
    void 챌린지에_대한_모든_북마크를_삭제한다 () {
        /* given */
        Long challengeId = ChallengeFixtures.챌린지_ID;
        Set<Long> userIds = Set.of(1L, 2L);

        when(redisTemplate.opsForSet().members(any(String.class)))
                .thenReturn(userIds);
        when(redisTemplate.opsForZSet().remove(any(String.class), any(Long.class)))
                .thenReturn(1L);
        when(redisTemplate.delete(any(List.class)))
                .thenReturn(3L);

        /* when */
        challengeBookmarkService.removeBookmarksByChallenge(challengeId);

        /* then */
        verify(redisTemplate.opsForZSet(), times(userIds.size())).remove(any(String.class), any(Long.class));
        verify(redisTemplate, times(1)).delete(any(List.class));
    }
}