package com.example.demo.repository;

import com.example.demo.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    // All ingredients for a recipe
    List<Ingredient> findByRecipeIdOrderByOrderNumAsc(Long recipeId);

    // Search recipes by ingredient name — "what can I cook?"
    List<Ingredient> findByNameContainingIgnoreCase(String name);
}