package org.example.telegramanalyticsbot.channels;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<ChannelEntity, Long> {
    List<ChannelEntity> findChannelEntityByOwnerChatId(Long ownerChatId);

    Optional<ChannelEntity> findChannelEntityById(Long id);
    Optional<ChannelEntity> findByUsernameAndOwnerChatId(String username, Long ownerChatId);
    boolean existsByUsernameAndOwnerChatId(String username, Long ownerChatId);
    Long countChannelEntitiesByOwnerChatId(Long ownerChatId);


}


