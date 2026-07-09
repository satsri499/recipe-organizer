package com.example.demo.dto;

public class ExtractionResponse {

    private ExtractedRecipeData recipe;
    private boolean isDuplicate;
    private String duplicateMessage;

    public ExtractionResponse(ExtractedRecipeData recipe, boolean isDuplicate, String duplicateMessage) {
        this.recipe = recipe;
        this.isDuplicate = isDuplicate;
        this.duplicateMessage = duplicateMessage;
    }

    public ExtractedRecipeData getRecipe() { return recipe; }
    public boolean isDuplicate() { return isDuplicate; }
    public String getDuplicateMessage() { return duplicateMessage; }
}