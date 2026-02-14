package org.example.telegramanalyticsbot.channels;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChannelService {
    private final ChannelRepository repository;
    private final ChannelValidator validator;
    private final SubscribersService subscribersService;

    @Value("${app.channels.max-per-user}")
    private int maxChannels;

    @Transactional
    public ChannelEntity addChannel(String username, Long ownerChatId) {
        log.info("add channel = {}, user = {}", username, ownerChatId);
        String normalizedUsername = normalizeUsername(username);

        if (!validator.isChannelAvailable(normalizedUsername)) {
            throw new IllegalArgumentException("Channel not found or private: " + normalizedUsername);
        }

        if (repository.existsByUsernameAndOwnerChatId(normalizedUsername, ownerChatId)) {
            throw new IllegalStateException("Channel already added: " + normalizedUsername);
        }

        long count = repository.countChannelEntitiesByOwnerChatId(ownerChatId);
        if (count >= maxChannels) {
            throw new IllegalStateException("Max channels limit reached: " + maxChannels);
        }

        ChannelEntity channel = ChannelEntity.builder()
                .username(normalizedUsername)
                .ownerChatId(ownerChatId)
                .createdAt(LocalDateTime.now())
                .build();

        ChannelEntity saved = repository.save(channel);
        log.info("channel added: {}, id = {}", normalizedUsername, saved.getId());
        return saved;
    }

    @Transactional()
    public List<ChannelEntity> getChannelsByUser(Long ownerChatId) {
        return repository.findChannelEntityByOwnerChatId(ownerChatId);
    }

    @Transactional
    public void removeChannel(String username, Long ownerChatId) {
        String normalized = normalizeUsername(username);

        ChannelEntity channel = repository.findByUsernameAndOwnerChatId(normalized, ownerChatId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found: " + normalized));

        repository.delete(channel);
        log.info("channel removed: {}", normalized);
    }

    public ChannelEntity getInfo(String username, Long ownerChatId){
        String normalized = normalizeUsername(username);
        ChannelEntity entity = repository.findByUsernameAndOwnerChatId(normalized, ownerChatId)
                .orElseThrow(() -> new EntityNotFoundException("Channel not found: " + normalized));

        return entity;
    }


    private String normalizeUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        String trimmed = username.trim();
        return trimmed.startsWith("@") ? trimmed.toLowerCase() : "@" + trimmed.toLowerCase();
    }
}
