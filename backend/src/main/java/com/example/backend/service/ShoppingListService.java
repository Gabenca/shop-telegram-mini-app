package com.example.backend.service;

import com.example.backend.domain.MealPlanEntry;
import com.example.backend.domain.Recipe;
import com.example.backend.domain.ShoppingListItem;
import com.example.backend.domain.Unit;
import com.example.backend.dto.CreateManualItemRequest;
import com.example.backend.dto.ShoppingListItemDto;
import com.example.backend.repository.MealPlanEntryRepository;
import com.example.backend.repository.ShoppingListItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final ShoppingListItemRepository shoppingListItemRepository;
    private final MealPlanEntryRepository mealPlanEntryRepository;

    @Transactional(readOnly = true)
    public List<ShoppingListItemDto> getShoppingListForWeek(LocalDate weekStart) {
        return shoppingListItemRepository.findByWeekStartDate(weekStart).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ShoppingListItemDto> regenerateShoppingList(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        List<MealPlanEntry> entries = mealPlanEntryRepository.findByDateBetween(weekStart, weekEnd);

        Map<String, ShoppingListItem> aggregated = entries.stream()
                .flatMap(e -> e.getRecipe().getIngredients().stream())
                .collect(Collectors.groupingBy(
                        i -> i.getName().toLowerCase() + "|" + i.getUnit().name(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    ShoppingListItem item = new ShoppingListItem();
                                    item.setWeekStartDate(weekStart);
                                    item.setIngredientName(list.get(0).getName());
                                    item.setTotalQuantity(list.stream().mapToDouble(i -> i.getWeightInGrams()).sum());
                                    item.setUnit(list.get(0).getUnit());
                                    item.setChecked(false);
                                    item.setManual(false);
                                    return item;
                                }
                        )
                ));

        List<ShoppingListItem> existing = shoppingListItemRepository.findByWeekStartDate(weekStart);
        List<ShoppingListItem> toDelete = existing.stream()
                .filter(i -> !i.isManual())
                .collect(Collectors.toList());
        shoppingListItemRepository.deleteAll(toDelete);

        List<ShoppingListItem> saved = shoppingListItemRepository.saveAll(aggregated.values());

        List<ShoppingListItem> manualItems = existing.stream()
                .filter(ShoppingListItem::isManual)
                .collect(Collectors.toList());

        manualItems.addAll(saved);
        return manualItems.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ShoppingListItemDto addManualItem(CreateManualItemRequest request) {
        ShoppingListItem item = ShoppingListItem.builder()
                .weekStartDate(request.getWeekStartDate())
                .ingredientName(request.getIngredientName())
                .totalQuantity(request.getTotalQuantity())
                .unit(request.getUnit())
                .checked(false)
                .manual(true)
                .build();

        ShoppingListItem saved = shoppingListItemRepository.save(item);
        return mapToDto(saved);
    }

    @Transactional
    public ShoppingListItemDto updateItem(Long id, ShoppingListItemDto dto) {
        ShoppingListItem item = shoppingListItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found: " + id));
        item.setChecked(dto.isChecked());
        ShoppingListItem saved = shoppingListItemRepository.save(item);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteItem(Long id) {
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
