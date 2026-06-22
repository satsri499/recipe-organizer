package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "recipe")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String cuisine;

    private String category;  // BREAKFAST, LUNCH, DINNER, SNACKS

    private Integer servings;

    private String sourceType;  // IMAGE, YOUTUBE, INSTAGRAM, MANUAL, URL

    private String sourceUrl;

    private String imagePath;

    private Boolean isFavourite = false;

    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    // One recipe has one nutrition
    @OneToOne(mappedBy = "recipe", cascade = CascadeType.ALL)
    private Nutrition nutrition;

    // One recipe has many ingredients
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    @OrderBy("orderNum ASC")  // always return ingredients in order
    private List<Ingredient> ingredients;

    // One recipe has many steps
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    @OrderBy("stepNumber ASC")  // always return steps in order
    private List<CookingStep> cookingSteps;

    // One recipe has many tags
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List<Tag> tags;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getServings() { return servings; }
    public void setServings(Integer servings) { this.servings = servings; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Boolean getIsFavourite() { return isFavourite; }
    public void setIsFavourite(Boolean isFavourite) { this.isFavourite = isFavourite; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Nutrition getNutrition() { return nutrition; }
    public void setNutrition(Nutrition nutrition) { this.nutrition = nutrition; }

    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) { this.ingredients = ingredients; }

    public List<CookingStep> getCookingSteps() { return cookingSteps; }
    public void setCookingSteps(List<CookingStep> cookingSteps) { this.cookingSteps = cookingSteps; }

    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }
}