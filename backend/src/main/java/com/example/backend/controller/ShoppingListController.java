package com.example.backend.controller;

import com.example.backend.domain.User;
import com.example.backend.dto.CreateManualItemRequest;
import com.example.backend.dto.ShoppingListItemDto;
import com.example.backend.service.ShoppingListService;
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
@RequestMapping("/api/v1/shopping-list")
@RequiredArgsConstructor
@Validated
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @GetMapping
    public ResponseEntity<List<ShoppingListItemDto>> getShoppingListForWeek(
            @RequestParam LocalDate weekStart,
            User user) {
        if (user.getCouple() == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<ShoppingListItemDto> shoppingList = shoppingListService.getShoppingListForWeek(weekStart, user.getCouple().getId());
        return ResponseEntity.ok(shoppingList);
    }

    @PostMapping("/regenerate")
    public ResponseEntity<List<ShoppingListItemDto>> regenerateShoppingList(
            @RequestParam LocalDate weekStart,
            User user) {
        if (user.getCouple() == null) {
            return ResponseEntity.badRequest().build();
        }

        List<ShoppingListItemDto> shoppingList = shoppingListService.regenerateShoppingList(weekStart, user.getCouple().getId());
        return ResponseEntity.ok(shoppingList);
    }

    @PostMapping("/items")
    public ResponseEntity<ShoppingListItemDto> addManualItem(
            @Valid @RequestBody CreateManualItemRequest createRequest,
            @RequestParam LocalDate weekStart,
            User user) {
        if (user.getCouple() == null) {
            return ResponseEntity.badRequest().build();
        }

        ShoppingListItemDto item = shoppingListService.addManualItem(createRequest, weekStart, user.getCouple().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PatchMapping("/items/{id}")
    public ResponseEntity<ShoppingListItemDto> updateItem(@PathVariable @Positive Long id, @RequestBody ShoppingListItemDto dto, User user) {
        if (user.getCouple() == null) {
            throw new IllegalStateException("User not in a couple");
        }
        ShoppingListItemDto result = shoppingListService.updateItem(id, dto, user.getCouple().getId());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable @Positive Long id, User user) {
        if (user.getCouple() == null) {
            throw new IllegalStateException("User not in a couple");
        }
        shoppingListService.deleteItem(id, user.getCouple().getId());
    }
}
