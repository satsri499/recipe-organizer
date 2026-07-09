package com.example.demo.service;

import com.example.demo.dto.RecipeRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.dto.ExtractedRecipeData;
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
    private final ImageStorageService imageStorageService;


    public RecipeService(RecipeRepository recipeRepository,
                         UserRepository userRepository,
                         NutritionRepository nutritionRepository,
                         IngredientRepository ingredientRepository,
                         CookingStepRepository cookingStepRepository,
                         TagRepository tagRepository,
                         ImageStorageService imageStorageService) {
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
        this.nutritionRepository = nutritionRepository;
        this.ingredientRepository = ingredientRepository;
        this.cookingStepRepository = cookingStepRepository;
        this.tagRepository = tagRepository;
        this.imageStorageService = imageStorageService;
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
        return recipeRepository.searchByNameOrIngredient(userId, name);
        //return recipeRepository.findByUserIdAndNameContainingIgnoreCase(userId, name);
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

   /* @Transactional  // if anything fails, everything rolls back
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
    }*/

    @Transactional
    public Recipe saveFullRecipe(RecipeRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check for duplicate recipe name
        if (recipeRepository.existsByUserIdAndNameIgnoreCase(userId, request.getName())) {
            throw new RuntimeException("DUPLICATE: A recipe named '" + request.getName() + "' already exists in your book");
        }

        // Save the image NOW (only happens when user confirms save)
        String imagePath = null;
        if (request.getImageBase64() != null && !request.getImageBase64().isEmpty()) {
            try {
                imagePath = imageStorageService.saveImageFromBase64(
                        request.getImageBase64(), request.getImageMediaType());
            } catch (Exception e) {
                System.out.println("Failed to save image: " + e.getMessage());
            }
        }

        Recipe recipe = new Recipe();
        recipe.setUser(user);
        recipe.setName(request.getName());
        recipe.setDescription(request.getDescription());
        recipe.setCuisine(request.getCuisine());
        recipe.setCategory(request.getCategory());
        recipe.setServings(request.getServings());
        recipe.setSourceType(request.getSourceType());
        recipe.setSourceUrl(request.getSourceUrl());
        recipe.setImagePath(imagePath);  // ← set the freshly saved path
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

    @Transactional
    public Recipe extractAndSave(ExtractedRecipeData data, Long userId,
                                 String sourceType, String sourceUrl,
                                 String imagePath) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Recipe recipe = new Recipe();
        recipe.setUser(user);
        recipe.setName(data.getName());
        recipe.setDescription(data.getDescription());
        recipe.setCuisine(data.getCuisine());
        recipe.setCategory(data.getCategory());
        recipe.setServings(data.getServings());
        recipe.setSourceType(sourceType);
        recipe.setSourceUrl(sourceUrl);
        recipe.setImagePath(imagePath);  // ← save image path
        recipe.setIsFavourite(false);
        Recipe savedRecipe = recipeRepository.save(recipe);

        // Save nutrition
        Nutrition nutrition = new Nutrition();
        nutrition.setRecipe(savedRecipe);
        nutrition.setCalories(data.getCalories());
        nutrition.setProteinG(data.getProteinG());
        nutrition.setCarbsG(data.getCarbsG());
        nutrition.setFatG(data.getFatG());
        nutrition.setFiberG(data.getFiberG());
        nutrition.setSugarG(data.getSugarG());
        nutritionRepository.save(nutrition);

        // Save ingredients
        if (data.getIngredients() != null) {
            for (ExtractedRecipeData.IngredientData ing : data.getIngredients()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setRecipe(savedRecipe);
                ingredient.setName(ing.getName());
                ingredient.setQuantity(ing.getQuantity());
                ingredient.setUnit(ing.getUnit());
                ingredient.setOrderNum(ing.getOrderNum());
                ingredientRepository.save(ingredient);
            }
        }

        // Save cooking steps
        if (data.getSteps() != null) {
            for (ExtractedRecipeData.StepData step : data.getSteps()) {
                CookingStep cookingStep = new CookingStep();
                cookingStep.setRecipe(savedRecipe);
                cookingStep.setStepNumber(step.getStepNumber());
                cookingStep.setInstruction(step.getInstruction());
                cookingStep.setDurationMins(step.getDurationMins());
                cookingStepRepository.save(cookingStep);
            }
        }

        // Save tags
        if (data.getTags() != null) {
            for (String tagName : data.getTags()) {
                Tag tag = new Tag();
                tag.setRecipe(savedRecipe);
                tag.setName(tagName);
                tagRepository.save(tag);
            }
        }

        return savedRecipe;
    }
    public Recipe getRecipeWithDetails(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        // Force load related data
        recipe.getIngredients().size();
        recipe.getCookingSteps().size();
        recipe.getTags().size();
        if (recipe.getNutrition() != null) {
            recipe.getNutrition().getCalories();
        }
        return recipe;
    }

    // Check for duplicate recipe name before saving
    public boolean isDuplicateRecipe(Long userId, String name) {
        return recipeRepository.existsByUserIdAndNameIgnoreCase(userId, name);
    }
    @Transactional
    public Recipe updateFullRecipe(Long recipeId, RecipeRequest request) {
        Recipe existing = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        // Update basic fields
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setCuisine(request.getCuisine());
        existing.setCategory(request.getCategory());
        existing.setServings(request.getServings());
        existing.setNotes(request.getNotes());
        existing.setRating(request.getRating());
        Recipe savedRecipe = recipeRepository.save(existing);

        // ---- Update Nutrition ----
        if (request.getNutrition() != null) {
            Nutrition nutrition = nutritionRepository.findByRecipeId(recipeId)
                    .orElse(new Nutrition());
            nutrition.setRecipe(savedRecipe);
            nutrition.setCalories(request.getNutrition().getCalories());
            nutrition.setProteinG(request.getNutrition().getProteinG());
            nutrition.setCarbsG(request.getNutrition().getCarbsG());
            nutrition.setFatG(request.getNutrition().getFatG());
            nutrition.setFiberG(request.getNutrition().getFiberG());
            nutrition.setSugarG(request.getNutrition().getSugarG());
            nutritionRepository.save(nutrition);
        }

        // ---- Replace Ingredients (delete old, insert new) ----
        if (request.getIngredients() != null) {
            List<Ingredient> oldIngredients = ingredientRepository.findByRecipeIdOrderByOrderNumAsc(recipeId);
            ingredientRepository.deleteAll(oldIngredients);

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

        // ---- Replace Cooking Steps (delete old, insert new) ----
        if (request.getCookingSteps() != null) {
            List<CookingStep> oldSteps = cookingStepRepository.findByRecipeIdOrderByStepNumberAsc(recipeId);
            cookingStepRepository.deleteAll(oldSteps);

            for (RecipeRequest.CookingStepRequest step : request.getCookingSteps()) {
                CookingStep cookingStep = new CookingStep();
                cookingStep.setRecipe(savedRecipe);
                cookingStep.setStepNumber(step.getStepNumber());
                cookingStep.setInstruction(step.getInstruction());
                cookingStep.setDurationMins(step.getDurationMins());
                cookingStepRepository.save(cookingStep);
            }
        }

        // ---- Replace Tags (delete old, insert new) ----
        if (request.getTags() != null) {
            List<Tag> oldTags = tagRepository.findByRecipeId(recipeId);
            tagRepository.deleteAll(oldTags);

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