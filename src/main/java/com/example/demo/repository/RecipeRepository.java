package com.example.demo.repository;

import com.example.demo.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
    // Search by name OR ingredient name
    @Query("SELECT DISTINCT r FROM Recipe r " +
            "LEFT JOIN r.ingredients i " +
            "WHERE r.user.id = :userId AND " +
            "(LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Recipe> searchByNameOrIngredient(@Param("userId") Long userId, @Param("query") String query);

    // Check if a recipe with this name already exists for the user

}
