package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity  // tells JPA "create a database table for this class"
public class Recipe_staticTwo {

    @Id  // this is the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // auto-increment ID
    private Long id;

    private String name;
    private int calories;
    private int protein;

    // Empty constructor — required by JPA
    public Recipe_staticTwo() {}

    public Recipe_staticTwo(String name, int calories, int protein) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public int getCalories() { return calories; }
    public int getProtein() { return protein; }
}
