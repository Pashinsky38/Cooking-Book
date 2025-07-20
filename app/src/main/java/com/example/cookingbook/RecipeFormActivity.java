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

public class RecipeFormActivity extends AppCompatActivity {

    private static final int IMAGE_PICK_CODE = 101;
    private static final int PERMISSION_CODE = 102;

    private EditText titleInput, descInput;
    private ImageView imageView;
    private Uri selectedImageUri;
    private int editingPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_form);

        titleInput = findViewById(R.id.titleInput);
        descInput = findViewById(R.id.descInput);
        imageView = findViewById(R.id.recipeImageView);
        Button deleteBtn = findViewById(R.id.deleteBtn);
        Button chooseImg = findViewById(R.id.chooseImageBtn);
        Button saveBtn = findViewById(R.id.saveBtn);

        if (getIntent().hasExtra("position")) {
            editingPosition = getIntent().getIntExtra("position", -1);
            Recipe recipe = RecipeManager.recipes.get(editingPosition);
            titleInput.setText(recipe.getTitle());
            descInput.setText(recipe.getDescription());
            if (recipe.getImageUri() != null) {
                selectedImageUri = Uri.parse(recipe.getImageUri());
                imageView.setImageURI(selectedImageUri);
            }
            // Show delete button only when editing
            deleteBtn.setVisibility(View.VISIBLE);
        } else {
            // Hide delete button when adding new recipe
            deleteBtn.setVisibility(View.GONE);
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
            String title = titleInput.getText().toString();
            String desc = descInput.getText().toString();
            String img = selectedImageUri != null ? selectedImageUri.toString() : null;

            Recipe r = new Recipe(title, desc, img);

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