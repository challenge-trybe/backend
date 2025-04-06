package com.trybe.moduleapi.chat.service;

import com.trybe.moduleapi.challenge.exception.participation.NotFoundChallengeParticipationException;
import com.trybe.moduleapi.chat.dto.ChatRequest;
import com.trybe.moduleapi.chat.dto.ChatResponse;
import com.trybe.moduleapi.chat.exception.NotFoundChatRoomException;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.chat.entity.ChatMessage;
import com.trybe.modulecore.chat.entity.ChatRoom;
import com.trybe.modulecore.chat.enums.MessageType;
import com.trybe.modulecore.chat.repository.ChatMessageRepository;
import com.trybe.modulecore.chat.repository.ChatRoomRepository;
import com.trybe.modulecore.user.entity.User;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(ChallengeParticipationRepository challengeParticipationRepository, ChatRoomRepository chatRoomRepository, ChatMessageRepository chatMessageRepository, SimpMessagingTemplate messagingTemplate) {
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public static final String CHAT_DESTINATION_PREFIX = "/sub/chat/challenges/";
    public static final String ENTER_MESSAGE = "[ %s ] 님이 입장하였습니다.";
    public static final String EXIT_MESSAGE = "[ %s ] 님이 퇴장하였습니다.";
    public static final String CHALLENGE_START_MESSAGE = "[ %s ] 챌린지가 시작되었습니다.";

    public void create(Challenge challenge){
        ChatRoom chatRoom = new ChatRoom(challenge);
        chatRoomRepository.save(chatRoom);
        String challengeStartMessage = createChallengeStartMessage(challenge.getTitle());
        ChatMessage chatMessage = createChatMessage(chatRoom, null, challengeStartMessage, MessageType.SYSTEM);
        chatMessageRepository.save(chatMessage);
    }

    @Transactional(readOnly = true)
    public List<ChatResponse.Message> findMessages(User user, Long challengeId, Long cursorId){
        validateExistsChatRoom(challengeId);
        validateExistsUserInChatRoom(challengeId, user, "챌린지에 참여한 회원이 아닙니다.");

        ChatRoom chatRoom = chatRoomRepository.findByChallengeId(challengeId);
        ChatMessage enterMessage = chatMessageRepository.findByChatRoomIdAndUserIdAndMessageType(user.getId(),chatRoom.getId(), MessageType.ENTER);

        Long latestId = chatMessageRepository.findLatestIdByChatRoomId(chatRoom.getId());
        cursorId = (cursorId == null) ? latestId + 1 : cursorId;
        LocalDateTime enterTime = (enterMessage != null) ? enterMessage.getCreatedAt() : LocalDateTime.of(1970, 1, 1, 0, 0);

        List<ChatMessage> messages = chatMessageRepository.findMessagesByCursorId(challengeId, cursorId, enterTime);
        List<ChatResponse.Message> messagesResponse = messages.stream()
                                                              .map(ChatResponse.Message::from)
                                                              .collect(Collectors.toList());
        return messagesResponse;
    }

    public void enter(User user, Long challengeId){
        ChatRoom chatRoom = chatRoomRepository.findByChallengeId(challengeId);
        String message = createEnterMessage(user.getUserId());
        ChatMessage chatMessage = createChatMessage(chatRoom, user, message, MessageType.ENTER);
        chatMessageRepository.save(chatMessage);
        ChatResponse.SystemMessage enterMessage = ChatResponse.SystemMessage.from(chatMessage);
        messagingTemplate.convertAndSend(CHAT_DESTINATION_PREFIX +  challengeId, enterMessage);
    }

    public void exit(User user, Long challengeId){
        ChatRoom chatRoom = chatRoomRepository.findByChallengeId(challengeId);
        String message = createExitMessage(user.getUserId());
        ChatMessage chatMessage = createChatMessage(chatRoom, user, message, MessageType.EXIT);
        chatMessageRepository.save(chatMessage);
        ChatResponse.SystemMessage enterMessage = ChatResponse.SystemMessage.from(chatMessage);
        messagingTemplate.convertAndSend(CHAT_DESTINATION_PREFIX +  challengeId, enterMessage);
    }

    public void delete(Long challengeId) {
        ChatRoom chatRoom = chatRoomRepository.findByChallengeId(challengeId);
        chatMessageRepository.deleteByChatRoomId(chatRoom.getId());
        chatRoomRepository.deleteById(chatRoom.getId());
    }

    @Transactional
    public void sendMessage(Long challengeId, User sendUser, ChatRequest.Send request){
        validateExistsUserInChatRoom(challengeId, sendUser, "챌린지에 참여한 회원만 메시지를 보낼 수 있습니다.");

        ChatRoom chatRoom = chatRoomRepository.findByChallengeId(challengeId);
        ChatMessage chatMessage = request.toEntity(chatRoom, sendUser, request.message());
        chatMessageRepository.save(chatMessage);
        ChatResponse.Message message = ChatResponse.Message.from(chatMessage);

        messagingTemplate.convertAndSend(CHAT_DESTINATION_PREFIX +  challengeId, message);

        /**
         * TODO
         * 해당 채팅방 유저들에게 채팅 도착 SSE 알림 전송
         */
    }

    private ChatMessage createChatMessage(ChatRoom chatRoom, User user, String message, MessageType messageType){
        return ChatMessage.builder()
                          .chatRoom(chatRoom)
                          .user(user)
                          .message(message)
                          .messageType(messageType)
                          .build();
    }

    private void validateExistsChatRoom(Long challengeId) {
        if (!chatRoomRepository.existsByChallengeId(challengeId)) {
            throw new NotFoundChatRoomException();
        }
    }

    private void validateExistsUserInChatRoom(Long challengeId, User user, String message){
        Long userId = user.getId();
        if (!challengeParticipationRepository.existsByUserIdAndChallengeId(userId, challengeId)) {
            throw new NotFoundChallengeParticipationException(message);
        }
    }

    private String createEnterMessage(String userId) {
        return String.format(ENTER_MESSAGE, userId);
    }

    private String createExitMessage(String userId) {
        return String.format(EXIT_MESSAGE, userId);
    }

    private String createChallengeStartMessage(String challengeTitle) {
        return String.format(CHALLENGE_START_MESSAGE, challengeTitle);
    }
}
