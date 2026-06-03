package com.example.backend.dto;

import com.example.backend.domain.MealType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateMealPlanEntryRequest {

    @NotNull
    private LocalDate date;

    @NotNull
    private MealType mealType;

    @NotNull
    private List<CreateDishRequest> dishes;
}