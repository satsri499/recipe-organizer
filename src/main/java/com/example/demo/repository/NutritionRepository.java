package com.example.demo.repository;

import com.example.demo.entity.Nutrition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NutritionRepository extends JpaRepository<Nutrition, Long> {

    // Get nutrition for a specific recipe
    Optional<Nutrition> findByRecipeId(Long recipeId);
}
