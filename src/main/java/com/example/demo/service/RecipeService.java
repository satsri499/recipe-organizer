package com.example.demo.service;

import com.example.demo.dto.RecipeRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final NutritionRepository nutritionRepository;
    private final IngredientRepository ingredientRepository;
    private final CookingStepRepository cookingStepRepository;
    private final TagRepository tagRepository;

    public RecipeService(RecipeRepository recipeRepository,
                         UserRepository userRepository,
                         NutritionRepository nutritionRepository,
                         IngredientRepository ingredientRepository,
                         CookingStepRepository cookingStepRepository,
                         TagRepository tagRepository) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.nutritionRepository = nutritionRepository;
        this.ingredientRepository = ingredientRepository;
        this.cookingStepRepository = cookingStepRepository;
        this.tagRepository = tagRepository;
    }

    // Get all recipes for a user
    public List<Recipe> getAllRecipes(Long userId) {
        return recipeRepository.findByUserId(userId);
    }

    // Get single recipe by ID
    public Recipe getRecipeById(Long recipeId) {
        return recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
    }

    // Search recipes by name
    public List<Recipe> searchByName(Long userId, String name) {
        return recipeRepository.findByUserIdAndNameContainingIgnoreCase(userId, name);
    }

    // Filter by category
    public List<Recipe> getByCategory(Long userId, String category) {
        return recipeRepository.findByUserIdAndCategory(userId, category);
    }

    // Get favourites only
    public List<Recipe> getFavourites(Long userId) {
        return recipeRepository.findByUserIdAndIsFavouriteTrue(userId);
    }

    // Save a new recipe
    public Recipe saveRecipe(Recipe recipe, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        recipe.setUser(user);
        return recipeRepository.save(recipe);
    }

    // Update an existing recipe
    public Recipe updateRecipe(Long recipeId, Recipe updated) {
        Recipe existing = getRecipeById(recipeId);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setCuisine(updated.getCuisine());
        existing.setCategory(updated.getCategory());
        existing.setServings(updated.getServings());
        existing.setNotes(updated.getNotes());
        existing.setRating(updated.getRating());
        return recipeRepository.save(existing);
    }

    // Toggle favourite
    public Recipe toggleFavourite(Long recipeId) {
        Recipe recipe = getRecipeById(recipeId);
        recipe.setIsFavourite(!recipe.getIsFavourite());
        return recipeRepository.save(recipe);
    }

    // Delete a recipe
    public void deleteRecipe(Long recipeId) {
        recipeRepository.deleteById(recipeId);
    }

    @Transactional  // if anything fails, everything rolls back
    public Recipe saveFullRecipe(RecipeRequest request, Long userId) {
        // 1. Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Create and save the Recipe
        Recipe recipe = new Recipe();
        recipe.setUser(user);
        recipe.setName(request.getName());
        recipe.setDescription(request.getDescription());
        recipe.setCuisine(request.getCuisine());
        recipe.setCategory(request.getCategory());
        recipe.setServings(request.getServings());
        recipe.setSourceType(request.getSourceType());
        recipe.setSourceUrl(request.getSourceUrl());
        recipe.setImagePath(request.getImagePath());
        recipe.setIsFavourite(request.getIsFavourite() != null ? request.getIsFavourite() : false);
        recipe.setRating(request.getRating());
        recipe.setNotes(request.getNotes());
        Recipe savedRecipe = recipeRepository.save(recipe);

        // 3. Save Nutrition
        if (request.getNutrition() != null) {
            Nutrition nutrition = new Nutrition();
            nutrition.setRecipe(savedRecipe);
            nutrition.setCalories(request.getNutrition().getCalories());
            nutrition.setProteinG(request.getNutrition().getProteinG());
            nutrition.setCarbsG(request.getNutrition().getCarbsG());
            nutrition.setFatG(request.getNutrition().getFatG());
            nutrition.setFiberG(request.getNutrition().getFiberG());
            nutrition.setSugarG(request.getNutrition().getSugarG());
            nutritionRepository.save(nutrition);
        }

        // 4. Save Ingredients
        if (request.getIngredients() != null) {
            for (RecipeRequest.IngredientRequest ing : request.getIngredients()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setRecipe(savedRecipe);
                ingredient.setName(ing.getName());
                ingredient.setQuantity(ing.getQuantity());
                ingredient.setUnit(ing.getUnit());
                ingredient.setOrderNum(ing.getOrderNum());
                ingredientRepository.save(ingredient);
            }
        }

        // 5. Save Cooking Steps
        if (request.getCookingSteps() != null) {
            for (RecipeRequest.CookingStepRequest step : request.getCookingSteps()) {
                CookingStep cookingStep = new CookingStep();
                cookingStep.setRecipe(savedRecipe);
                cookingStep.setStepNumber(step.getStepNumber());
                cookingStep.setInstruction(step.getInstruction());
                cookingStep.setDurationMins(step.getDurationMins());
                cookingStepRepository.save(cookingStep);
            }
        }

        // 6. Save Tags
        if (request.getTags() != null) {
            for (RecipeRequest.TagRequest tagRequest : request.getTags()) {
                Tag tag = new Tag();
                tag.setRecipe(savedRecipe);
                tag.setName(tagRequest.getName());
                tagRepository.save(tag);
            }
        }

        return savedRecipe;
    }
}