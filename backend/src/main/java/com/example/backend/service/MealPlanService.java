package com.example.backend.service;

import com.example.backend.domain.Couple;
import com.example.backend.domain.MealPlanEntry;
import com.example.backend.domain.Recipe;
import com.example.backend.dto.CreateMealPlanEntryRequest;
import com.example.backend.dto.MealPlanEntryDto;
import com.example.backend.exception.AccessDeniedException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.CoupleRepository;
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
    private final CoupleRepository coupleRepository;

    @Transactional(readOnly = true)
    public List<MealPlanEntryDto> getMealPlanForWeek(LocalDate weekStart, Long coupleId) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return mealPlanEntryRepository.findByDateBetweenAndCoupleId(weekStart, weekEnd, coupleId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MealPlanEntryDto addMealPlanEntry(CreateMealPlanEntryRequest request, Long coupleId) {
        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found: " + request.getRecipeId()));

        if (!recipe.getCouple().getId().equals(coupleId)) {
            throw new AccessDeniedException("Recipe does not belong to your couple");
        }

        Couple couple = coupleRepository.findById(coupleId)
            .orElseThrow(() -> new ResourceNotFoundException("Couple not found"));

        MealPlanEntry entry = MealPlanEntry.builder()
                .date(request.getDate())
                .recipe(recipe)
                .mealType(request.getMealType())
                .couple(couple)
                .build();

        MealPlanEntry saved = mealPlanEntryRepository.save(entry);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteMealPlanEntry(Long id, Long coupleId) {
        MealPlanEntry entry = mealPlanEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal plan entry not found: " + id));
        
        if (!entry.getCouple().getId().equals(coupleId)) {
            throw new AccessDeniedException("Access denied");
        }
        
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
