package com.pms.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Represents a direct message between two users.
 *
 * <p>Messages are persisted for chat history and also delivered
 * in real-time via WebSocket when both parties are online.</p>
 */
@Entity
@Table(name = "messages")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Message extends BaseEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Builder.Default
    @Column(name = "read_status", nullable = false)
    private Boolean readStatus = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;
}
