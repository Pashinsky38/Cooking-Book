package com.example.cookingbook;

import java.util.ArrayList;

public class Recipe {
    private String title;
    private String description;
    private String imageUri;
    private String category;
    private ArrayList<String> ingredients;
    private boolean isVegetarian;
    private boolean isVegan;
    private boolean isGlutenFree;

    public Recipe() {
        // Needed for Gson
        this.ingredients = new ArrayList<>();
    }

    public Recipe(String title, String description, String imageUri) {
        this.title = title;
        this.description = description;
        this.imageUri = imageUri;
        this.category = "Other";
        this.ingredients = new ArrayList<>();
        this.isVegetarian = false;
        this.isVegan = false;
        this.isGlutenFree = false;
    }

    public Recipe(String title, String description, String imageUri, String category) {
        this.title = title;
        this.description = description;
        this.imageUri = imageUri;
        this.category = category != null ? category : "Other";
        this.ingredients = new ArrayList<>();
        this.isVegetarian = false;
        this.isVegan = false;
        this.isGlutenFree = false;
    }

    public Recipe(String title, String description, String imageUri, String category,
                  ArrayList<String> ingredients, boolean isVegetarian, boolean isVegan, boolean isGlutenFree) {
        this.title = title;
        this.description = description;
        this.imageUri = imageUri;
        this.category = category != null ? category : "Other";
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        this.isVegetarian = isVegetarian;
        this.isVegan = isVegan;
        this.isGlutenFree = isGlutenFree;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUri() { return imageUri; }
    public String getCategory() { return category; }
    public ArrayList<String> getIngredients() { return ingredients; }
    public boolean isVegetarian() { return isVegetarian; }
    public boolean isVegan() { return isVegan; }
    public boolean isGlutenFree() { return isGlutenFree; }

    public void setIngredients(ArrayList<String> ingredients) {
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
    }
    public void setVegetarian(boolean vegetarian) { isVegetarian = vegetarian; }
    public void setVegan(boolean vegan) { isVegan = vegan; }
    public void setGlutenFree(boolean glutenFree) { isGlutenFree = glutenFree; }
}