package org.example.telegramanalyticsbot.channels;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChannelInfo {
    private String username;
    private String title;
    private boolean isExists;
    private boolean isPublic;
    private Long subscribers;
}
