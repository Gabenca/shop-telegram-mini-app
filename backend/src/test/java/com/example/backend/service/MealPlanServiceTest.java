package com.example.backend.service;

import com.example.backend.domain.MealPlanEntry;
import com.example.backend.domain.MealType;
import com.example.backend.domain.Recipe;
import com.example.backend.dto.CreateMealPlanEntryRequest;
import com.example.backend.dto.MealPlanEntryDto;
import com.example.backend.repository.MealPlanEntryRepository;
import com.example.backend.repository.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {

    @Mock
    private MealPlanEntryRepository mealPlanEntryRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private MealPlanService mealPlanService;

    @Test
    void addMealPlanEntry_shouldReturnDto() {
        Recipe recipe = Recipe.builder().id(1L).name("Pasta").build();
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(mealPlanEntryRepository.save(any())).thenAnswer(inv -> {
            MealPlanEntry e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        CreateMealPlanEntryRequest request = new CreateMealPlanEntryRequest();
        request.setDate(LocalDate.now());
        request.setRecipeId(1L);
        request.setMealType(MealType.LUNCH);

        MealPlanEntryDto result = mealPlanService.addMealPlanEntry(request);

        assertThat(result.getRecipeName()).isEqualTo("Pasta");
        assertThat(result.getMealType()).isEqualTo(MealType.LUNCH);
    }

    @Test
    void getMealPlanForWeek_shouldReturnEntries() {
        LocalDate start = LocalDate.now();
        Recipe recipe = Recipe.builder().id(1L).name("Pasta").build();
        MealPlanEntry entry = MealPlanEntry.builder().id(1L).date(start).recipe(recipe).mealType(MealType.DINNER).build();
        when(mealPlanEntryRepository.findByDateBetween(start, start.plusDays(6))).thenReturn(List.of(entry));

        List<MealPlanEntryDto> result = mealPlanService.getMealPlanForWeek(start);

        assertThat(result).hasSize(1);
    }
}
