package com.example.backend.dto;

import com.example.backend.domain.Unit;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateDishRequest {
    private Long recipeId;
    private String manualName;
    private Double manualQuantity;
    private Unit manualUnit;

    @NotNull
    private Integer sortOrder;
}