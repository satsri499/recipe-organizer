package com.example.demo.repository;

import com.example.demo.entity.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    // Get all meals planned for a specific day
    List<MealPlan> findByUserIdAndPlanDate(Long userId, LocalDate planDate);

    // Get all meals for a date range (weekly view)
    List<MealPlan> findByUserIdAndPlanDateBetween(Long userId, LocalDate start, LocalDate end);

    // Get meals for a specific slot on a day
    List<MealPlan> findByUserIdAndPlanDateAndMealSlot(Long userId, LocalDate planDate, String mealSlot);
}
