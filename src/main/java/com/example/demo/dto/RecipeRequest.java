package com.example.demo.dto;

import java.util.List;

public class RecipeRequest {

    private String name;
    private String description;
    private String cuisine;
    private String category;
    private Integer servings;
    private String sourceType;
    private String sourceUrl;
    private String imagePath;
    private Boolean isFavourite = false;
    private Integer rating;
    private String notes;

    private NutritionRequest nutrition;
    private List<IngredientRequest> ingredients;
    private List<CookingStepRequest> cookingSteps;
    private List<TagRequest> tags;

    private String imageBase64;     // new — Base64 image data
    private String imageMediaType;  // new — e.g. "image/jpeg"


    // Getters and Setters
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
    public NutritionRequest getNutrition() { return nutrition; }
    public void setNutrition(NutritionRequest nutrition) { this.nutrition = nutrition; }
    public List<IngredientRequest> getIngredients() { return ingredients; }
    public void setIngredients(List<IngredientRequest> ingredients) { this.ingredients = ingredients; }
    public List<CookingStepRequest> getCookingSteps() { return cookingSteps; }
    public void setCookingSteps(List<CookingStepRequest> cookingSteps) { this.cookingSteps = cookingSteps; }
    public List<TagRequest> getTags() { return tags; }
    public void setTags(List<TagRequest> tags) { this.tags = tags; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public String getImageMediaType() { return imageMediaType; }
    public void setImageMediaType(String imageMediaType) { this.imageMediaType = imageMediaType; }

    // Nested DTOs
    public static class NutritionRequest {
        private Integer calories;
        private Integer proteinG;
        private Integer carbsG;
        private Integer fatG;
        private Integer fiberG;
        private Integer sugarG;

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

    public static class IngredientRequest {
        private String name;
        private String quantity;
        private String unit;
        private Integer orderNum;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public Integer getOrderNum() { return orderNum; }
        public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    }

    public static class CookingStepRequest {
        private Integer stepNumber;
        private String instruction;
        private Integer durationMins;

        public Integer getStepNumber() { return stepNumber; }
        public void setStepNumber(Integer stepNumber) { this.stepNumber = stepNumber; }
        public String getInstruction() { return instruction; }
        public void setInstruction(String instruction) { this.instruction = instruction; }
        public Integer getDurationMins() { return durationMins; }
        public void setDurationMins(Integer durationMins) { this.durationMins = durationMins; }
    }

    public static class TagRequest {
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}