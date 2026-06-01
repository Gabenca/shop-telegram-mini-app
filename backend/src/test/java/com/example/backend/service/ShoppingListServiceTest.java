package com.example.backend.service;

import com.example.backend.domain.*;
import com.example.backend.dto.CreateManualItemRequest;
import com.example.backend.dto.ShoppingListItemDto;
import com.example.backend.repository.MealPlanEntryRepository;
import com.example.backend.repository.ShoppingListItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    @Mock
    private ShoppingListItemRepository shoppingListItemRepository;

    @Mock
    private MealPlanEntryRepository mealPlanEntryRepository;

    @InjectMocks
    private ShoppingListService shoppingListService;

    @Test
    void regenerateShoppingList_shouldAggregateIngredients() {
        LocalDate weekStart = LocalDate.now();
        Recipe recipe = Recipe.builder().id(1L).name("Pasta").ingredients(new ArrayList<>()).build();
        recipe.getIngredients().add(Ingredient.builder().name("Flour").weightInGrams(500.0).unit(Unit.GRAM).build());
        recipe.getIngredients().add(Ingredient.builder().name("Flour").weightInGrams(300.0).unit(Unit.GRAM).build());

        MealPlanEntry entry = MealPlanEntry.builder().date(weekStart).recipe(recipe).build();

        when(mealPlanEntryRepository.findByDateBetween(weekStart, weekStart.plusDays(6))).thenReturn(List.of(entry));
        when(shoppingListItemRepository.findByWeekStartDate(weekStart)).thenReturn(List.of());
        when(shoppingListItemRepository.saveAll(any())).thenAnswer(inv -> {
            java.util.Collection<ShoppingListItem> items = inv.getArgument(0);
            return new java.util.ArrayList<>(items);
        });

        List<ShoppingListItemDto> result = shoppingListService.regenerateShoppingList(weekStart);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTotalQuantity()).isEqualTo(800.0);
    }

    @Test
    void addManualItem_shouldReturnDto() {
        CreateManualItemRequest request = new CreateManualItemRequest();
        request.setWeekStartDate(LocalDate.now());
        request.setIngredientName("Milk");
        request.setTotalQuantity(1.0);
        request.setUnit(Unit.MILLILITER);

        when(shoppingListItemRepository.save(any())).thenAnswer(inv -> {
            ShoppingListItem i = inv.getArgument(0);
            i.setId(1L);
            return i;
        });

        ShoppingListItemDto result = shoppingListService.addManualItem(request);

        assertThat(result.getIngredientName()).isEqualTo("Milk");
        assertThat(result.isManual()).isTrue();
    }
}
