package com.example.backend.dto;

import com.example.backend.domain.MealType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MealPlanEntryDto {
    private Long id;
    private LocalDate date;
    private MealType mealType;
    private List<MealPlanEntryDishDto> dishes;
}