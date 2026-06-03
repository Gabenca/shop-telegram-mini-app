package com.example.backend.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/telegram")
@Slf4j
public class TelegramWebhookController {

    @PostMapping("/webhook")
    public Map<String, String> onUpdate(@RequestBody Map<String, Object> update) {
        log.info("Telegram webhook received update id={}", update.get("update_id"));
        return Map.of("status", "ok");
    }
}
