package org.example.telegramanalyticsbot.channels;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class SubscribersService {
    private final HttpClient httpClient;

    public SubscribersService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public Long getSubscriberCount(String username) {
        try {
            String channelName = username.startsWith("@") ? username.substring(1) : username;
            String url = "https://t.me/" + channelName;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseSubscribers(response.body());
            }
        } catch (Exception e) {
            log.error("Error getting subscribers: {}", e.getMessage());
        }
        return null;
    }

    private Long parseSubscribers(String html) {
        try {

            Pattern pattern = Pattern.compile(
                    "<div class=\"tgme_page_extra\">([^<]+)</div>"
            );
            Matcher matcher = pattern.matcher(html);

            if (matcher.find()) {
                String text = matcher.group(1);

                Pattern numPattern = Pattern.compile("([\\d\\s.,]+)\\s*(subscribers?|members?|participants?)");
                Matcher numMatcher = numPattern.matcher(text);

                if (numMatcher.find()) {
                    String number = numMatcher.group(1);
                    String multiplier = numMatcher.group(2);

                    number = number.replaceAll("[,\\s]", "");

                    double value = Double.parseDouble(number);

                    if (multiplier.equals("K")) {
                        return (long) (value * 1000);
                    } else if (multiplier.equals("M")) {
                        return (long) (value * 1000000);
                    } else {
                        return (long) value;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse subscribers: {}", e.getMessage());
        }
        return null;
    }
}