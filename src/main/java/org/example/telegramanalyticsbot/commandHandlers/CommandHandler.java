package org.example.telegramanalyticsbot.commandHandlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.telegramanalyticsbot.channels.ChannelEntity;
import org.example.telegramanalyticsbot.channels.ChannelService;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class CommandHandler {
    private final ChannelService channelService;

    public String startMessage(String name){
        return "hi, "+name;
    }
    public boolean addChannel(String channelName, Long userChatId){
        channelService.addChannel(channelName, userChatId);
        return true;
    }
    public List<ChannelEntity> getAllChannelsByChatId(Long userChatId){
        return channelService.getChannelsByUser(userChatId);
    }
    public void removeChannel(String channelName, Long userChatId){
        channelService.removeChannel(channelName, userChatId);
    }
    public ChannelEntity getChannelInfo(String username, Long userChatId){
        return channelService.getInfo(username, userChatId);
    }
}
