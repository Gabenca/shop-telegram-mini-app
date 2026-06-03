package com.example.backend.service;

import com.example.backend.domain.*;
import com.example.backend.dto.CreateManualItemRequest;
import com.example.backend.dto.ShoppingListItemDto;
import com.example.backend.event.ShoppingListRegenerateEvent;
import com.example.backend.repository.CoupleRepository;
import com.example.backend.repository.MealPlanEntryRepository;
import com.example.backend.repository.ShoppingListItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    @Mock
    private ShoppingListItemRepository shoppingListItemRepository;

    @Mock
    private MealPlanEntryRepository mealPlanEntryRepository;

    @Mock
    private CoupleRepository coupleRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ShoppingListService shoppingListService;

    @Test
    void addManualItem_shouldReturnDto() {
        CreateManualItemRequest request = new CreateManualItemRequest();
        request.setWeekStartDate(LocalDate.now());
        request.setIngredientName("Milk");
        request.setTotalQuantity(1.0);
        request.setUnit(Unit.MILLILITER);

        when(coupleRepository.findById(1L)).thenReturn(Optional.of(new Couple()));
        when(shoppingListItemRepository.save(any())).thenAnswer(inv -> {
            ShoppingListItem i = inv.getArgument(0);
            i.setId(1L);
            return i;
        });

        ShoppingListItemDto result = shoppingListService.addManualItem(request, LocalDate.now(), 1L);

        assertThat(result.getIngredientName()).isEqualTo("Milk");
        assertThat(result.isManual()).isTrue();
    }

    @Test
    void getShoppingListForWeek_shouldReturnItems() {
        LocalDate weekStart = LocalDate.now();
        when(shoppingListItemRepository.findByWeekStartDateAndCoupleId(weekStart, 1L))
            .thenReturn(List.of());

        List<ShoppingListItemDto> result = shoppingListService.getShoppingListForWeek(weekStart, 1L);

        assertThat(result).isEmpty();
    }

    @Test
    void regenerateShoppingList_shouldPublishEvent() {
        LocalDate weekStart = LocalDate.now();
        Couple couple = Couple.builder().id(1L).build();
        when(mealPlanEntryRepository.findByDateBetweenAndCoupleId(any(), any(), any())).thenReturn(List.of());
        when(shoppingListItemRepository.findByWeekStartDateAndCoupleId(weekStart, 1L)).thenReturn(List.of());
        when(coupleRepository.findById(1L)).thenReturn(Optional.of(couple));

        shoppingListService.regenerateShoppingList(weekStart, 1L, 10L);

        ArgumentCaptor<ShoppingListRegenerateEvent> captor = ArgumentCaptor.forClass(ShoppingListRegenerateEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getCoupleId()).isEqualTo(1L);
        assertThat(captor.getValue().getActorUserId()).isEqualTo(10L);
    }
}
