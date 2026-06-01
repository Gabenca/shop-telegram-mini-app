package com.example.backend.controller;

import com.example.backend.dto.CreateRecipeRequest;
import com.example.backend.dto.RecipeDto;
import com.example.backend.service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping
    public List<RecipeDto> getAllRecipes() {
        return recipeService.getAllRecipes();
    }

    @GetMapping("/{id}")
    public RecipeDto getRecipeById(@PathVariable Long id) {
        return recipeService.getRecipeById(id);
    }

    @PostMapping
    public RecipeDto createRecipe(@RequestBody @Valid CreateRecipeRequest request) {
        return recipeService.createRecipe(request);
    }

    @PutMapping("/{id}")
    public RecipeDto updateRecipe(@PathVariable Long id, @RequestBody @Valid CreateRecipeRequest request) {
        return recipeService.updateRecipe(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
    }
}
