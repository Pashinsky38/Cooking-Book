package com.example.cookingbook;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class RecipeManager {
    public static ArrayList<Recipe> recipes = new ArrayList<>();
    private static final String PREFS_NAME = "cooking_book_prefs";
    private static final String RECIPES_KEY = "recipes";

    public static void loadRecipes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(RECIPES_KEY, null);

        if (json != null) {
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<Recipe>>() {}.getType();
                recipes = gson.fromJson(json, type);
            } catch (Exception e) {
                e.printStackTrace(); // Logs to Logcat
                recipes = new ArrayList<>(); // fallback
            }
        }
    }


    public static void saveRecipes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(recipes);
        editor.putString(RECIPES_KEY, json);
        editor.apply();
    }
}
