package com.example.backend.service;

import com.example.backend.domain.Couple;
import com.example.backend.domain.MealPlanEntry;
import com.example.backend.domain.ShoppingListItem;
import com.example.backend.dto.CreateManualItemRequest;
import com.example.backend.dto.ShoppingListItemDto;
import com.example.backend.event.ShoppingListRegenerateEvent;
import com.example.backend.exception.AccessDeniedException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.CoupleRepository;
import com.example.backend.repository.MealPlanEntryRepository;
import com.example.backend.repository.ShoppingListItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final ShoppingListItemRepository shoppingListItemRepository;
    private final MealPlanEntryRepository mealPlanEntryRepository;
    private final CoupleRepository coupleRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<ShoppingListItemDto> getShoppingListForWeek(LocalDate weekStart, Long coupleId) {
        return shoppingListItemRepository.findByWeekStartDateAndCoupleId(weekStart, coupleId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ShoppingListItemDto> regenerateShoppingList(LocalDate weekStart, Long coupleId, Long userId) {
        LocalDate weekEnd = weekStart.plusDays(6);
        List<MealPlanEntry> entries = mealPlanEntryRepository.findByDateBetweenAndCoupleId(weekStart, weekEnd, coupleId);

        Map<String, ShoppingListItem> aggregated = entries.stream()
                .flatMap(e -> e.getDishes().stream())
                .flatMap(dish -> {
                    if (dish.getRecipe() != null) {
                        return dish.getRecipe().getIngredients().stream();
                    } else {
                        com.example.backend.domain.Ingredient manualIngredient = new com.example.backend.domain.Ingredient();
                        manualIngredient.setName(dish.getManualName());
                        manualIngredient.setQuantity(dish.getManualQuantity() != null ? dish.getManualQuantity() : 0);
                        manualIngredient.setUnit(dish.getManualUnit());
                        return java.util.stream.Stream.of(manualIngredient);
                    }
                })
                .collect(Collectors.groupingBy(
                        i -> i.getName().toLowerCase() + "|" + i.getUnit().name(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    ShoppingListItem item = new ShoppingListItem();
                                    item.setWeekStartDate(weekStart);
                                    item.setIngredientName(list.get(0).getName());
                                    item.setTotalQuantity(list.stream().mapToDouble(i -> i.getQuantity()).sum());
                                    item.setUnit(list.get(0).getUnit());
                                    item.setChecked(false);
                                    item.setManual(false);
                                    return item;
                                }
                        )
                ));

        List<ShoppingListItem> existing = shoppingListItemRepository.findByWeekStartDateAndCoupleId(weekStart, coupleId);
        List<ShoppingListItem> toDelete = existing.stream()
                .filter(i -> !i.isManual())
                .collect(Collectors.toList());
        shoppingListItemRepository.deleteAll(toDelete);

        Couple couple = coupleRepository.findById(coupleId)
            .orElseThrow(() -> new ResourceNotFoundException("Couple not found"));

        List<ShoppingListItem> savedItems = new java.util.ArrayList<>();
        for (ShoppingListItem item : aggregated.values()) {
            item.setCouple(couple);
            savedItems.add(shoppingListItemRepository.save(item));
        }

        List<ShoppingListItem> manualItems = existing.stream()
                .filter(ShoppingListItem::isManual)
                .collect(Collectors.toList());

        manualItems.addAll(savedItems);

        eventPublisher.publishEvent(new ShoppingListRegenerateEvent(coupleId, userId));

        return manualItems.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ShoppingListItemDto addManualItem(CreateManualItemRequest request, LocalDate weekStart, Long coupleId) {
        Couple couple = coupleRepository.findById(coupleId)
            .orElseThrow(() -> new ResourceNotFoundException("Couple not found"));

        ShoppingListItem item = ShoppingListItem.builder()
                .weekStartDate(weekStart)
                .ingredientName(request.getIngredientName())
                .totalQuantity(request.getTotalQuantity())
                .unit(request.getUnit())
                .checked(false)
                .manual(true)
                .couple(couple)
                .build();

        ShoppingListItem saved = shoppingListItemRepository.save(item);
        return mapToDto(saved);
    }

    @Transactional
    public ShoppingListItemDto updateItem(Long id, ShoppingListItemDto dto, Long coupleId) {
        ShoppingListItem item = shoppingListItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));

        if (!item.getCouple().getId().equals(coupleId)) {
            throw new AccessDeniedException("Access denied");
        }

        item.setChecked(dto.isChecked());
        ShoppingListItem saved = shoppingListItemRepository.save(item);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteItem(Long id, Long coupleId) {
        ShoppingListItem item = shoppingListItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));

        if (!item.getCouple().getId().equals(coupleId)) {
            throw new AccessDeniedException("Access denied");
        }

        shoppingListItemRepository.deleteById(id);
    }

    private ShoppingListItemDto mapToDto(ShoppingListItem item) {
        ShoppingListItemDto dto = new ShoppingListItemDto();
        dto.setId(item.getId());
        dto.setWeekStartDate(item.getWeekStartDate());
        dto.setIngredientName(item.getIngredientName());
        dto.setTotalQuantity(item.getTotalQuantity());
        dto.setUnit(item.getUnit());
        dto.setChecked(item.isChecked());
        dto.setManual(item.isManual());
        return dto;
    }
}
