package com.example.backend.dto;

import com.example.backend.domain.Unit;
import lombok.Data;

@Data
public class MealPlanEntryDishDto {
    private Long id;
    private Long recipeId;
    private String recipeName;
    private String manualName;
    private Double manualQuantity;
    private Unit manualUnit;
    private Integer sortOrder;
}