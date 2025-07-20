package com.example.cookingbook;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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

    private EditText titleInput, descInput, tagsInput;
    private Spinner categorySpinner;
    private ImageView imageView;
    private Uri selectedImageUri;
    private int editingPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_form);

        titleInput = findViewById(R.id.titleInput);
        descInput = findViewById(R.id.descInput);
        tagsInput = findViewById(R.id.tagsInput);
        categorySpinner = findViewById(R.id.categorySpinner);
        imageView = findViewById(R.id.recipeImageView);
        Button deleteBtn = findViewById(R.id.deleteBtn);
        Button chooseImg = findViewById(R.id.chooseImageBtn);
        Button saveBtn = findViewById(R.id.saveBtn);

        setupCategorySpinner();

        if (getIntent().hasExtra("position")) {
            editingPosition = getIntent().getIntExtra("position", -1);
            Recipe recipe = RecipeManager.recipes.get(editingPosition);
            titleInput.setText(recipe.getTitle());
            descInput.setText(recipe.getDescription());
            tagsInput.setText(recipe.getTagsAsString());

            // Set category in spinner
            String[] categories = Recipe.CATEGORIES;
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(recipe.getCategory())) {
                    categorySpinner.setSelection(i);
                    break;
                }
            }

            if (recipe.getImageUri() != null) {
                selectedImageUri = Uri.parse(recipe.getImageUri());
                imageView.setImageURI(selectedImageUri);
            }
            // Show delete button only when editing
            deleteBtn.setVisibility(View.VISIBLE);
        } else {
            // Hide delete button when adding new recipe
            deleteBtn.setVisibility(View.GONE);
            // Set default category to "Other" (last item in array)
            categorySpinner.setSelection(Recipe.CATEGORIES.length - 1);
        }

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
            String tagsString = tagsInput.getText().toString().trim();

            // Validate required fields
            if (title.isEmpty()) {
                titleInput.setError("Title is required");
                titleInput.requestFocus();
                return;
            }

            if (desc.isEmpty()) {
                descInput.setError("Description is required");
                descInput.requestFocus();
                return;
            }

            // Create recipe with category and tags
            Recipe r = new Recipe(title, desc, img, category, new ArrayList<>());
            r.setTagsFromString(tagsString);

            if (editingPosition == -1)
                RecipeManager.recipes.add(r);
            else
                RecipeManager.recipes.set(editingPosition, r);

            RecipeManager.saveRecipes(this); // Save to storage
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

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.copyOfRange(Recipe.CATEGORIES, 1, Recipe.CATEGORIES.length) // Skip "All" category
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
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