package com.example.backend.service;

import com.example.backend.domain.MealPlanEntry;
import com.example.backend.domain.Recipe;
import com.example.backend.dto.CreateMealPlanEntryRequest;
import com.example.backend.dto.MealPlanEntryDto;
import com.example.backend.repository.MealPlanEntryRepository;
import com.example.backend.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealPlanService {

    private final MealPlanEntryRepository mealPlanEntryRepository;
    private final RecipeRepository recipeRepository;

    @Transactional(readOnly = true)
    public List<MealPlanEntryDto> getMealPlanForWeek(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return mealPlanEntryRepository.findByDateBetween(weekStart, weekEnd).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MealPlanEntryDto addMealPlanEntry(CreateMealPlanEntryRequest request) {
        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new RuntimeException("Recipe not found: " + request.getRecipeId()));

        MealPlanEntry entry = MealPlanEntry.builder()
                .date(request.getDate())
                .recipe(recipe)
                .mealType(request.getMealType())
                .build();

        MealPlanEntry saved = mealPlanEntryRepository.save(entry);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteMealPlanEntry(Long id) {
        mealPlanEntryRepository.deleteById(id);
    }

    private MealPlanEntryDto mapToDto(MealPlanEntry entry) {
        MealPlanEntryDto dto = new MealPlanEntryDto();
        dto.setId(entry.getId());
        dto.setDate(entry.getDate());
        dto.setRecipeId(entry.getRecipe().getId());
        dto.setRecipeName(entry.getRecipe().getName());
        dto.setMealType(entry.getMealType());
        return dto;
    }
}
