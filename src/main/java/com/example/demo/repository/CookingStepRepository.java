package com.example.demo.repository;

import com.example.demo.entity.CookingStep;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CookingStepRepository extends JpaRepository<CookingStep, Long> {

    // All steps for a recipe in order
    List<CookingStep> findByRecipeIdOrderByStepNumberAsc(Long recipeId);
}