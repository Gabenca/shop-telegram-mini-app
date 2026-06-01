package com.example.backend.controller;

import com.example.backend.dto.CreateManualItemRequest;
import com.example.backend.dto.ShoppingListItemDto;
import com.example.backend.service.ShoppingListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shopping-list")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @GetMapping
    public List<ShoppingListItemDto> getShoppingListForWeek(@RequestParam LocalDate weekStart) {
        return shoppingListService.getShoppingListForWeek(weekStart);
    }

    @PostMapping("/regenerate")
    public List<ShoppingListItemDto> regenerateShoppingList(@RequestParam LocalDate weekStart) {
        return shoppingListService.regenerateShoppingList(weekStart);
    }

    @PostMapping("/items")
    public ShoppingListItemDto addManualItem(@RequestBody @Valid CreateManualItemRequest request) {
        return shoppingListService.addManualItem(request);
    }

    @PatchMapping("/items/{id}")
    public ShoppingListItemDto updateItem(@PathVariable Long id, @RequestBody ShoppingListItemDto dto) {
        return shoppingListService.updateItem(id, dto);
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long id) {
        shoppingListService.deleteItem(id);
    }
}
