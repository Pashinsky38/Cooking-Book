package com.example.cookingbook;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;

public class RecipeFormActivity extends AppCompatActivity {

    private static final int IMAGE_PICK_CODE = 101;
    private static final int PERMISSION_CODE = 102;

    private EditText titleInput, descInput, ingredientInput;
    private ImageView imageView;
    private Uri selectedImageUri;
    private int editingPosition = -1;
    private Spinner categorySpinner;
    private CheckBox vegetarianCheckbox, veganCheckbox, glutenFreeCheckbox, meatCheckbox;
    private LinearLayout ingredientsList;
    private ArrayList<String> ingredients;
    private View gradientBackground;

    // Default gradient colors
    private int currentStartColor = 0xFFFF6B6B;
    private int currentEndColor = 0xFFFFD93D;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_form);

        titleInput = findViewById(R.id.titleInput);
        descInput = findViewById(R.id.descInput);
        imageView = findViewById(R.id.recipeImageView);
        ingredientInput = findViewById(R.id.ingredientInput);
        ingredientsList = findViewById(R.id.ingredientsList);
        Button deleteBtn = findViewById(R.id.deleteBtn);
        Button chooseImg = findViewById(R.id.chooseImageBtn);
        Button saveBtn = findViewById(R.id.saveBtn);
        Button addIngredientBtn = findViewById(R.id.addIngredientBtn);
        categorySpinner = findViewById(R.id.categorySpinner);
        vegetarianCheckbox = findViewById(R.id.vegetarianCheckbox);
        veganCheckbox = findViewById(R.id.veganCheckbox);
        glutenFreeCheckbox = findViewById(R.id.glutenFreeCheckbox);
        meatCheckbox = findViewById(R.id.meatCheckbox);
        gradientBackground = findViewById(R.id.dynamicGradientBackground);

        ingredients = new ArrayList<>();

        String[] categories = {"Appetizers", "Main Course", "Desserts", "Beverages", "Salads", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Set initial gradient
        applyGradient(currentStartColor, currentEndColor, false);

        // Check if we're editing an existing recipe
        if (getIntent().hasExtra("position")) {
            editingPosition = getIntent().getIntExtra("position", -1);
            Recipe recipe = RecipeManager.recipes.get(editingPosition);
            titleInput.setText(recipe.getTitle());
            descInput.setText(recipe.getDescription());

            if (recipe.getImageUri() != null && !recipe.getImageUri().isEmpty()) {
                selectedImageUri = Uri.parse(recipe.getImageUri());
                imageView.setImageURI(selectedImageUri);

                // Extract colors from the image and apply gradient
                extractAndApplyColors(selectedImageUri);
            }

            // Set dietary filters
            vegetarianCheckbox.setChecked(recipe.isVegetarian());
            veganCheckbox.setChecked(recipe.isVegan());
            glutenFreeCheckbox.setChecked(recipe.isGlutenFree());
            meatCheckbox.setChecked(recipe.hasMeat());

            // Set ingredients
            if (recipe.getIngredients() != null) {
                ingredients.addAll(recipe.getIngredients());
                displayIngredients();
            }

            // Set the current category for editing
            String currentCategory = recipe.getCategory();
            if (currentCategory != null) {
                int categoryPosition = Arrays.asList(categories).indexOf(currentCategory);
                if (categoryPosition >= 0) {
                    categorySpinner.setSelection(categoryPosition);
                }
            }

            // Show delete button only when editing
            deleteBtn.setVisibility(View.VISIBLE);
        } else {
            // Hide delete button when adding new recipe
            deleteBtn.setVisibility(View.GONE);
        }

        addIngredientBtn.setOnClickListener(v -> {
            String ingredient = ingredientInput.getText().toString().trim();
            if (!ingredient.isEmpty()) {
                ingredients.add(ingredient);
                ingredientInput.setText("");
                displayIngredients();
            } else {
                Toast.makeText(this, "Please enter an ingredient", Toast.LENGTH_SHORT).show();
            }
        });

        chooseImg.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_CODE);
            } else {
                pickImageFromGallery();
            }
        });

        saveBtn.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String desc = descInput.getText().toString().trim();
            String img = selectedImageUri != null ? selectedImageUri.toString() : null;
            String category = categorySpinner.getSelectedItem().toString();

            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a recipe title", Toast.LENGTH_SHORT).show();
                return;
            }

            Recipe r = new Recipe(title, desc, img, category, ingredients,
                    vegetarianCheckbox.isChecked(),
                    veganCheckbox.isChecked(),
                    glutenFreeCheckbox.isChecked(),
                    meatCheckbox.isChecked());

            if (editingPosition == -1)
                RecipeManager.recipes.add(r);
            else
                RecipeManager.recipes.set(editingPosition, r);

            RecipeManager.saveRecipes(this);
            finish();
        });

        deleteBtn.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_confirmation_title))
                .setMessage(getString(R.string.delete_confirmation_message))
                .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                    RecipeManager.recipes.remove(editingPosition);
                    RecipeManager.saveRecipes(this);
                    finish();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show());
    }

    private void extractAndApplyColors(Uri imageUri) {
        ColorUtils.extractColorsFromImage(this, imageUri, (startColor, endColor) -> {
            // Apply the gradient with animation
            applyGradient(startColor, endColor, true);
        });
    }

    private void applyGradient(int startColor, int endColor, boolean animate) {
        GradientDrawable newGradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{startColor, endColor}
        );

        if (animate) {
            // Animate from current colors to new colors
            animateGradientChange(currentStartColor, currentEndColor, startColor, endColor);
        } else {
            gradientBackground.setBackground(newGradient);
        }

        currentStartColor = startColor;
        currentEndColor = endColor;
    }

    private void animateGradientChange(int fromStart, int fromEnd, int toStart, int toEnd) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(500);

        animator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            ArgbEvaluator evaluator = new ArgbEvaluator();

            int currentStart = (int) evaluator.evaluate(fraction, fromStart, toStart);
            int currentEnd = (int) evaluator.evaluate(fraction, fromEnd, toEnd);

            GradientDrawable gradient = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{currentStart, currentEnd}
            );

            gradientBackground.setBackground(gradient);
        });

        animator.start();
    }

    private void displayIngredients() {
        ingredientsList.removeAllViews();
        for (int i = 0; i < ingredients.size(); i++) {
            String ingredient = ingredients.get(i);
            LinearLayout ingredientRow = new LinearLayout(this);
            ingredientRow.setOrientation(LinearLayout.HORIZONTAL);
            ingredientRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            ingredientRow.setPadding(0, 8, 0, 8);

            TextView ingredientText = new TextView(this);
            ingredientText.setText("• " + ingredient);
            ingredientText.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            ingredientText.setTextSize(16);
            ingredientText.setTextColor(getResources().getColor(android.R.color.black));

            Button removeBtn = new Button(this);
            removeBtn.setText("Remove");
            removeBtn.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            final int index = i;
            removeBtn.setOnClickListener(v -> {
                ingredients.remove(index);
                displayIngredients();
            });

            ingredientRow.addView(ingredientText);
            ingredientRow.addView(removeBtn);
            ingredientsList.addView(ingredientRow);
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageView.setImageURI(selectedImageUri);

            // Extract colors and update gradient when new image is selected
            extractAndApplyColors(selectedImageUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery();
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }
}