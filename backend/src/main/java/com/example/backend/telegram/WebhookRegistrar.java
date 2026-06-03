package com.example.backend.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookRegistrar implements ApplicationRunner {

    @Value("${telegram.webhook.enabled:false}")
    private boolean enabled;

    @Value("${telegram.webhook.url:}")
    private String webhookUrl;

    @Value("${telegram.bot-token:}")
    private String botToken;

    private final WebClient.Builder webClientBuilder;

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("Telegram webhook registration skipped (telegram.webhook.enabled=false)");
            return;
        }
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Telegram webhook is enabled but telegram.webhook.url is empty; skipping registration");
            return;
        }
        if (botToken == null || botToken.isBlank()) {
            log.warn("Telegram webhook is enabled but telegram.bot-token is empty; skipping registration");
            return;
        }

        String url = "https://api.telegram.org/bot" + botToken + "/setWebhook";
        try {
            WebClient webClient = webClientBuilder.build();
            Map<?, ?> response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("url", webhookUrl))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response != null && Boolean.TRUE.equals(response.get("ok"))) {
                log.info("Telegram webhook registered: {}", webhookUrl);
            } else {
                log.warn("Telegram webhook registration failed: {}", response);
            }
        } catch (Exception e) {
            log.error("Telegram webhook registration error: {}", e.getMessage());
        }
    }
}
