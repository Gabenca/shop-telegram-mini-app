package com.example.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "shopping_list_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate weekStartDate;

    private String ingredientName;

    private Double totalQuantity;

    @Enumerated(EnumType.STRING)
    private Unit unit;

    private boolean checked;

    private boolean manual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Couple couple;
}
