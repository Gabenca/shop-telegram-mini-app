package com.example.backend.controller;

import com.example.backend.domain.User;
import com.example.backend.dto.CreateMealPlanEntriesRequest;
import com.example.backend.dto.CreateMealPlanEntryRequest;
import com.example.backend.dto.MealPlanEntryDto;
import com.example.backend.service.MealPlanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/meal-plan")
@RequiredArgsConstructor
@Validated
public class MealPlanController {

    private final MealPlanService mealPlanService;

    @GetMapping
    public ResponseEntity<List<MealPlanEntryDto>> getMealPlanForWeek(
            @RequestParam LocalDate weekStart,
            User user) {
        if (user.getCouple() == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<MealPlanEntryDto> mealPlan = mealPlanService.getMealPlanForWeek(weekStart, user.getCouple().getId());
        return ResponseEntity.ok(mealPlan);
    }

    @PostMapping
    public ResponseEntity<MealPlanEntryDto> addMealPlanEntry(
            @Valid @RequestBody CreateMealPlanEntryRequest createRequest,
            User user) {
        if (user.getCouple() == null) {
            return ResponseEntity.badRequest().build();
        }

        MealPlanEntryDto entry = mealPlanService.addMealPlanEntries(List.of(createRequest), user.getCouple().getId()).get(0);
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<MealPlanEntryDto>> addMealPlanEntries(
            @Valid @RequestBody CreateMealPlanEntriesRequest createRequest,
            User user) {
        if (user.getCouple() == null) {
            return ResponseEntity.badRequest().build();
        }

        List<MealPlanEntryDto> entries = mealPlanService.addMealPlanEntries(createRequest.getEntries(), user.getCouple().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(entries);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MealPlanEntryDto> updateMealPlanEntry(
            @PathVariable @Positive Long id,
            @Valid @RequestBody CreateMealPlanEntryRequest updateRequest,
            User user) {
        if (user.getCouple() == null) {
            return ResponseEntity.badRequest().build();
        }

        MealPlanEntryDto entry = mealPlanService.updateMealPlanEntry(id, updateRequest, user.getCouple().getId());
        return ResponseEntity.ok(entry);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMealPlanEntry(@PathVariable @Positive Long id, User user) {
        if (user.getCouple() == null) {
            throw new IllegalStateException("User not in a couple");
        }
        mealPlanService.deleteMealPlanEntry(id, user.getCouple().getId());
    }

    @DeleteMapping("/dishes/{dishId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDish(@PathVariable @Positive Long dishId, User user) {
        if (user.getCouple() == null) {
            throw new IllegalStateException("User not in a couple");
        }
        mealPlanService.deleteDish(dishId, user.getCouple().getId());
    }
}