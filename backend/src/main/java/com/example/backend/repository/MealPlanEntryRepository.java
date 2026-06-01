package com.example.backend.repository;

import com.example.backend.domain.MealPlanEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealPlanEntryRepository extends JpaRepository<MealPlanEntry, Long> {
    List<MealPlanEntry> findByDateBetween(LocalDate start, LocalDate end);
    List<MealPlanEntry> findByDateBetweenAndCoupleId(LocalDate start, LocalDate end, Long coupleId);
}
