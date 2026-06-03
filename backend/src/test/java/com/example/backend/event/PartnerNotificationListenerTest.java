package com.example.backend.event;

import com.example.backend.domain.Couple;
import com.example.backend.domain.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.telegram.TelegramBotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartnerNotificationListenerTest {

    @Mock
    private TelegramBotService telegramBotService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PartnerNotificationListener listener;

    @Test
    void onMealPlanChanged_shouldNotifyPartnerOnly() {
        Couple couple = Couple.builder().id(1L).build();
        User actor = User.builder().id(10L).telegramId(111L).couple(couple).build();
        User partner = User.builder().id(20L).telegramId(222L).couple(couple).build();
        when(userRepository.findByCoupleId(1L)).thenReturn(List.of(actor, partner));

        listener.onMealPlanChanged(new MealPlanChangedEvent(1L, 10L));

        verify(telegramBotService, times(1)).sendMessage(222L, "🍽 Партнёр обновил план питания");
        verify(telegramBotService, never()).sendMessage(111L, "🍽 Партнёр обновил план питания");
    }

    @Test
    void onShoppingListChanged_shouldNotifyPartnerOnly() {
        Couple couple = Couple.builder().id(1L).build();
        User actor = User.builder().id(10L).telegramId(111L).couple(couple).build();
        User partner = User.builder().id(20L).telegramId(222L).couple(couple).build();
        when(userRepository.findByCoupleId(1L)).thenReturn(List.of(actor, partner));

        listener.onShoppingListChanged(new ShoppingListRegenerateEvent(1L, 10L));

        verify(telegramBotService, times(1)).sendMessage(222L, "🛒 Список покупок обновлён");
        verify(telegramBotService, never()).sendMessage(111L, "🛒 Список покупок обновлён");
    }

    @Test
    void onMealPlanChanged_soloUser_shouldNotNotifyAnyone() {
        Couple couple = Couple.builder().id(1L).build();
        User solo = User.builder().id(10L).telegramId(111L).couple(couple).build();
        when(userRepository.findByCoupleId(1L)).thenReturn(List.of(solo));

        listener.onMealPlanChanged(new MealPlanChangedEvent(1L, 10L));

        verify(telegramBotService, never()).sendMessage(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
    }
}
