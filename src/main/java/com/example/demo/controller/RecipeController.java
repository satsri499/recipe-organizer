package com.example.demo.controller;

import com.example.demo.dto.RecipeRequest;
import com.example.demo.entity.Recipe;
import com.example.demo.service.RecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    // GET /api/recipes?userId=1
    @GetMapping
    public ResponseEntity<List<Recipe>> getAllRecipes(@RequestParam Long userId) {
        return ResponseEntity.ok(recipeService.getAllRecipes(userId));
    }

    // GET /api/recipes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getRecipe(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(recipeService.getRecipeById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/recipes/search?userId=1&name=pasta
    @GetMapping("/search")
    public ResponseEntity<List<Recipe>> search(
            @RequestParam Long userId,
            @RequestParam String name) {
        return ResponseEntity.ok(recipeService.searchByName(userId, name));
    }

    // GET /api/recipes/category?userId=1&category=BREAKFAST
    @GetMapping("/category")
    public ResponseEntity<List<Recipe>> getByCategory(
            @RequestParam Long userId,
            @RequestParam String category) {
        return ResponseEntity.ok(recipeService.getByCategory(userId, category));
    }

    // GET /api/recipes/favourites?userId=1
    @GetMapping("/favourites")
    public ResponseEntity<List<Recipe>> getFavourites(@RequestParam Long userId) {
        return ResponseEntity.ok(recipeService.getFavourites(userId));
    }

    // POST /api/recipes?userId=1
   /* @PostMapping
    public ResponseEntity<Recipe> saveRecipe(
            @RequestBody Recipe recipe,
            @RequestParam Long userId) {
        try {
            return ResponseEntity.ok(recipeService.saveRecipe(recipe, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }*/

    // PUT /api/recipes/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Recipe> updateRecipe(
            @PathVariable Long id,
            @RequestBody Recipe recipe) {
        try {
            return ResponseEntity.ok(recipeService.updateRecipe(id, recipe));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT /api/recipes/{id}/favourite
    @PutMapping("/{id}/favourite")
    public ResponseEntity<Recipe> toggleFavourite(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(recipeService.toggleFavourite(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/recipes/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/recipes?userId=3
    @PostMapping
    public ResponseEntity<Recipe> saveRecipe(
            @RequestBody RecipeRequest request,
            @RequestParam Long userId) {
        System.out.println("=== SAVE RECIPE HIT ===");
        System.out.println("Recipe name: " + request.getName());
        System.out.println("UserId: " + userId);
        Recipe saved = recipeService.saveFullRecipe(request, userId);
        return ResponseEntity.ok(saved);
    }
    // TEST endpoint — no auth needed to debug
    @PostMapping("/test")
    public ResponseEntity<String> testPost() {
        System.out.println("=== TEST POST HIT ===");
        return ResponseEntity.ok("POST is working!");
    }
}
