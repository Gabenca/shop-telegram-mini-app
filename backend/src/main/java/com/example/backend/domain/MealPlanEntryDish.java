package com.example.backend.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "meal_plan_entry_dishes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealPlanEntryDish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_entry_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MealPlanEntry mealPlanEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Recipe recipe;

    private String manualName;

    private Double manualQuantity;

    @Enumerated(EnumType.STRING)
    private Unit manualUnit;

    private Integer sortOrder;
}