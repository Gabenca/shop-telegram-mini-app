package com.example.backend.controller;

import com.example.backend.domain.User;
import com.example.backend.dto.CreateManualItemRequest;
import com.example.backend.dto.ShoppingListItemDto;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ShoppingListService;
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
@RequestMapping("/api/shopping-list")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<ShoppingListItemDto>> getShoppingListForWeek(
            @RequestParam LocalDate weekStart,
            HttpServletRequest request) {
        User user = getUserFromRequest(request);
        if (user.getCouple() == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<ShoppingListItemDto> shoppingList = shoppingListService.getShoppingListForWeek(weekStart, user.getCouple().getId());
        return ResponseEntity.ok(shoppingList);
    }

    @PostMapping("/regenerate")
    public ResponseEntity<List<ShoppingListItemDto>> regenerateShoppingList(
            @RequestParam LocalDate weekStart,
            HttpServletRequest request) {
        User user = getUserFromRequest(request);
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
            HttpServletRequest request) {
        User user = getUserFromRequest(request);
        if (user.getCouple() == null) {
            return ResponseEntity.badRequest().build();
        }

        ShoppingListItemDto item = shoppingListService.addManualItem(createRequest, weekStart, user.getCouple().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PatchMapping("/items/{id}")
    public ShoppingListItemDto updateItem(@PathVariable Long id, @RequestBody ShoppingListItemDto dto, HttpServletRequest request) {
        User user = getUserFromRequest(request);
        if (user.getCouple() == null) {
            throw new IllegalStateException("User not in a couple");
        }
        return shoppingListService.updateItem(id, dto, user.getCouple().getId());
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long id, HttpServletRequest request) {
        User user = getUserFromRequest(request);
        if (user.getCouple() == null) {
            throw new IllegalStateException("User not in a couple");
        }
        shoppingListService.deleteItem(id, user.getCouple().getId());
    }

    private User getUserFromRequest(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
