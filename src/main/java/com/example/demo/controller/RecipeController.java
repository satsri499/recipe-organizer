package com.example.demo.controller;

import com.example.demo.dto.RecipeRequest;
import com.example.demo.dto.ExtractedRecipeData;
import com.example.demo.dto.ExtractionRequest;
import com.example.demo.dto.ExtractionResponse;
import com.example.demo.entity.Recipe;
import com.example.demo.service.ImageStorageService;
import com.example.demo.service.RecipeService;
import com.example.demo.service.RecipeExtractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;


import java.util.List;


@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final RecipeExtractionService extractionService;
    private final ImageStorageService imageStorageService;


    public RecipeController(RecipeService recipeService,
                            RecipeExtractionService extractionService,
                            ImageStorageService  imageStorageService ) {
        this.recipeService = recipeService;
        this.extractionService=extractionService;
        this.imageStorageService=imageStorageService;
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
    public ResponseEntity<?> updateRecipe(
            @PathVariable Long id,
            @RequestBody RecipeRequest request) {
        try {
            Recipe updated = recipeService.updateFullRecipe(id, request);
            return ResponseEntity.ok(updated);
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
    // POST /api/recipes/extract/url?userId=3
// Only extracts, does NOT save
    @PostMapping("/extract/url")
    public ResponseEntity<?> extractFromUrl(
            @RequestBody ExtractionRequest request,
            @RequestParam Long userId) {
        try {
            System.out.println("=== EXTRACT FROM URL (no save) ===");

            ExtractedRecipeData data;
            switch (request.getSourceType().toUpperCase()) {
                case "WEBSITE":
                case "URL":
                    data = extractionService.extractFromWebsite(request.getSourceUrl());
                    break;
                case "YOUTUBE":
                    data = extractionService.extractFromYoutube(request.getSourceUrl());
                    break;
                default:
                    return ResponseEntity.badRequest().body("Unsupported source type");
            }

            boolean isDuplicate = recipeService.isDuplicateRecipe(userId, data.getName());
            String message = isDuplicate
                    ? "You already have a recipe named '" + data.getName() + "' in your book"
                    : null;

            ExtractionResponse response = new ExtractionResponse(data, isDuplicate, message);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong: " + e.getMessage());
        }
    }

    // POST /api/recipes/extract/image?userId=3
// Only extracts, does NOT save
    @PostMapping("/extract/image")
    public ResponseEntity<?> extractFromImage(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam Long userId) {
        try {
            System.out.println("=== EXTRACT FROM IMAGE (no save) ===");

            if (files.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select at least one image");
            }

            List<ExtractedRecipeData> extractedRecipes = extractionService.extractFromImages(files);

            List<ExtractionResponse> responses = new ArrayList<>();
            for (ExtractedRecipeData data : extractedRecipes) {
                boolean isDuplicate = recipeService.isDuplicateRecipe(userId, data.getName());
                String message = isDuplicate
                        ? "You already have a recipe named '" + data.getName() + "' in your book"
                        : null;
                responses.add(new ExtractionResponse(data, isDuplicate, message));
            }

            if (responses.size() == 1) {
                return ResponseEntity.ok(responses.get(0));
            } else {
                return ResponseEntity.ok(responses);
            }

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong: " + e.getMessage());
        }
    }

    // GET /api/recipes/{id}/details
    @GetMapping("/{id}/details")
    public ResponseEntity<Recipe> getRecipeDetails(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(recipeService.getRecipeWithDetails(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


}
