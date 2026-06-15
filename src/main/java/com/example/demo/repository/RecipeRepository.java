package com.example.demo.repository;

import com.example.demo.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    // All recipes for a specific user
    List<Recipe> findByUserId(Long userId);

    // Search by name (case insensitive)
    List<Recipe> findByUserIdAndNameContainingIgnoreCase(Long userId, String name);

    // Filter by category (BREAKFAST, LUNCH etc.)
    List<Recipe> findByUserIdAndCategory(Long userId, String category);

    // Get only favourites
    List<Recipe> findByUserIdAndIsFavouriteTrue(Long userId);

}
