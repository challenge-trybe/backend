package com.trybe.moduleapi.chat.service;

import com.trybe.moduleapi.challenge.exception.participation.NotFoundChallengeParticipationException;
import com.trybe.moduleapi.chat.dto.ChatRequest;
import com.trybe.moduleapi.chat.dto.ChatResponse;
import com.trybe.moduleapi.chat.exception.NotFoundChatRoomException;
import com.trybe.moduleapi.common.dto.CursorResponse;
import com.trybe.moduleapi.notification.service.NotificationProducerService;
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
import com.trybe.modulecore.notification.enums.NotificationType;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Limit;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomUserCache chatRoomUserCache;
    private final UserService userService;
    private final NotificationProducerService notificationProducerService;

    public ChatService(ChallengeParticipationRepository challengeParticipationRepository, ChatRoomRepository chatRoomRepository, ChatMessageRepository chatMessageRepository, SimpMessagingTemplate messagingTemplate, ChatRoomUserCache chatRoomUserCache, UserService userService, NotificationProducerService notificationProducerService) {
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.messagingTemplate = messagingTemplate;
        this.chatRoomUserCache = chatRoomUserCache;
        this.userService = userService;
        this.notificationProducerService = notificationProducerService;
    }

    public static final String CHAT_DESTINATION_PREFIX = "/sub/chat/chatRoom/";
    public static final String ENTER_MESSAGE = "[ %s ] 님이 입장하였습니다.";
    public static final String EXIT_MESSAGE = "[ %s ] 님이 퇴장하였습니다.";
    public static final String CHALLENGE_INIT_MESSAGE = "[ %s ] 챌린지 단체 채팅방입니다";
    public static final String CHALLENGE_START_MESSAGE = "[ %s ] 챌린지가 시작되었습니다.";
    public static final String CHALLENGE_CLOSED_MESSAGE = "[ %s ] 챌린지가 종료되었습니다.";

    private static final String NOTICE_TITLE = "새로운 매시지가 도착했습니다.";
    private static final String USER_NOTICE_MESSAGE_FORMAT = "[%s] %s님의 새로운 메시지입니다.";
    private static final String SYSTEM_NOTICE_MESSAGE_FORMAT = "[%s] 새로운 시스템 메시지가 도착했습니다.";

    private static final int MESSAGE_LIMIT_SIZE = 20;

    @Transactional
    public void sendMessage(Long chatRoomId, User sender, ChatRequest.Send request) {
        validateExistsChatRoom(chatRoomId);
        validateExistsUserInChatRoom(chatRoomId, sender.getId(), "해당 채팅방에 속한 회원만 메시지를 보낼 수 있습니다.");

        ChatRoom chatRoom = getChatRoom(chatRoomId);
        ChatMessage chatMessage = request.toEntity(chatRoom, sender);
        chatMessageRepository.save(chatMessage);
        ChatResponse.Message message = ChatResponse.Message.from(chatMessage);

        messagingTemplate.convertAndSend(CHAT_DESTINATION_PREFIX + chatRoomId, message);
        notifyOfflineUsers(chatRoom, chatRoom.getChallenge(), sender);
    }

    @Transactional(readOnly = true)
    public CursorResponse<ChatResponse.Message> findMessages(User user, Long chatRoomId, Long cursorId) {
        validateExistsChatRoom(chatRoomId);
        validateExistsUserInChatRoom(chatRoomId, user.getId(), "해당 채팅방에 속한 회원만 메시지를 조회할 수 있습니다.");

        ChatRoom chatRoom = getChatRoom(chatRoomId);
        ChatMessage enterMessage = chatMessageRepository.findByChatRoomIdAndUserIdAndMessageType(user.getId(), chatRoom.getId(), MessageType.ENTER);

        Long latestId = chatMessageRepository.findLatestIdByChatRoomId(chatRoom.getId());
        cursorId = (cursorId == null) ? latestId + 1 : cursorId;
        LocalDateTime enterTime = (enterMessage != null) ? enterMessage.getCreatedAt() : LocalDateTime.of(1970, 1, 1, 0, 0);

        List<ChatMessage> messages = chatMessageRepository.findMessagesByCursorId(chatRoomId, cursorId, enterTime, Limit.of(MESSAGE_LIMIT_SIZE + 1));

        boolean hasNext = messages.size() > MESSAGE_LIMIT_SIZE;
        messages = hasNext ? messages.subList(0, MESSAGE_LIMIT_SIZE) : messages;

        Long nextCursor = hasNext ? messages.get(messages.size() - 1).getId() : null;

        List<ChatResponse.Message> messagesResponse = messages.stream()
                .map(ChatResponse.Message::from)
                .collect(Collectors.toList());
        return CursorResponse.of(messagesResponse, nextCursor, messagesResponse.size(), hasNext);
    }

    public Long create(Challenge challenge) {
        ChatRoom chatRoom = new ChatRoom(challenge);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        String challengeInitMessage = createChallengeInitMessage(challenge.getTitle());
        ChatMessage chatMessage = createChatMessage(chatRoom, null, challengeInitMessage, MessageType.SYSTEM);
        chatMessageRepository.save(chatMessage);

        return savedChatRoom.getId();
    }

    public void delete(Long challengeId) {
        ChatRoom chatRoom = getChatRoomByChallengeId(challengeId);
        Long chatRoomId = chatRoom.getId();

        chatRoomUserCache.clear(chatRoomId);
        chatMessageRepository.deleteByChatRoomId(chatRoomId);
        chatRoomRepository.deleteById(chatRoomId);
    }

    public void addUserToChatRoom(Long chatRoomId, User user){
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        Long challengeId = chatRoom.getChallenge().getId();
        String message = createEnterMessage(user.getNickname());

        ChatMessage chatMessage = createChatMessage(chatRoom, user, message, MessageType.ENTER);
        chatMessageRepository.save(chatMessage);

        chatRoomUserCache.addUserToChatRoom(chatRoomId, user.getUserId());
        ChatResponse.Message enterMessage = ChatResponse.Message.from(chatMessage);
        messagingTemplate.convertAndSend(CHAT_DESTINATION_PREFIX +  challengeId, enterMessage);
    }

    public void deleteUserFromChatRoom(Long chatRoomId, User user){
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        Long challengeId = chatRoom.getChallenge().getId();
        String message = createExitMessage(user.getNickname());

        ChatMessage chatMessage = createChatMessage(chatRoom, user, message, MessageType.EXIT);
        chatMessageRepository.save(chatMessage);

        chatRoomUserCache.deleteUserFromChatRoom(chatRoomId, user.getUserId());
        ChatResponse.Message exitMessage = ChatResponse.Message.from(chatMessage);
        messagingTemplate.convertAndSend(CHAT_DESTINATION_PREFIX +  challengeId, exitMessage);
    }

    public ChatRoom getChatRoomByChallengeId(Long challengeId) {
        return chatRoomRepository.findByChallengeId(challengeId)
                                 .orElseThrow(NotFoundChatRoomException::new);
    }

    public void sendChallengeStartMessage(Challenge challenge) {
        String message = String.format(CHALLENGE_START_MESSAGE, challenge.getTitle());
        ChatRoom chatRoom = getChatRoomByChallengeId(challenge.getId());

        ChatMessage chatMessage = createChatMessage(chatRoom, null, message, MessageType.SYSTEM);
        ChatResponse.Message startMessage = ChatResponse.Message.from(chatMessage);
        messagingTemplate.convertAndSend(CHAT_DESTINATION_PREFIX + challenge.getId(), startMessage);

        notifyOfflineUsers(chatRoom, chatRoom.getChallenge(), null);
    }

    public void sendChallengeClosedMessage(Challenge challenge) {
        String message = String.format(CHALLENGE_CLOSED_MESSAGE, challenge.getTitle());
        ChatRoom chatRoom = getChatRoomByChallengeId(challenge.getId());

        ChatMessage chatMessage = createChatMessage(chatRoom, null, message, MessageType.SYSTEM);
        ChatResponse.Message closedMessage = ChatResponse.Message.from(chatMessage);
        messagingTemplate.convertAndSend(CHAT_DESTINATION_PREFIX + challenge.getId(), closedMessage);

        notifyOfflineUsers(chatRoom, challenge, null);
        chatRoomUserCache.clear(chatRoom.getId());
    }

    private void notifyOfflineUsers(ChatRoom chatRoom, Challenge challenge, User sender) {
        String message = createNotifyMessage(challenge.getTitle(), sender);

        Set<String> offlineUserUserIds = chatRoomUserCache.findOfflineUserIds(chatRoom.getId());
        List<User> offlineUsers = userService.findByUserIdIn(offlineUserUserIds);

        Map<UUID, Notification> notificationMap = offlineUsers.stream()
                .collect(Collectors.toMap(
                        User::getUuid,
                        user -> new Notification(user.getId(), NotificationType.CHAT, chatRoom.getId(), NOTICE_TITLE, message)
                ));

        notificationProducerService.publishChatNotification(notificationMap);
    }

    private ChatRoom getChatRoom(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(NotFoundChatRoomException::new);
    }

    private String createNotifyMessage(String challengeTitle, User sender) {
        if (sender == null) {
            return String.format(SYSTEM_NOTICE_MESSAGE_FORMAT, challengeTitle);
        }
        return String.format(USER_NOTICE_MESSAGE_FORMAT, challengeTitle, sender.getNickname());
    }

    private ChatMessage createChatMessage(ChatRoom chatRoom, User user, String message, MessageType messageType) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .user(user)
                .message(message)
                .messageType(messageType)
                .build();
    }

    private String createEnterMessage(String nickname) {
        return String.format(ENTER_MESSAGE, nickname);
    }

    private String createExitMessage(String nickname) {
        return String.format(EXIT_MESSAGE, nickname);
    }

    private String createChallengeInitMessage(String challengeTitle) {
        return String.format(CHALLENGE_INIT_MESSAGE, challengeTitle);
    }

    private void validateExistsChatRoom(Long chatRoomId) {
        if (!chatRoomRepository.existsById(chatRoomId)) {
            throw new NotFoundChatRoomException();
        }
    }

    private void validateExistsUserInChatRoom(Long chatRoomId, Long userId, String message) {
        ChatRoom chatRoom = getChatRoom(chatRoomId);
        Long challengeId = chatRoom.getChallenge().getId();
        if (!challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(userId, challengeId, ParticipationStatus.ACCEPTED)) {
            throw new NotFoundChallengeParticipationException(message);
        }
    }
}
