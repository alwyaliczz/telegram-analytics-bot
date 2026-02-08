package org.example.telegramanalyticsbot.commandHandlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageSender{

    public void sendMessage(TelegramLongPollingBot bot, long chatId, String text){
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        executeMessage(bot, sendMessage);
    }

    private void executeMessage(TelegramLongPollingBot bot, SendMessage sendMessage) {
        try{
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.info("Error sending message: {}", e.getMessage());
        }
    }


}
