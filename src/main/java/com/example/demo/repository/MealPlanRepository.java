package com.example.demo.repository;

import com.example.demo.entity.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    // Get all meals planned for a specific day
    @Query("SELECT mp FROM MealPlan mp " +
            "LEFT JOIN FETCH mp.recipe r " +
            "LEFT JOIN FETCH r.nutrition " +
            "WHERE mp.user.id = :userId AND mp.planDate = :date")
    List<MealPlan> findByUserIdAndPlanDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date);
    // Get all meals for a date range (weekly view)
    List<MealPlan> findByUserIdAndPlanDateBetween(Long userId, LocalDate start, LocalDate end);

    // Get meals for a specific slot on a day
    List<MealPlan> findByUserIdAndPlanDateAndMealSlot(Long userId, LocalDate planDate, String mealSlot);
}
