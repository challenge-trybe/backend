package com.trybe.modulecore.chat.repository;

import com.trybe.modulecore.chat.entity.ChatMessage;
import com.trybe.modulecore.chat.enums.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId AND m.id < :cursorId AND m.createdAt >= :enterTime ORDER BY m.id DESC")
    List<ChatMessage> findMessagesByCursorId(@Param("chatRoomId") Long chatRoomId, @Param("cursorId") Long cursorId, @Param("enterTime") LocalDateTime enterTime);

    void deleteByChatRoomId(Long chatRoomId);

    @Query("SELECT MAX(m.id) FROM ChatMessage m WHERE m.chatRoom.id = :chatRoomId")
    Long findLatestIdByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    ChatMessage findByChatRoomIdAndUserIdAndMessageType(Long userId, Long chatRoomId, MessageType messageType);
}
