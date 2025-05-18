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
        String sessionIdAndUserMapping = createRedisKey(sessionId);
        redisTemplate.opsForValue().set(sessionIdAndUserMapping, userId);

        Long longChatRoomId = Long.valueOf(chatRoomId);
        String offlineUserRedisKey = createRedisKey(longChatRoomId);
        redisTemplate.opsForSet().remove(offlineUserRedisKey, userId);
    }

    public void offline(String userId, String sessionId) {
        String chatRoomId = getChatRoomIdBySessionId(sessionId);

        Long longChatRoomId = Long.valueOf(chatRoomId);
        String offlineUserRedisKey = createRedisKey(longChatRoomId);
        redisTemplate.opsForSet().add(offlineUserRedisKey, userId);

        String sessionIdAndUserMapping = createRedisKey(sessionId);
        redisTemplate.delete(sessionIdAndUserMapping);
    }

    public void addUserToChatRoom(Long chatRoomId, String userId){
        Long longChatRoomId = Long.valueOf(chatRoomId);
        String offlineUserRedisKey = createRedisKey(longChatRoomId);
        redisTemplate.opsForSet().add(offlineUserRedisKey, userId);

    }

    public void deleteUserToChatRoom(Long chatRoomId, String userName){
        Long longChatRoomId = Long.valueOf(chatRoomId);
        String offlineUserRedisKey = createRedisKey(longChatRoomId);
        redisTemplate.opsForSet().add(offlineUserRedisKey, userName);
    }

    public void clear(Long chatRoomId){
        String offlineUserRedisKey = createRedisKey(chatRoomId);
        redisTemplate.delete(offlineUserRedisKey);

    }

    public Set<String> findOfflineUserIds(Long chatRoomId){
        String redisKey = createRedisKey(chatRoomId);
        return redisTemplate.opsForSet().members(redisKey);
    }

    private String getChatRoomIdBySessionId(String sessionId){
        String redisKey = createRedisKey(sessionId);
        return redisTemplate.opsForValue().get(redisKey);
    }

    private String createRedisKey(Long chatRoomId){
        return String.format(CHATROOM_OFFLINE_USERS_REDIS_KEY, chatRoomId);
    }

    private String createRedisKey(String sessionId){
        return String.format(CHATROOM_SESSION_ID_REDIS_KEY, sessionId);
    }
}
