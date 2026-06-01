package com.example.backend.controller;

import com.example.backend.dto.CreateRecipeRequest;
import com.example.backend.dto.RecipeDto;
import com.example.backend.service.RecipeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecipeController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecipeService recipeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllRecipes_shouldReturn200() throws Exception {
        when(recipeService.getAllRecipes()).thenReturn(List.of(new RecipeDto()));

        mockMvc.perform(get("/api/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createRecipe_shouldReturn200() throws Exception {
        CreateRecipeRequest request = new CreateRecipeRequest();
        request.setName("Test");
        
        RecipeDto dto = new RecipeDto();
        dto.setId(1L);
        dto.setName("Test");

        when(recipeService.createRecipe(any())).thenReturn(dto);

        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
