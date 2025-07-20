package com.example.cookingbook;

public class Recipe {
    private String title;
    private String description;
    private String imageUri;
    private String category; // Add this field

    public Recipe() {
        // Needed for Gson
    }

    public Recipe(String title, String description, String imageUri) {
        this.title = title;
        this.description = description;
        this.imageUri = imageUri;
        this.category = "Other"; // Default category
    }

    // Add new constructor with category
    public Recipe(String title, String description, String imageUri, String category) {
        this.title = title;
        this.description = description;
        this.imageUri = imageUri;
        this.category = category != null ? category : "Other";
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUri() { return imageUri; }
    public String getCategory() { return category; } // Add getter
}