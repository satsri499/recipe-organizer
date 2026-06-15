package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "nutrition")
public class Nutrition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    private Integer calories;
    private Integer proteinG;
    private Integer carbsG;
    private Integer fatG;
    private Integer fiberG;
    private Integer sugarG;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Recipe getRecipe() { return recipe; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }

    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }

    public Integer getProteinG() { return proteinG; }
    public void setProteinG(Integer proteinG) { this.proteinG = proteinG; }

    public Integer getCarbsG() { return carbsG; }
    public void setCarbsG(Integer carbsG) { this.carbsG = carbsG; }

    public Integer getFatG() { return fatG; }
    public void setFatG(Integer fatG) { this.fatG = fatG; }

    public Integer getFiberG() { return fiberG; }
    public void setFiberG(Integer fiberG) { this.fiberG = fiberG; }

    public Integer getSugarG() { return sugarG; }
    public void setSugarG(Integer sugarG) { this.sugarG = sugarG; }
}
