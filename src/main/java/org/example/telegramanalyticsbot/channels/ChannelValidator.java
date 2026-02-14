package org.example.telegramanalyticsbot.channels;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ChannelValidator {
    private final HttpClient httpClient;
    private final SubscribersService subscriberService;

    public ChannelValidator(SubscribersService subscriberService) {
        this.subscriberService = subscriberService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    @SneakyThrows
    public boolean isChannelAvailable(String username) {
        ChannelInfo info = getChannelInfo(username);
        return info.isExists() && info.isPublic();
    }

    public ChannelInfo getChannelInfo(String username) {
        try {
            String channelName = username.startsWith("@")
                    ? username.substring(1)
                    : username;

            String url = "https://t.me/" + channelName;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                log.warn("Channel {} not found (404)", username);
                return ChannelInfo.builder()
                        .username(username)
                        .isExists(false)
                        .isPublic(false)
                        .build();
            }

            if (response.statusCode() == 200) {
                String html = response.body();

                Long subs = subscriberService.getSubscriberCount(username);

                boolean isChannelPage = html.contains("tgme_channel_info") ||
                        html.contains("tgme_page") ||
                        html.contains("channel_info");

                boolean isPlaceholder = html.contains("If you have Telegram, you can contact") ||
                        html.contains("You can view and join") ||
                        html.contains("right away");

                boolean isPrivate = html.contains("private") &&
                        (html.contains("channel") || html.contains("group"));

                boolean hasSubscribers = html.contains("subscriber") ||
                        html.contains("member") ||
                        html.contains("participants");

                boolean exists = isChannelPage && (!isPlaceholder || hasSubscribers);

                if (!exists) {
                    log.info("Channel {} does not exist - isChannelPage: {}, isPlaceholder: {}, hasSubscribers: {}",
                            username, isChannelPage, isPlaceholder, hasSubscribers);
                    return ChannelInfo.builder()
                            .username(username)
                            .isExists(false)
                            .isPublic(false)
                            .build();
                }

                String title = extractTitle(html);

                boolean isPublic = !isPrivate && !html.contains("Private channel");

                log.info("Channel {} found - title: {}, isPublic: {}", username, title, isPublic);

                return ChannelInfo.builder()
                        .username("@" + channelName)
                        .title(title)
                        .isExists(true)
                        .isPublic(isPublic)
                        .subscribers(subs)
                        .build();
            }

            log.warn("Channel {} returned unexpected status code: {}", username, response.statusCode());
            return ChannelInfo.builder()
                    .username(username)
                    .isExists(false)
                    .isPublic(false)
                    .build();

        } catch (Exception e) {
            log.error("Error getting channel info for {}: {}", username, e.getMessage());
            return ChannelInfo.builder()
                    .username(username)
                    .isExists(false)
                    .isPublic(false)
                    .build();
        }
    }

    private String extractTitle(String html) {

        // meta og:title
        Pattern metaPattern = Pattern.compile("<meta property=\"og:title\" content=\"([^\"]+)\"");
        Matcher metaMatcher = metaPattern.matcher(html);
        if (metaMatcher.find()) {
            return cleanTitle(metaMatcher.group(1));
        }

        // tgme_page_title
        Pattern pageTitlePattern = Pattern.compile("<div[^>]*class=\"tgme_page_title\"[^>]*>([^<]+)</div>");
        Matcher pageTitleMatcher = pageTitlePattern.matcher(html);
        if (pageTitleMatcher.find()) {
            return cleanTitle(pageTitleMatcher.group(1));
        }

        // title
        Pattern titlePattern = Pattern.compile("<title>(.*?)</title>");
        Matcher titleMatcher = titlePattern.matcher(html);
        if (titleMatcher.find()) {
            String title = titleMatcher.group(1);
            title = title.replace("Telegram: Contact @", "").trim();
            return title;
        }

        return null;
    }

    private String cleanTitle(String title) {
        if (title == null) return null;
        return title.replaceAll("<[^>]*>", "").trim();
    }
}
