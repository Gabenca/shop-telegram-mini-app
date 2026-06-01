package com.example.backend.controller;

import com.example.backend.domain.User;
import com.example.backend.dto.CreateMealPlanEntryRequest;
import com.example.backend.dto.MealPlanEntryDto;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.MealPlanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/meal-plan")
@RequiredArgsConstructor
public class MealPlanController {

    private final MealPlanService mealPlanService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<MealPlanEntryDto>> getMealPlanForWeek(
            @RequestParam LocalDate weekStart,
            HttpServletRequest request) {
        User user = getUserFromRequest(request);
        if (user.getCouple() == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<MealPlanEntryDto> mealPlan = mealPlanService.getMealPlanForWeek(weekStart, user.getCouple().getId());
        return ResponseEntity.ok(mealPlan);
    }

    @PostMapping
    public ResponseEntity<MealPlanEntryDto> addMealPlanEntry(
            @Valid @RequestBody CreateMealPlanEntryRequest createRequest,
            HttpServletRequest request) {
        User user = getUserFromRequest(request);
        if (user.getCouple() == null) {
            return ResponseEntity.badRequest().build();
        }

        MealPlanEntryDto entry = mealPlanService.addMealPlanEntry(createRequest, user.getCouple().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMealPlanEntry(@PathVariable Long id) {
        mealPlanService.deleteMealPlanEntry(id);
    }

    private User getUserFromRequest(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
