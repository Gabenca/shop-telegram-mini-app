package com.example.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "meal_plan_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealPlanEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Recipe recipe;

    @Enumerated(EnumType.STRING)
    private MealType mealType;
}
