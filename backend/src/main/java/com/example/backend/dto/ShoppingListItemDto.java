package com.example.backend.dto;

import com.example.backend.domain.Unit;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ShoppingListItemDto {

    private Long id;
    private LocalDate weekStartDate;
    private String ingredientName;
    private Double totalQuantity;
    private Unit unit;
    private boolean checked;
    private boolean manual;
}
