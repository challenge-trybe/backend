package com.trybe.modulecore.chat.repository;

import com.trybe.modulecore.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    ChatRoom findByChallengeId(Long challengeId);
}
