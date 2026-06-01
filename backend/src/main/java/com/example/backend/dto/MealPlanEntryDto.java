package com.example.backend.dto;

import com.example.backend.domain.MealType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MealPlanEntryDto {

    private Long id;
    private LocalDate date;
    private Long recipeId;
    private String recipeName;
    private MealType mealType;
}
