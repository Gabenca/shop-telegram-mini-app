package com.example.backend.event;

import com.example.backend.domain.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.telegram.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PartnerNotificationListener {

    private final TelegramBotService telegramBotService;
    private final UserRepository userRepository;

    @EventListener
    @Async
    @Transactional(readOnly = true)
    public void onMealPlanChanged(MealPlanChangedEvent event) {
        notifyPartner(event, "🍽 Партнёр обновил план питания");
    }

    @EventListener
    @Async
    @Transactional(readOnly = true)
    public void onShoppingListChanged(ShoppingListRegenerateEvent event) {
        notifyPartner(event, "🛒 Список покупок обновлён");
    }

    private void notifyPartner(DomainEvent event, String message) {
        List<User> users = userRepository.findByCoupleId(event.getCoupleId());
        for (User partner : users) {
            if (partner.getId().equals(event.getActorUserId())) continue;
            try {
                telegramBotService.sendMessage(partner.getTelegramId(), message);
            } catch (Exception e) {
                log.warn("Failed to notify partner {}: {}", partner.getTelegramId(), e.getMessage());
            }
        }
    }
}
