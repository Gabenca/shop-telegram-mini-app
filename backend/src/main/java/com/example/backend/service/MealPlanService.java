package com.example.backend.service;

import com.example.backend.domain.*;
import com.example.backend.dto.*;
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
    public List<MealPlanEntryDto> addMealPlanEntries(List<CreateMealPlanEntryRequest> requests, Long coupleId) {
        Couple couple = coupleRepository.findById(coupleId)
            .orElseThrow(() -> new ResourceNotFoundException("Couple not found"));

        return requests.stream()
            .map(request -> {
                MealPlanEntry entry = MealPlanEntry.builder()
                        .date(request.getDate())
                        .mealType(request.getMealType())
                        .couple(couple)
                        .build();

                List<MealPlanEntryDish> dishes = request.getDishes().stream()
                    .map(dishRequest -> createDish(dishRequest, entry, coupleId))
                    .collect(Collectors.toList());
                
                entry.setDishes(dishes);
                MealPlanEntry saved = mealPlanEntryRepository.save(entry);
                return mapToDto(saved);
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public MealPlanEntryDto updateMealPlanEntry(Long entryId, CreateMealPlanEntryRequest request, Long coupleId) {
        MealPlanEntry entry = mealPlanEntryRepository.findById(entryId)
            .orElseThrow(() -> new ResourceNotFoundException("Entry not found: " + entryId));
        
        if (!entry.getCouple().getId().equals(coupleId)) {
            throw new AccessDeniedException("Access denied");
        }

        entry.getDishes().clear();
        List<MealPlanEntryDish> newDishes = request.getDishes().stream()
            .map(dishRequest -> createDish(dishRequest, entry, coupleId))
            .collect(Collectors.toList());
        entry.setDishes(newDishes);
        
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

    @Transactional
    public void deleteDish(Long dishId, Long coupleId) {
        MealPlanEntry entry = mealPlanEntryRepository.findByDishesId(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found"));
        
        if (!entry.getCouple().getId().equals(coupleId)) {
            throw new AccessDeniedException("Access denied");
        }
        
        entry.getDishes().removeIf(d -> d.getId().equals(dishId));
        mealPlanEntryRepository.save(entry);
    }

    private MealPlanEntryDish createDish(CreateDishRequest request, MealPlanEntry entry, Long coupleId) {
        MealPlanEntryDish.MealPlanEntryDishBuilder builder = MealPlanEntryDish.builder()
                .mealPlanEntry(entry)
                .sortOrder(request.getSortOrder());

        if (request.getRecipeId() != null) {
            Recipe recipe = recipeRepository.findById(request.getRecipeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Recipe not found: " + request.getRecipeId()));
            if (!recipe.getCouple().getId().equals(coupleId)) {
                throw new AccessDeniedException("Recipe does not belong to your couple");
            }
            builder.recipe(recipe);
        } else {
            builder.manualName(request.getManualName())
                  .manualQuantity(request.getManualQuantity())
                  .manualUnit(request.getManualUnit());
        }

        return builder.build();
    }

    private MealPlanEntryDto mapToDto(MealPlanEntry entry) {
        MealPlanEntryDto dto = new MealPlanEntryDto();
        dto.setId(entry.getId());
        dto.setDate(entry.getDate());
        dto.setMealType(entry.getMealType());
        dto.setDishes(entry.getDishes().stream()
                .map(this::mapDishToDto)
                .collect(Collectors.toList()));
        return dto;
    }

    private MealPlanEntryDishDto mapDishToDto(MealPlanEntryDish dish) {
        MealPlanEntryDishDto dto = new MealPlanEntryDishDto();
        dto.setId(dish.getId());
        dto.setSortOrder(dish.getSortOrder());
        if (dish.getRecipe() != null) {
            dto.setRecipeId(dish.getRecipe().getId());
            dto.setRecipeName(dish.getRecipe().getName());
        } else {
            dto.setManualName(dish.getManualName());
            dto.setManualQuantity(dish.getManualQuantity());
            dto.setManualUnit(dish.getManualUnit());
        }
        return dto;
    }
}