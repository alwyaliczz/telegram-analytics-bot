package org.example.telegramanalyticsbot.config;

import lombok.SneakyThrows;
import org.example.telegramanalyticsbot.bot.TgBot;
import org.example.telegramanalyticsbot.channels.ChannelRepository;
import org.example.telegramanalyticsbot.channels.ChannelService;
import org.example.telegramanalyticsbot.channels.ChannelValidator;
import org.example.telegramanalyticsbot.commandHandlers.CommandHandler;
import org.example.telegramanalyticsbot.commandHandlers.MessageSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfiguration {
    @Bean
    @SneakyThrows
    public TgBot tgBot(
            @Value("${telegram.bot.token}") String botToken,
            TelegramBotsApi telegramBotsApi,
            @Value("${telegram.bot.username}") String username,
            CommandHandler handler,
            MessageSender messageSender,
            ChannelValidator validator

    ){
        var botOptions = new DefaultBotOptions();

        var bot = new TgBot(botOptions, botToken, username, handler, messageSender, validator);

        if (botToken == null || botToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Bot token cannot be empty!");
        }

        telegramBotsApi.registerBot(bot);
        return bot;
    }

    @Bean
    @SneakyThrows
    public TelegramBotsApi telegramBotsApi(){
        return new TelegramBotsApi(DefaultBotSession.class);
    }
}
