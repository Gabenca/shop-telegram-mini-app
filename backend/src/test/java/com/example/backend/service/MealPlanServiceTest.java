package com.example.backend.service;

import com.example.backend.domain.Couple;
import com.example.backend.domain.MealPlanEntry;
import com.example.backend.domain.MealType;
import com.example.backend.domain.Recipe;
import com.example.backend.dto.CreateDishRequest;
import com.example.backend.dto.CreateMealPlanEntryRequest;
import com.example.backend.dto.MealPlanEntryDto;
import com.example.backend.event.MealPlanChangedEvent;
import com.example.backend.repository.CoupleRepository;
import com.example.backend.repository.MealPlanEntryRepository;
import com.example.backend.repository.RecipeRepository;
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
class MealPlanServiceTest {

    @Mock
    private MealPlanEntryRepository mealPlanEntryRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private CoupleRepository coupleRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MealPlanService mealPlanService;

    @Test
    void addMealPlanEntries_shouldReturnDtos() {
        Couple couple = Couple.builder().id(1L).build();
        Recipe recipe = Recipe.builder().id(1L).name("Pasta").couple(couple).build();
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(coupleRepository.findById(1L)).thenReturn(Optional.of(couple));
        when(mealPlanEntryRepository.save(any())).thenAnswer(inv -> {
            MealPlanEntry e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        CreateMealPlanEntryRequest request = new CreateMealPlanEntryRequest();
        request.setDate(LocalDate.now());
        request.setMealType(MealType.LUNCH);
        CreateDishRequest dish = new CreateDishRequest();
        dish.setRecipeId(1L);
        dish.setSortOrder(0);
        request.setDishes(List.of(dish));

        List<MealPlanEntryDto> result = mealPlanService.addMealPlanEntries(List.of(request), 1L, 10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMealType()).isEqualTo(MealType.LUNCH);
    }

    @Test
    void addMealPlanEntries_shouldPublishMealPlanChangedEvent() {
        Couple couple = Couple.builder().id(1L).build();
        when(coupleRepository.findById(1L)).thenReturn(Optional.of(couple));
        when(mealPlanEntryRepository.save(any())).thenAnswer(inv -> {
            MealPlanEntry e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        CreateMealPlanEntryRequest request = new CreateMealPlanEntryRequest();
        request.setDate(LocalDate.now());
        request.setMealType(MealType.DINNER);
        CreateDishRequest dish = new CreateDishRequest();
        dish.setManualName("Soup");
        dish.setSortOrder(0);
        request.setDishes(List.of(dish));

        mealPlanService.addMealPlanEntries(List.of(request), 1L, 10L);

        ArgumentCaptor<MealPlanChangedEvent> captor = ArgumentCaptor.forClass(MealPlanChangedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getCoupleId()).isEqualTo(1L);
        assertThat(captor.getValue().getActorUserId()).isEqualTo(10L);
    }

    @Test
    void getMealPlanForWeek_shouldReturnEntries() {
        LocalDate start = LocalDate.now();
        when(mealPlanEntryRepository.findByWeekWithDishes(start, start.plusDays(6), 1L))
            .thenReturn(List.of());

        List<MealPlanEntryDto> result = mealPlanService.getMealPlanForWeek(start, 1L);

        assertThat(result).isEmpty();
    }
}
