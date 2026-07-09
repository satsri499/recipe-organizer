package com.example.demo.dto;

import com.example.demo.entity.MealPlan;

public class MealPlanResponse {

    private Long id;
    private String mealSlot;
    private String planDate;
    private Integer servings;
    private RecipeInfo recipe;

    // Nested class for recipe info
    public static class RecipeInfo {
        private Long id;
        private String name;
        private String category;
        private NutritionInfo nutrition;

        public static class NutritionInfo {
            private Integer calories;
            private Integer proteinG;
            private Integer carbsG;
            private Integer fatG;

            public NutritionInfo(Integer calories, Integer proteinG, Integer carbsG, Integer fatG) {
                this.calories = calories;
                this.proteinG = proteinG;
                this.carbsG = carbsG;
                this.fatG = fatG;
            }

            public Integer getCalories() { return calories; }
            public Integer getProteinG() { return proteinG; }
            public Integer getCarbsG() { return carbsG; }
            public Integer getFatG() { return fatG; }
        }

        public RecipeInfo(Long id, String name, String category, NutritionInfo nutrition) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.nutrition = nutrition;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public NutritionInfo getNutrition() { return nutrition; }
    }

    // Build from MealPlan entity
    public static MealPlanResponse from(MealPlan mealPlan) {
        MealPlanResponse response = new MealPlanResponse();
        response.id = mealPlan.getId();
        response.mealSlot = mealPlan.getMealSlot();
        response.planDate = mealPlan.getPlanDate().toString();
        response.servings = mealPlan.getServings();

        if (mealPlan.getRecipe() != null) {
            RecipeInfo.NutritionInfo nutrition = null;
            if (mealPlan.getRecipe().getNutrition() != null) {
                nutrition = new RecipeInfo.NutritionInfo(
                        mealPlan.getRecipe().getNutrition().getCalories(),
                        mealPlan.getRecipe().getNutrition().getProteinG(),
                        mealPlan.getRecipe().getNutrition().getCarbsG(),
                        mealPlan.getRecipe().getNutrition().getFatG()
                );
            }
            response.recipe = new RecipeInfo(
                    mealPlan.getRecipe().getId(),
                    mealPlan.getRecipe().getName(),
                    mealPlan.getRecipe().getCategory(),
                    nutrition
            );
        }

        return response;
    }

    public Long getId() { return id; }
    public String getMealSlot() { return mealSlot; }
    public String getPlanDate() { return planDate; }
    public Integer getServings() { return servings; }
    public RecipeInfo getRecipe() { return recipe; }
}