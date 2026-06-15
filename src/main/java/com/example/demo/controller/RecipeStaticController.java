package com.example.demo.controller;

import com.example.demo.entity.Recipe_static;
import com.example.demo.repository.RecipeRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController  // tells Spring "this class handles HTTP requests"
public class RecipeStaticController {

    private final RecipeRepository recipeRepository;

    // Spring automatically injects the repository here
    public RecipeStaticController(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    // GET /recipes — fetch all saved recipes
    /*@GetMapping("/recipes")
    public List<Recipe_staticTwo> getRecipes() {
        return recipeRepository.findAll();
    }

    // POST /recipes — save a new recipe
    @PostMapping("/recipes")
    public Recipe_staticTwo saveRecipe(@RequestBody Recipe_staticTwo recipeStaticTwo) {
        return recipeRepository.save(recipeStaticTwo);
    }*/

    @GetMapping("/staticRecipe")  // when someone hits GET /recipes, run this method
    public List<String> getStaticRecipe() {
        return List.of("Pasta Carbonara", "Chicken Salad", "Oat Pancakes");
    }

    @GetMapping("/staticRecipes")
    public List<Recipe_static> getStaticRecipes() {
        return List.of(
                new Recipe_static("Pasta Carbonara", 650, 28),
                new Recipe_static("Chicken Salad", 400, 42),
                new Recipe_static("Oat Pancakes", 320, 12)
        );
    }

    @GetMapping("/errorPage")
    public List<Recipe_static> getError() {
        Recipe_static r = new Recipe_static("Pasta", 650, 28);

        int test = r.calories / 0;  // this will crash!

        return List.of(r);
    }
}
