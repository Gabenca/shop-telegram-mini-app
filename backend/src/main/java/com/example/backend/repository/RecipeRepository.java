package com.example.backend.repository;

import com.example.backend.domain.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByCoupleId(Long coupleId);

    @Query("SELECT r FROM Recipe r WHERE r.couple.id = :coupleId AND LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Recipe> searchByNameAndCoupleId(@Param("query") String query, @Param("coupleId") Long coupleId);
}
