package com.example.backend.controller;

import com.example.backend.dto.MealPlanEntryDto;
import com.example.backend.service.MealPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MealPlanController.class)
@AutoConfigureMockMvc(addFilters = false)
class MealPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MealPlanService mealPlanService;

    @Test
    void getMealPlanForWeek_shouldReturn200() throws Exception {
        when(mealPlanService.getMealPlanForWeek(LocalDate.of(2026, 6, 1)))
                .thenReturn(List.of(new MealPlanEntryDto()));

        mockMvc.perform(get("/api/meal-plan").param("weekStart", "2026-06-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
