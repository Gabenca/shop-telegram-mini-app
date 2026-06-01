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

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<RecipeDto>> getAllRecipes(HttpServletRequest request) {
        User user = getUserFromRequest(request);
        if (user.getCouple() == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<RecipeDto> recipes = recipeService.getAllRecipes(user.getCouple().getId());
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/{id}")
    public RecipeDto getRecipeById(@PathVariable Long id, HttpServletRequest request) {
        User user = getUserFromRequest(request);
        if (user.getCouple() == null) {
            throw new IllegalStateException("User not in a couple");
        }
        return recipeService.getRecipeById(id, user.getCouple().getId());
    }

    @PostMapping
    public ResponseEntity<RecipeDto> createRecipe(
            @Valid @RequestBody CreateRecipeRequest createRequest,
            HttpServletRequest request) {
        User user = getUserFromRequest(request);
        if (user.getCouple() == null) {
            return ResponseEntity.badRequest().build();
        }

        RecipeDto recipe = recipeService.createRecipe(createRequest, user.getCouple().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(recipe);
    }

    @PutMapping("/{id}")
    public RecipeDto updateRecipe(@PathVariable Long id, @RequestBody @Valid CreateRecipeRequest request, HttpServletRequest httpRequest) {
        User user = getUserFromRequest(httpRequest);
        if (user.getCouple() == null) {
            throw new IllegalStateException("User not in a couple");
        }
        return recipeService.updateRecipe(id, request, user.getCouple().getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecipe(@PathVariable Long id, HttpServletRequest request) {
        User user = getUserFromRequest(request);
        if (user.getCouple() == null) {
            throw new IllegalStateException("User not in a couple");
        }
        recipeService.deleteRecipe(id, user.getCouple().getId());
    }

    private User getUserFromRequest(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
