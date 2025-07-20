package com.example.cookingbook;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private String title;
    private String description;
    private String imageUri;
    private String category;
    private List<String> tags;

    // Default categories
    public static final String[] CATEGORIES = {
            "All",
            "Breakfast",
            "Lunch",
            "Dinner",
            "Dessert",
            "Snack",
            "Appetizer",
            "Beverage",
            "Other"
    };

    public Recipe() {
        // Needed for Gson
        this.tags = new ArrayList<>();
        this.category = "Other";
    }

    public Recipe(String title, String description, String imageUri) {
        this.title = title;
        this.description = description;
        this.imageUri = imageUri;
        this.tags = new ArrayList<>();
        this.category = "Other";
    }

    public Recipe(String title, String description, String imageUri, String category, List<String> tags) {
        this.title = title;
        this.description = description;
        this.imageUri = imageUri;
        this.category = category != null ? category : "Other";
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUri() { return imageUri; }
    public String getCategory() { return category != null ? category : "Other"; }
    public List<String> getTags() { return tags != null ? tags : new ArrayList<>(); }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
    public void setCategory(String category) { this.category = category; }
    public void setTags(List<String> tags) { this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>(); }

    // Helper methods for tags
    public void addTag(String tag) {
        if (tags == null) tags = new ArrayList<>();
        if (!tags.contains(tag.trim()) && !tag.trim().isEmpty()) {
            tags.add(tag.trim());
        }
    }

    public void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }

    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }

    public String getTagsAsString() {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(", ", tags);
    }

    public void setTagsFromString(String tagsString) {
        tags = new ArrayList<>();
        if (tagsString != null && !tagsString.trim().isEmpty()) {
            String[] tagArray = tagsString.split(",");
            for (String tag : tagArray) {
                String trimmedTag = tag.trim();
                if (!trimmedTag.isEmpty()) {
                    tags.add(trimmedTag);
                }
            }
        }
    }
}