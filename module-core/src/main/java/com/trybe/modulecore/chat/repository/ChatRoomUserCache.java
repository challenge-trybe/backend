package com.trybe.modulecore.chat.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class ChatRoomUserCache {
    private final StringRedisTemplate redisTemplate;

    private final String CHATROOM_SESSION_ID_REDIS_KEY = "chatroom:sessionId:%s"; // sessionId : chatRoomId
    private final String CHATROOM_OFFLINE_USERS_REDIS_KEY = "chatroom:%d:offline:users";

    public ChatRoomUserCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void online(Long chatRoomId, String userId, String sessionId) {
        String sessionRoomKey = createSessionRoomKey(sessionId);
        redisTemplate.opsForValue().set(sessionRoomKey, String.valueOf(chatRoomId));

        String offlineUserKey = createOfflineUserRedisKey(chatRoomId);
        redisTemplate.opsForSet().remove(offlineUserKey, userId);
    }

    public void offline(String userId, String sessionId) {
        Long chatRoomId = getChatRoomIdBySessionId(sessionId);

        String offlineUserKey = createOfflineUserRedisKey(chatRoomId);
        redisTemplate.opsForSet().add(offlineUserKey, userId);

        String sessionRoomKey = createSessionRoomKey(sessionId);
        redisTemplate.delete(sessionRoomKey);
    }

    public void addUserToChatRoom(Long chatRoomId, String userId){
        String offlineUserKey = createOfflineUserRedisKey(chatRoomId);
        redisTemplate.opsForSet().add(offlineUserKey, userId);

    }

    public void deleteUserFromChatRoom(Long chatRoomId, String userId){
        String offlineUserKey = createOfflineUserRedisKey(chatRoomId);
        redisTemplate.opsForSet().remove(offlineUserKey, userId);
    }

    public void clear(Long chatRoomId){
        String offlineUserRedisKey = createOfflineUserRedisKey(chatRoomId);
        redisTemplate.delete(offlineUserRedisKey);
    }

    public Set<String> findOfflineUserIds(Long chatRoomId){
        String redisKey = createOfflineUserRedisKey(chatRoomId);
        return redisTemplate.opsForSet().members(redisKey);
    }

    private Long getChatRoomIdBySessionId(String sessionId){
        String sessionRoomKey = createSessionRoomKey(sessionId);
        return Long.valueOf(redisTemplate.opsForValue().get(sessionRoomKey));
    }

    private String createOfflineUserRedisKey(Long chatRoomId){
        return String.format(CHATROOM_OFFLINE_USERS_REDIS_KEY, chatRoomId);
    }

    private String createSessionRoomKey(String sessionId){
        return String.format(CHATROOM_SESSION_ID_REDIS_KEY, sessionId);
    }
}
