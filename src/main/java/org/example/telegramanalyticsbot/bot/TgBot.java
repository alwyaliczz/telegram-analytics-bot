package org.example.telegramanalyticsbot.bot;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.telegramanalyticsbot.channels.ChannelEntity;
import org.example.telegramanalyticsbot.channels.ChannelInfo;
import org.example.telegramanalyticsbot.channels.ChannelValidator;
import org.example.telegramanalyticsbot.commandHandlers.CommandHandler;
import org.example.telegramanalyticsbot.commandHandlers.MessageSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Slf4j
public class TgBot extends TelegramLongPollingBot {
    private final String token;
    private final String botUsername;
    private final CommandHandler handler;
    private final MessageSender sender;
    private final ChannelValidator validator;

    public TgBot(DefaultBotOptions options, String token, String botUsername, CommandHandler handler, MessageSender sender, ChannelValidator validator) {
        super(options);
        this.token = token;
        this.botUsername = botUsername;
        this.handler = handler;
        this.sender = sender;
        this.validator = validator;
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            var text = update.getMessage().getText();
            var chatId = update.getMessage().getChatId();

            log.info("message received from {}: {}", chatId, text);

            String[] parts = text.split(" ", 2);
            String command = parts[0];
            String arguments = parts.length > 1 ? parts[1] : "";

            switch(command){
                case "/start":
                    handleStartCommand(chatId, update.getMessage().getFrom().getFirstName());
                    break;
                case "/my-channels":
                    handleGetAllChannels(chatId);
                    break;
                case "/add":
                    handleAddChannelCommand(chatId, arguments);
                    break;
                case "/remove":
                    removeChannelCommand(chatId, arguments);
                    break;
                case "/info":
                    getChannelInfo(chatId, arguments);
                    break;
                default:
                    //todo: make a proper non-command reply
                    sender.sendMessage(this, chatId, "default reply");
            }
        }
    }
    private void handleGetAllChannels(Long chatId){
        //todo make a normal message
        String message = handler.getAllChannelsByChatId(chatId).toString();
        sender.sendMessage(this, chatId, message);

    }
    private void handleStartCommand(Long chatId, String name){
        sender.sendMessage(this, chatId, handler.startMessage(name) );
    }
    private void handleAddChannelCommand(Long chatId, String arguments) {
        if (arguments.trim().isEmpty()) {
            sender.sendMessage(this, chatId, "use: /add @username");
            return;
        }

        String username = arguments.trim();
        ChannelInfo info = validator.getChannelInfo(username);

        if (!info.isExists()) {
            sender.sendMessage(this, chatId, "channel: " + username + " doesn't exist");
            return;
        }

        if (!info.isPublic()) {
            sender.sendMessage(this, chatId, "channel: " + username + " is private");
            return;
        }


        boolean added = handler.addChannel(username, chatId);
        if (added){
            String showSubs = info.getSubscribers() != null
                    ? String.format("Subscribers: %,d", info.getSubscribers())
                    : "Subscribers: ???";
            String response = "channel: " + username + " has been added, "+showSubs;
            sender.sendMessage(this, chatId, response);
        }
        else{
            sender.sendMessage(this, chatId, "channel is already in your list");
        }
    }

    private void removeChannelCommand(Long chatId, String arguments) {
        if (arguments.trim().isEmpty()) {
            sender.sendMessage(this, chatId, "use: /remove @username");
            return;
        }
        String username = arguments.trim();
        handler.removeChannel(username, chatId);
        String response = "channel: " + username + " has been removed";
        sender.sendMessage(this, chatId, response);
        //todo make a proper error response (/remove [doesn't exist])
    }

    private void getChannelInfo(Long chatId, String arguments){
        try {
            if (arguments.trim().isEmpty()) {
                sender.sendMessage(this, chatId, "use: /get-info (@)username");
                return;
            }
            String username = arguments.trim();
            ChannelEntity channel = handler.getChannelInfo(username, chatId);

            ChannelInfo info = validator.getChannelInfo(channel.getUsername());
            String response =
                    info.getTitle()+"\n"+
                    "username: "+ info.getUsername()+"\n"+
                     String.format("subscribers: %,d", info.getSubscribers());

            sender.sendMessage(this, chatId, response);
        } catch (Exception e) {
            sender.sendMessage(this, chatId, "channel not found in your list");
        }

    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return token;
    }

}
