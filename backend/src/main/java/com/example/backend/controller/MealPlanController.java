package com.example.backend.controller;

import com.example.backend.dto.CreateMealPlanEntryRequest;
import com.example.backend.dto.MealPlanEntryDto;
import com.example.backend.service.MealPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/meal-plan")
@RequiredArgsConstructor
public class MealPlanController {

    private final MealPlanService mealPlanService;

    @GetMapping
    public List<MealPlanEntryDto> getMealPlanForWeek(@RequestParam LocalDate weekStart) {
        return mealPlanService.getMealPlanForWeek(weekStart);
    }

    @PostMapping
    public MealPlanEntryDto addMealPlanEntry(@RequestBody @Valid CreateMealPlanEntryRequest request) {
        return mealPlanService.addMealPlanEntry(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMealPlanEntry(@PathVariable Long id) {
        mealPlanService.deleteMealPlanEntry(id);
    }
}
