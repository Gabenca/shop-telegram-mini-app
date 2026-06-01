package com.example.backend.service;

import com.example.backend.domain.Ingredient;
import com.example.backend.domain.Recipe;
import com.example.backend.domain.Unit;
import com.example.backend.dto.CreateRecipeRequest;
import com.example.backend.dto.RecipeDto;
import com.example.backend.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;

    @Transactional
    public RecipeDto createRecipe(CreateRecipeRequest request) {
        Recipe recipe = Recipe.builder()
                .name(request.getName())
                .description(request.getDescription())
                .photoUrl(request.getPhotoUrl())
                .instructions(request.getInstructions())
                .build();

        if (request.getIngredients() != null) {
            List<Ingredient> ingredients = request.getIngredients().stream()
                    .map(i -> Ingredient.builder()
                            .name(i.getName())
                            .weightInGrams(i.getWeightInGrams())
                            .unit(Unit.valueOf(i.getUnit()))
                            .recipe(recipe)
                            .build())
                    .collect(Collectors.toList());
            recipe.setIngredients(ingredients);
        }

        Recipe saved = recipeRepository.save(recipe);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<RecipeDto> getAllRecipes() {
        return recipeRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RecipeDto getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found: " + id));
        return mapToDto(recipe);
    }

    @Transactional
    public RecipeDto updateRecipe(Long id, CreateRecipeRequest request) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found: " + id));

        recipe.setName(request.getName());
        recipe.setDescription(request.getDescription());
        recipe.setPhotoUrl(request.getPhotoUrl());
        recipe.setInstructions(request.getInstructions());

        recipe.getIngredients().clear();
        if (request.getIngredients() != null) {
            List<Ingredient> ingredients = request.getIngredients().stream()
                    .map(i -> Ingredient.builder()
                            .name(i.getName())
                            .weightInGrams(i.getWeightInGrams())
                            .unit(Unit.valueOf(i.getUnit()))
                            .recipe(recipe)
                            .build())
                    .collect(Collectors.toList());
            recipe.getIngredients().addAll(ingredients);
        }

        Recipe saved = recipeRepository.save(recipe);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteRecipe(Long id) {
        recipeRepository.deleteById(id);
    }

    private RecipeDto mapToDto(Recipe recipe) {
        RecipeDto dto = new RecipeDto();
        dto.setId(recipe.getId());
        dto.setName(recipe.getName());
        dto.setDescription(recipe.getDescription());
        dto.setPhotoUrl(recipe.getPhotoUrl());
        dto.setInstructions(recipe.getInstructions());
        dto.setCreatedAt(recipe.getCreatedAt());
        dto.setUpdatedAt(recipe.getUpdatedAt());
        dto.setIngredients(recipe.getIngredients().stream()
                .map(i -> {
                    var idto = new com.example.backend.dto.IngredientDto();
                    idto.setId(i.getId());
                    idto.setName(i.getName());
                    idto.setWeightInGrams(i.getWeightInGrams());
                    idto.setUnit(i.getUnit());
                    return idto;
                })
                .collect(Collectors.toList()));
        return dto;
    }
}
