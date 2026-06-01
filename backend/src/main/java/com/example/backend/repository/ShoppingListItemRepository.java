package com.example.backend.repository;

import com.example.backend.domain.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {
    List<ShoppingListItem> findByWeekStartDate(LocalDate weekStartDate);
    List<ShoppingListItem> findByWeekStartDateAndCoupleId(LocalDate weekStartDate, Long coupleId);
}
