package com.example.backend.dto;

import com.example.backend.domain.Unit;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RecipeDto {

    private Long id;
    private String name;
    private String description;
    private String photoUrl;
    private String instructions;
    private List<IngredientDto> ingredients;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
