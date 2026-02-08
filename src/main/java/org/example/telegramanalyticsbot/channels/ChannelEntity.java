package org.example.telegramanalyticsbot.channels;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "channels",
        uniqueConstraints = @UniqueConstraint(columnNames = "username"))

public class ChannelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(name = "owner_chat_id", nullable = false)
    private Long ownerChatId; // who added

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ChannelEntity(String username, Long ownerChatId) {
        this.username = username;
        this.ownerChatId = ownerChatId;
        this.createdAt = LocalDateTime.now();
    }

}
