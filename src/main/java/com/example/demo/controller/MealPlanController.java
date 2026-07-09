package com.example.demo.controller;

import com.example.demo.entity.MealPlan;
import com.example.demo.service.MealPlanService;
import com.example.demo.dto.MealPlanResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/mealplan")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    // GET /api/mealplan/day?userId=1&date=2026-06-09
    @GetMapping("/day")
    public ResponseEntity<List<MealPlanResponse>> getDayPlan(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<MealPlan> plans = mealPlanService.getDayPlan(userId, date);

        // Convert each MealPlan to MealPlanResponse DTO
        // This includes the recipe name and nutrition
        List<MealPlanResponse> responses = plans.stream()
                .map(MealPlanResponse::from)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // GET /api/mealplan/week?userId=1&weekStart=2026-06-09
    @GetMapping("/week")
    public ResponseEntity<List<MealPlan>> getWeekPlan(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return ResponseEntity.ok(mealPlanService.getWeekPlan(userId, weekStart));
    }

    // POST /api/mealplan
    @PostMapping
    public ResponseEntity<MealPlan> addToMealPlan(
            @RequestParam Long userId,
            @RequestParam Long recipeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String mealSlot,
            @RequestParam(required = false) Integer servings) {
        try {
            MealPlan plan = mealPlanService.addToMealPlan(
                    userId, recipeId, date, mealSlot, servings);
            return ResponseEntity.ok(plan);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // DELETE /api/mealplan/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFromMealPlan(@PathVariable Long id) {
        mealPlanService.removeFromMealPlan(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/mealplan/daily-summary?userId=1&date=2026-06-09
    @GetMapping("/daily-summary")
    public ResponseEntity<DailySummary> getDailySummary(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        int calories = mealPlanService.getDailyCalories(userId, date);
        int protein = mealPlanService.getDailyProtein(userId, date);
        return ResponseEntity.ok(new DailySummary(date, calories, protein));
    }

    // Simple record to return daily summary
    record DailySummary(LocalDate date, int totalCalories, int totalProtein) {}
}
