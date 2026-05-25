package com.pms.repository;

import com.pms.entity.Message;
import com.pms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Message} entities (direct messaging / chat).
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Retrieves the full chat history between two users, ordered by sentAt.
     * Uses an OR condition so it works regardless of who sent which message.
     */
    @Query("SELECT m FROM Message m " +
           "WHERE (m.sender = :user1 AND m.receiver = :user2) " +
           "   OR (m.sender = :user2 AND m.receiver = :user1) " +
           "ORDER BY m.sentAt ASC")
    Page<Message> findChatHistory(
            @Param("user1") User user1,
            @Param("user2") User user2,
            Pageable pageable);

    /** Count unread messages received by a user. */
    long countByReceiverAndReadStatusFalse(User receiver);
}
