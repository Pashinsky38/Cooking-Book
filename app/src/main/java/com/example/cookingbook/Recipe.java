package com.example.cookingbook;

public class Recipe {
    private String title;
    private String description;
    private String imageUri;

    public Recipe() {
        // Needed for Gson
    }

    public Recipe(String title, String description, String imageUri) {
        this.title = title;
        this.description = description;
        this.imageUri = imageUri;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUri() { return imageUri; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
}

