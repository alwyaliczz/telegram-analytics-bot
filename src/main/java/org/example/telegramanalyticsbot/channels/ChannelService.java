package org.example.telegramanalyticsbot.channels;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChannelService {
    private final ChannelRepository repository;

    @Value("${app.channels.max-per-user}")
    private int maxChannels;

    @Transactional
    public ChannelEntity addChannel(String username, Long ownerChatId){
        log.info("add channel = {}, user = {}", username, ownerChatId);
        String normalizedUsername = normalizeUsername(username);
        isAvailable(normalizedUsername, ownerChatId);
        canAddChannel(ownerChatId);

        ChannelEntity channel = ChannelEntity.builder()
                .username(normalizedUsername)
                .ownerChatId(ownerChatId)
                .build();
        ChannelEntity saved = repository.save(channel);
        log.info("channel: {} added, id = {}", normalizedUsername, saved.getId());
        return saved;
    }

    @Transactional
    public List<ChannelEntity> getChannelsByUser(Long ownerChatId){
        return repository.findChannelEntityByOwnerChatId(ownerChatId);
    }

    public void removeChannel(String username,Long ownerChatId){
        String normalized = normalizeUsername(username);

        ChannelEntity channel = repository.findByUsernameAndOwnerChatId(username, ownerChatId)
                .orElseThrow(() -> new IllegalArgumentException("channel "+normalized+" not found"));
        repository.delete(channel);
        log.info("channel: {} deleted", normalized);
    }


    private String normalizeUsername(String username) {
        if (username.startsWith("@")) {
            return username.toLowerCase();
        } else {
            return "@" + username.toLowerCase();
        }
    }
    private void isAvailable(String username, Long ownerChatId){
        String user = normalizeUsername(username);
        if(repository.existsByUsernameAndOwnerChatId(username, ownerChatId)){
            throw new IllegalStateException("channel "+username+" already added");
        }
    }
    private void canAddChannel(Long ownerChatId){
        long count = repository.countChannelEntitiesByOwnerChatId(ownerChatId);
        if (count >= maxChannels){
            throw new IllegalStateException("max amount of channels exceeded");
        }
    }
}
