package com.example.backend.dto;

import com.example.backend.domain.Unit;
import lombok.Data;

@Data
public class IngredientDto {

    private Long id;
    private String name;
    private Double weightInGrams;
    private Unit unit;
}
