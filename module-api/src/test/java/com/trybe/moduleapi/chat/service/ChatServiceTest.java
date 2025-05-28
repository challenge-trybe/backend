package com.trybe.moduleapi.chat.service;

import com.trybe.moduleapi.challenge.exception.participation.NotFoundChallengeParticipationException;
import com.trybe.moduleapi.challenge.fixtures.ChallengeFixtures;
import com.trybe.moduleapi.chat.dto.ChatRequest;
import com.trybe.moduleapi.chat.dto.ChatResponse;
import com.trybe.moduleapi.chat.exception.NotFoundChatRoomException;
import com.trybe.moduleapi.chat.fixture.ChatFixtures;
import com.trybe.moduleapi.common.dto.CursorResponse;
import com.trybe.moduleapi.notification.service.NotificationProducerService;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.moduleapi.user.service.UserService;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.chat.entity.ChatMessage;
import com.trybe.modulecore.chat.entity.ChatRoom;
import com.trybe.modulecore.chat.enums.MessageType;
import com.trybe.modulecore.chat.repository.ChatMessageRepository;
import com.trybe.modulecore.chat.repository.ChatRoomRepository;
import com.trybe.modulecore.chat.repository.ChatRoomUserCache;
import com.trybe.modulecore.notification.entity.Notification;
import com.trybe.modulecore.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {
    @Mock
    ChallengeParticipationRepository challengeParticipationRepository;
    @Mock
    ChatRoomRepository chatRoomRepository;
    @Mock
    ChatMessageRepository chatMessageRepository;
    @Mock
    SimpMessagingTemplate messagingTemplate;
    @Mock
    ChatRoomUserCache chatRoomUserCache;
    @Mock
    UserService userService;
    @Mock
    NotificationProducerService notificationProducerService;

    @InjectMocks
    ChatService chatService;

    @Captor
    private ArgumentCaptor<Map<UUID, Notification>> notificationMapCaptor;

    @Test
    @DisplayName("해당 채팅방에 속한 회원이 메시지를 보내면 성공한다.")
    void 해당_채팅방에_속한_회원이_메시지를_보내면_성공한다 () {
        /* given */
        User 회원 = UserFixtures.회원();
        ChatRoom 채팅방 = ChatFixtures.채팅방();
        Long 채팅방_ID = ChatFixtures.채팅방_ID;
        ChatRequest.Send 채팅_메시지_전송_요청 = ChatFixtures.채팅_메시지_전송_요청;

        Set<String> 오프라인_유저_아이디 = ChatFixtures.오프라인_유저_아이디;
        List<User> 오프라인_유저 = UserFixtures.채팅_오프라인_유저;

        when(chatRoomRepository.existsById(채팅방_ID))
                .thenReturn(true);
        when(chatRoomRepository.findById(채팅방_ID))
                .thenReturn(Optional.of(채팅방));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), any(ParticipationStatus.class)))
                .thenReturn(true);
        when(chatRoomUserCache.findOfflineUserIds(any()))
                .thenReturn(오프라인_유저_아이디);
        when(userService.findByUserIdIn(오프라인_유저_아이디))
                .thenReturn(오프라인_유저);

        /* when */
        chatService.sendMessage(채팅방_ID, 회원, 채팅_메시지_전송_요청);

        /* then */
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
        verify(messagingTemplate, times(1)).convertAndSend(any(String.class), any(ChatResponse.Message.class));
        verify(notificationProducerService, times(1)).publishChatNotification(notificationMapCaptor.capture());
        Map<UUID, Notification> 캡쳐된_채팅_알림맵 = notificationMapCaptor.getValue();
        assertEquals(캡쳐된_채팅_알림맵.size() , 오프라인_유저.size());
    }

    @Test
    @DisplayName("메시지 전송 시 해당 채팅방이 존재하지 않으면 예외를 던진다.")
    void 메시지_전송_시_해당_채팅방이_존재하지_않으면_예외를_던진다 () {
        /* given */
        User 회원 = UserFixtures.회원();
        Long 채팅방_ID = ChatFixtures.채팅방_ID;
        ChatRequest.Send 채팅_메시지_전송_요청 = ChatFixtures.채팅_메시지_전송_요청;

        when(chatRoomRepository.existsById(채팅방_ID))
                .thenReturn(false);

        /* when, then */
        assertThrows(NotFoundChatRoomException.class,
                () -> chatService.sendMessage(채팅방_ID, 회원, 채팅_메시지_전송_요청));
    }

    @Test
    @DisplayName("해당 챌린지에 참여 상태가 아닌 회원이 메시지를 보내면 예외를 던진다.")
    void 해당_챌린지에_참여_상태가_아닌_회원이_메시지를_보내면_예외를_던진다 () {
        /* given */
        User 회원 = UserFixtures.회원();
        ChatRoom 채팅방 = ChatFixtures.채팅방();
        Long 채팅방_ID = ChatFixtures.채팅방_ID;
        ChatRequest.Send 채팅_메시지_전송_요청 = ChatFixtures.채팅_메시지_전송_요청;

        when(chatRoomRepository.existsById(채팅방_ID))
                .thenReturn(true);
        when(chatRoomRepository.findById(채팅방_ID))
                .thenReturn(Optional.of(채팅방));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), any(ParticipationStatus.class)))
                .thenReturn(false);

        /* when, then */
        assertThrows(NotFoundChallengeParticipationException.class,
                () -> chatService.sendMessage(채팅방_ID, 회원, 채팅_메시지_전송_요청));
    }

    @Test
    @DisplayName("정상적인 채팅 메시지 조회 시 메시지 내역을 반환한다.")
    void 정상적인_채팅_메시지_조회_시_메시지_내역을_반환한다 () {
        /* given */
        User 회원 = UserFixtures.회원;
        ChatRoom 채팅방 = ChatFixtures.채팅방();
        Long 채팅방_ID = ChatFixtures.채팅방_ID;
        Long 커서_ID = ChatFixtures.커서_ID;

        MessageType 입장 = ChatFixtures.입장;
        ChatMessage 입장_메시지 = ChatFixtures.채팅_메시지(채팅방, 회원, "입장 메시지", 입장);
        List<ChatMessage> 채팅_메시지_내역 = ChatFixtures.채팅_메시지_내역;

        when(chatRoomRepository.existsById(채팅방_ID))
                .thenReturn(true);
        when(chatRoomRepository.findById(채팅방_ID))
                .thenReturn(Optional.of(채팅방));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), any(ParticipationStatus.class)))
                .thenReturn(true);
        when(chatMessageRepository.findByChatRoomIdAndUserIdAndMessageType(회원.getId(), 채팅방.getId(), 입장))
                .thenReturn(입장_메시지);
        when(chatMessageRepository.findLatestIdByChatRoomId(채팅방.getId()))
                .thenReturn(커서_ID);
        when(chatMessageRepository.findMessagesByCursorId(채팅방_ID, 커서_ID+1, 입장_메시지.getCreatedAt(), Limit.of(ChatFixtures.메시지_조회_제한_개수+1)))
                .thenReturn(채팅_메시지_내역);

        /* when */
        CursorResponse<ChatResponse.Message> response = chatService.findMessages(회원, 채팅방_ID, null);

        /* then */
        assertEquals(response.content().size(), 채팅_메시지_내역.size());
        assertEquals(response.hasNext(), false);
        assertEquals(response.nextCursor(), null);
    }

    @Test
    @DisplayName("해당 채팅방이 존재하지 않으면 채팅 메시지 조회 시 예외를 던진다.")
    void 해당_채팅방이_존재하지_않으면_채팅_메시지_조회_시_예외를_던진다 () {
        /* given */
        User 회원 = UserFixtures.회원();
        Long 채팅방_ID = ChatFixtures.채팅방_ID;

        when(chatRoomRepository.existsById(채팅방_ID))
                .thenReturn(false);

        /* when, then */
        assertThrows(NotFoundChatRoomException.class,
                () -> chatService.findMessages(회원, 채팅방_ID, null));
    }

    @Test
    @DisplayName("해당 채팅방에 참여상태가 아닌 회원이 채팅 메시지 조회 시 예외를 던진다.")
    void 해당_채팅방에_참여상태가_아닌_회원이_채팅_메시지_조회_시_예외를_던진다 () {
        /* given */
        User 회원 = UserFixtures.회원;
        ChatRoom 채팅방 = ChatFixtures.채팅방();
        Long 채팅방_ID = ChatFixtures.채팅방_ID;

        when(chatRoomRepository.existsById(채팅방_ID))
                .thenReturn(true);
        when(chatRoomRepository.findById(채팅방_ID))
                .thenReturn(Optional.of(채팅방));
        when(challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(any(), any(), any(ParticipationStatus.class)))
                .thenReturn(false);

        /* when, then */
        assertThrows(NotFoundChallengeParticipationException.class,
                () -> chatService.findMessages(회원, 채팅방_ID, null));
    }

    @Test
    @DisplayName("챌린지 생성 시 해당 챌린지에 대한 채팅방이 생성된다.")
    void 챌린지_생성_시_해당_챌린지에_대한_채팅방이_생성된다 () {
        /* given */
        Challenge 챌린지 = ChallengeFixtures.챌린지();
        ChatRoom 채팅방 = ChatFixtures.채팅방(챌린지);

        when(chatRoomRepository.save(any(ChatRoom.class)))
                .thenReturn(채팅방);

        /* when */
        Long chatRoomId = chatService.create(챌린지);

        /* then */
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
        assertEquals(채팅방.getId(), chatRoomId);
    }

    @Test
    @DisplayName("챌린지 참여 수락 시 채팅방에 입장한다.")
    void 챌린지_참여_수락_시_채팅방에_입장한다 () {
        /* given */
        User 회원 = UserFixtures.회원;

        ChatRoom 채팅방 = ChatFixtures.채팅방();
        Long 채팅방_ID = ChatFixtures.채팅방_ID;

        when(chatRoomRepository.findById(채팅방_ID))
                .thenReturn(Optional.of(채팅방));

        /* when */
        chatService.addUserToChatRoom(채팅방_ID, 회원);

        /* then */
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
        verify(messagingTemplate, times(1)).convertAndSend(any(String.class), any(ChatResponse.Message.class));
    }

    @Test
    @DisplayName("챌린지 탈퇴 시 채팅방에서 나간다.")
    void 챌린지_탈퇴_시_채팅방에서_나간다 () {
        /* given */
        User 회원 = UserFixtures.회원;

        ChatRoom 채팅방 = ChatFixtures.채팅방();
        Long 채팅방_ID = ChatFixtures.채팅방_ID;

        when(chatRoomRepository.findById(채팅방_ID))
                .thenReturn(Optional.of(채팅방));

        /* when */
        chatService.deleteUserFromChatRoom(채팅방_ID, 회원);

        /* then */
        verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
        verify(messagingTemplate, times(1)).convertAndSend(any(String.class), any(ChatResponse.Message.class));
    }

    @Test
    @DisplayName("챌린지 삭제 시 채팅방은 삭제된다.")
    void 챌린지_삭제_시_채팅방은_삭제된다 () {
        /* given */
        Long 챌린지_ID = ChallengeFixtures.챌린지_ID;
        ChatRoom 채팅방 = spy(ChatFixtures.채팅방());

        when(chatRoomRepository.findByChallengeId(챌린지_ID))
                .thenReturn(Optional.of(채팅방));

        /* when */
        chatService.delete(챌린지_ID);

        /* then */
        verify(chatRoomUserCache, times(1)).clear(any());
        verify(chatMessageRepository, times(1)).deleteByChatRoomId(any());
        verify(chatRoomRepository, times(1)).deleteById(any());
    }

    @Test
    @DisplayName("챌린지 시작 시 시작 메시지가 채팅방에 전송된다.")
    void 챌린지_시작_시_시작_메시지가_채팅방에_전송된다 () {
        /* given */
        Challenge 챌린지 = ChallengeFixtures.챌린지();
        ChatRoom 채팅방 = ChatFixtures.채팅방();

        Set<String> 오프라인_유저_아이디 = ChatFixtures.오프라인_유저_아이디;
        List<User> 오프라인_유저 = UserFixtures.채팅_오프라인_유저;

        when(chatRoomRepository.findByChallengeId(챌린지.getId()))
                .thenReturn(Optional.of(채팅방));
        when(chatRoomUserCache.findOfflineUserIds(any()))
                .thenReturn(오프라인_유저_아이디);
        when(userService.findByUserIdIn(오프라인_유저_아이디))
                .thenReturn(오프라인_유저);


        /* when */
        chatService.sendChallengeStartMessage(챌린지);

        /* then */
        verify(messagingTemplate, times(1)).convertAndSend(any(String.class), any(ChatResponse.Message.class));
        verify(notificationProducerService, times(1)).publishChatNotification(notificationMapCaptor.capture());
        Map<UUID, Notification> 캡쳐된_채팅_알림맵 = notificationMapCaptor.getValue();
        assertEquals(캡쳐된_채팅_알림맵.size() , 오프라인_유저.size());
    }

    @Test
    @DisplayName("챌린지 종료 시 종료 메시지가 채팅방에 전송된다.")
    void 챌린지_종료_시_종료_메시지가_채팅방에_전송된다 () {
        /* given */
        Challenge 챌린지 = ChallengeFixtures.챌린지();
        ChatRoom 채팅방 = ChatFixtures.채팅방();

        Set<String> 오프라인_유저_아이디 = ChatFixtures.오프라인_유저_아이디;
        List<User> 오프라인_유저 = UserFixtures.채팅_오프라인_유저;

        when(chatRoomRepository.findByChallengeId(챌린지.getId()))
                .thenReturn(Optional.of(채팅방));
        when(chatRoomUserCache.findOfflineUserIds(any()))
                .thenReturn(오프라인_유저_아이디);
        when(userService.findByUserIdIn(오프라인_유저_아이디))
                .thenReturn(오프라인_유저);

        /* when */
        chatService.sendChallengeClosedMessage(챌린지);

        /* then */
        verify(messagingTemplate, times(1)).convertAndSend(any(String.class), any(ChatResponse.Message.class));
        verify(notificationProducerService, times(1)).publishChatNotification(notificationMapCaptor.capture());
        Map<UUID, Notification> 캡쳐된_채팅_알림맵 = notificationMapCaptor.getValue();
        assertEquals(캡쳐된_채팅_알림맵.size() , 오프라인_유저.size());
    }
}
