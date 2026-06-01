package com.example.backend.service;

import com.example.backend.domain.Couple;
import com.example.backend.domain.Recipe;
import com.example.backend.domain.Unit;
import com.example.backend.dto.CreateRecipeRequest;
import com.example.backend.dto.IngredientRequest;
import com.example.backend.dto.RecipeDto;
import com.example.backend.repository.CoupleRepository;
import com.example.backend.repository.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private CoupleRepository coupleRepository;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    void createRecipe_shouldReturnRecipeDto() {
        CreateRecipeRequest request = new CreateRecipeRequest();
        request.setName("Test Recipe");
        request.setDescription("Desc");
        IngredientRequest ing = new IngredientRequest();
        ing.setName("Flour");
        ing.setWeightInGrams(500.0);
        ing.setUnit("GRAM");
        request.setIngredients(List.of(ing));

        when(coupleRepository.findById(1L)).thenReturn(Optional.of(new Couple()));
        when(recipeRepository.save(any())).thenAnswer(inv -> {
            Recipe r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        RecipeDto result = recipeService.createRecipe(request, 1L);

        assertThat(result.getName()).isEqualTo("Test Recipe");
        assertThat(result.getIngredients()).hasSize(1);
        assertThat(result.getIngredients().get(0).getName()).isEqualTo("Flour");
    }

    @Test
    void getAllRecipes_shouldReturnList() {
        Recipe recipe = Recipe.builder().id(1L).name("Pasta").build();
        when(recipeRepository.findByCoupleId(1L)).thenReturn(List.of(recipe));

        List<RecipeDto> result = recipeService.getAllRecipes(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Pasta");
    }

    @Test
    void getRecipeById_shouldReturnRecipeDto() {
        Couple couple = Couple.builder().id(1L).build();
        Recipe recipe = Recipe.builder().id(1L).name("Pasta").couple(couple).build();
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        RecipeDto result = recipeService.getRecipeById(1L, 1L);

        assertThat(result.getName()).isEqualTo("Pasta");
    }
}
