package com.example.demo.dto;

import java.util.List;

public class ExtractedRecipeData {

    private String name;
    private String description;
    private String cuisine;
    private String category;
    private Integer servings;

    // Nutrition
    private Integer calories;
    private Integer proteinG;
    private Integer carbsG;
    private Integer fatG;
    private Integer fiberG;
    private Integer sugarG;

    // Lists
    private List<IngredientData> ingredients;
    private List<StepData> steps;
    private List<String> tags;

    // -------------------------------------------------------
    // Nested classes
    // -------------------------------------------------------
    public static class IngredientData {
        private String name;
        private String quantity;
        private String unit;
        private Integer orderNum;

        public IngredientData(String name, String quantity, String unit, Integer orderNum) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
            this.orderNum = orderNum;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public Integer getOrderNum() { return orderNum; }
        public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    }

    public static class StepData {
        private Integer stepNumber;
        private String instruction;
        private Integer durationMins;

        public StepData(Integer stepNumber, String instruction, Integer durationMins) {
            this.stepNumber = stepNumber;
            this.instruction = instruction;
            this.durationMins = durationMins;
        }

        public Integer getStepNumber() { return stepNumber; }
        public void setStepNumber(Integer stepNumber) { this.stepNumber = stepNumber; }
        public String getInstruction() { return instruction; }
        public void setInstruction(String instruction) { this.instruction = instruction; }
        public Integer getDurationMins() { return durationMins; }
        public void setDurationMins(Integer durationMins) { this.durationMins = durationMins; }
    }

    // -------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------
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

    public List<IngredientData> getIngredients() { return ingredients; }
    public void setIngredients(List<IngredientData> ingredients) { this.ingredients = ingredients; }

    public List<StepData> getSteps() { return steps; }
    public void setSteps(List<StepData> steps) { this.steps = steps; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}