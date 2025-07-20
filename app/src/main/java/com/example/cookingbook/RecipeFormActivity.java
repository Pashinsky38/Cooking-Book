package com.example.cookingbook;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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
                Toast.makeText(this, "Permission to access images denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
