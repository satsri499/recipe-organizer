package com.example.demo.controller;

import com.example.demo.dto.RecipeRequest;
import com.example.demo.dto.ExtractedRecipeData;
import com.example.demo.dto.ExtractionRequest;
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

    // POST /api/recipes/extract/url?userId=3
    @PostMapping("/extract/url")
    public ResponseEntity<?> extractFromUrl(
            @RequestBody ExtractionRequest request,
            @RequestParam Long userId) {
        try {
            System.out.println("=== EXTRACT FROM URL ===");
            System.out.println("Source type: " + request.getSourceType());
            System.out.println("URL: " + request.getSourceUrl());

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
                    return ResponseEntity.badRequest()
                            .body("Unsupported source type: " + request.getSourceType());
            }

            Recipe saved = recipeService.extractAndSave(
                    data, userId, request.getSourceType(), request.getSourceUrl(), null); // ← null for imagePath

            return ResponseEntity.ok(saved);

        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(501)
                    .body("This feature is coming soon");
        } catch (RuntimeException e) {
            // This catches "No recipe found" from Claude
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Extraction error: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Something went wrong: " + e.getMessage());
        }
    }

    @PostMapping("/extract/image")
    public ResponseEntity<?> extractFromImage(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam Long userId) {
        try {
            System.out.println("=== EXTRACT FROM IMAGE ===");
            System.out.println("Number of files: " + files.size());

            // 1. Validate files
            if (files.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Please select at least one image");
            }

            // Validate file size — max 20MB per image
            for (MultipartFile file : files) {
                if (file.getSize() > 20 * 1024 * 1024) {
                    return ResponseEntity.badRequest()
                            .body("File '" + file.getOriginalFilename() +
                                    "' is too large. Maximum size is 20MB.");
                }
            }

            // 2. Save all images to disk
            List<String> imagePaths = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String imagePath = imageStorageService.saveImage(file);
                    imagePaths.add(imagePath);
                    System.out.println("Saved: " + imagePath);
                }
            }

            // 3. Extract recipes from all images together
            List<ExtractedRecipeData> extractedRecipes =
                    extractionService.extractFromImages(files);

            System.out.println("Recipes extracted: " + extractedRecipes.size());

            // 4. Save each recipe
            List<Recipe> savedRecipes = new ArrayList<>();
            for (int i = 0; i < extractedRecipes.size(); i++) {
                // Link first image to first recipe, second to second etc.
                // If more recipes than images — use first image for all
                String imagePath = imagePaths.isEmpty() ? null :
                        (i < imagePaths.size() ? imagePaths.get(i) : imagePaths.get(0));

                Recipe saved = recipeService.extractAndSave(
                        extractedRecipes.get(i), userId, "IMAGE", null, imagePath);
                savedRecipes.add(saved);
            }

            // 5. Return results
            if (savedRecipes.size() == 1) {
                return ResponseEntity.ok(savedRecipes.get(0));
            } else {
                return ResponseEntity.ok(savedRecipes);
            }

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Image extraction error: " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Something went wrong: " + e.getMessage());
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
