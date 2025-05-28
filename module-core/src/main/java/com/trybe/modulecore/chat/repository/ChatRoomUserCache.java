package com.trybe.modulecore.chat.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class ChatRoomUserCache {
    private final StringRedisTemplate redisTemplate;

    private final String CHATROOM_SESSION_ID_REDIS_KEY = "chatroom:sessionId:%s"; // sessionId : chatRoomId
    private final String CHATROOM_OFFLINE_USERS_REDIS_KEY = "chatroom:%s:offline:users";

    public ChatRoomUserCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void online(String chatRoomId, String userId, String sessionId) {
        String sessionRoomKey = createSessionRoomKey(sessionId);
        redisTemplate.opsForValue().set(sessionRoomKey, chatRoomId);

        Long longChatRoomId = Long.valueOf(chatRoomId);
        String offlineUserKey = createOfflineUserRedisKey(longChatRoomId);
        redisTemplate.opsForSet().remove(offlineUserKey, userId);
    }

    public void offline(String userId, String sessionId) {
        String chatRoomId = getChatRoomIdBySessionId(sessionId);

        Long longChatRoomId = Long.valueOf(chatRoomId);
        String offlineUserKey = createOfflineUserRedisKey(longChatRoomId);
        redisTemplate.opsForSet().add(offlineUserKey, userId);

        String sessionRoomKey = createSessionRoomKey(sessionId);
        redisTemplate.delete(sessionRoomKey);
    }

    public void addUserToChatRoom(Long chatRoomId, String userId){
        Long longChatRoomId = Long.valueOf(chatRoomId);
        String offlineUserKey = createOfflineUserRedisKey(longChatRoomId);
        redisTemplate.opsForSet().add(offlineUserKey, userId);

    }

    public void deleteUserFromChatRoom(Long chatRoomId, String userId){
        Long longChatRoomId = Long.valueOf(chatRoomId);
        String offlineUserKey = createOfflineUserRedisKey(longChatRoomId);
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

    private String getChatRoomIdBySessionId(String sessionId){
        String redisKey = createSessionRoomKey(sessionId);
        return redisTemplate.opsForValue().get(redisKey);
    }

    private String createOfflineUserRedisKey(Long chatRoomId){
        return String.format(CHATROOM_OFFLINE_USERS_REDIS_KEY, chatRoomId);
    }

    private String createSessionRoomKey(String sessionId){
        return String.format(CHATROOM_SESSION_ID_REDIS_KEY, sessionId);
    }
}
