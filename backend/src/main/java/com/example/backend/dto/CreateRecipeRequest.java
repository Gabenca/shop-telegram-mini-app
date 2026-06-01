package com.example.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class CreateRecipeRequest {

    @NotBlank
    private String name;

    private String description;

    private String photoUrl;

    private String instructions;

    @Valid
    private List<IngredientRequest> ingredients;
}
