package com.example.backend.controller;

import com.example.backend.domain.User;
import com.example.backend.dto.CreateRecipeRequest;
import com.example.backend.dto.RecipeDto;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.RecipeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

package com.example.backend.controller;

import com.example.backend.domain.User;
import com.example.backend.dto.CreateRecipeRequest;
import com.example.backend.dto.RecipeDto;
import com.example.backend.service.RecipeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
@Validated
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping
    public ResponseEntity<List<RecipeDto>> getAllRecipes(User user) {
        if (user.getCouple() == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<RecipeDto> recipes = recipeService.getAllRecipes(user.getCouple().getId());
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDto> getRecipeById(@PathVariable @Positive Long id, User user) {
        if (user.getCouple() == null) {
            throw new IllegalStateException("User not in a couple");
        }
        RecipeDto dto = recipeService.getRecipeById(id, user.getCouple().getId());
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<RecipeDto> createRecipe(
            @Valid @RequestBody CreateRecipeRequest createRequest,
            User user) {
        if (user.getCouple() == null) {
            return ResponseEntity.badRequest().build();
        }

        RecipeDto recipe = recipeService.createRecipe(createRequest, user.getCouple().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(recipe);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeDto> updateRecipe(@PathVariable @Positive Long id, @RequestBody @Valid CreateRecipeRequest request, User user) {
        if (user.getCouple() == null) {
            throw new IllegalStateException("User not in a couple");
        }
        RecipeDto dto = recipeService.updateRecipe(id, request, user.getCouple().getId());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecipe(@PathVariable @Positive Long id, User user) {
        if (user.getCouple() == null) {
            throw new IllegalStateException("User not in a couple");
        }
        recipeService.deleteRecipe(id, user.getCouple().getId());
    }
}
