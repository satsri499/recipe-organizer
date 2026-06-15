package com.example.demo.service;

import com.example.demo.entity.MealPlan;
import com.example.demo.entity.Recipe;
import com.example.demo.entity.User;
import com.example.demo.repository.MealPlanRepository;
import com.example.demo.repository.NutritionRepository;
import com.example.demo.repository.RecipeRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final NutritionRepository nutritionRepository;

    public MealPlanService(MealPlanRepository mealPlanRepository,
                           RecipeRepository recipeRepository,
                           UserRepository userRepository,
                           NutritionRepository nutritionRepository) {
        this.mealPlanRepository = mealPlanRepository;
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.nutritionRepository = nutritionRepository;
    }

    // Get all meals for a specific day
    public List<MealPlan> getDayPlan(Long userId, LocalDate date) {
        return mealPlanRepository.findByUserIdAndPlanDate(userId, date);
    }

    // Get weekly plan
    public List<MealPlan> getWeekPlan(Long userId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return mealPlanRepository.findByUserIdAndPlanDateBetween(userId, weekStart, weekEnd);
    }

    // Add a recipe to a meal slot
    public MealPlan addToMealPlan(Long userId, Long recipeId,
                                  LocalDate date, String mealSlot, Integer servings) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        MealPlan plan = new MealPlan();
        plan.setUser(user);
        plan.setRecipe(recipe);
        plan.setPlanDate(date);
        plan.setMealSlot(mealSlot);
        plan.setServings(servings != null ? servings : 1);
        return mealPlanRepository.save(plan);
    }

    // Remove a meal from the plan
    public void removeFromMealPlan(Long mealPlanId) {
        mealPlanRepository.deleteById(mealPlanId);
    }

    // Calculate total calories for a day
    public int getDailyCalories(Long userId, LocalDate date) {
        List<MealPlan> plans = getDayPlan(userId, date);
        return plans.stream()
                .mapToInt(plan -> {
                    return nutritionRepository
                            .findByRecipeId(plan.getRecipe().getId())
                            .map(n -> n.getCalories() * plan.getServings())
                            .orElse(0);
                })
                .sum();
    }

    // Calculate total protein for a day
    public int getDailyProtein(Long userId, LocalDate date) {
        List<MealPlan> plans = getDayPlan(userId, date);
        return plans.stream()
                .mapToInt(plan -> {
                    return nutritionRepository
                            .findByRecipeId(plan.getRecipe().getId())
                            .map(n -> n.getProteinG() * plan.getServings())
                            .orElse(0);
                })
                .sum();
    }
}