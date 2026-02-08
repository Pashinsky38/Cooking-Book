package com.example.cookingbook;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
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

import com.example.cookingbook.databinding.ActivityRecipeFormBinding;

import java.util.ArrayList;
import java.util.Arrays;

public class RecipeFormActivity extends AppCompatActivity {

    private static final int IMAGE_PICK_CODE = 101;
    private static final int PERMISSION_CODE = 102;

    private ActivityRecipeFormBinding binding;
    private Uri selectedImageUri;
    private int editingPosition = -1;
    private ArrayList<String> ingredients;

    // Default gradient colors
    private int currentStartColor = 0xFFFF6B6B;
    private int currentEndColor = 0xFFFFD93D;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ingredients = new ArrayList<>();

        String[] categories = {"Appetizers", "Main Course", "Desserts", "Beverages", "Salads", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.categorySpinner.setAdapter(categoryAdapter);

        // Set initial gradient
        applyGradient(currentStartColor, currentEndColor, false);

        // Check if we're editing an existing recipe
        if (getIntent().hasExtra("position")) {
            editingPosition = getIntent().getIntExtra("position", -1);
            Recipe recipe = RecipeManager.recipes.get(editingPosition);
            binding.titleInput.setText(recipe.getTitle());
            binding.descInput.setText(recipe.getDescription());

            if (recipe.getImageUri() != null && !recipe.getImageUri().isEmpty()) {
                selectedImageUri = Uri.parse(recipe.getImageUri());
                binding.recipeImageView.setImageURI(selectedImageUri);

                // Extract colors from the image and apply gradient
                extractAndApplyColors(selectedImageUri);
            }

            // Set dietary filters
            binding.vegetarianCheckbox.setChecked(recipe.isVegetarian());
            binding.veganCheckbox.setChecked(recipe.isVegan());
            binding.glutenFreeCheckbox.setChecked(recipe.isGlutenFree());
            binding.meatCheckbox.setChecked(recipe.hasMeat());

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
                    binding.categorySpinner.setSelection(categoryPosition);
                }
            }

            // Show delete button only when editing
            binding.deleteBtn.setVisibility(View.VISIBLE);
        } else {
            // Hide delete button when adding new recipe
            binding.deleteBtn.setVisibility(View.GONE);
        }

        binding.addIngredientBtn.setOnClickListener(v -> {
            String ingredient = binding.ingredientInput.getText().toString().trim();
            if (!ingredient.isEmpty()) {
                ingredients.add(ingredient);
                binding.ingredientInput.setText("");
                displayIngredients();
            } else {
                Toast.makeText(this, "Please enter an ingredient", Toast.LENGTH_SHORT).show();
            }
        });

        binding.chooseImageBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_CODE);
            } else {
                pickImageFromGallery();
            }
        });

        binding.saveBtn.setOnClickListener(v -> {
            String title = binding.titleInput.getText().toString().trim();
            String desc = binding.descInput.getText().toString().trim();
            String img = selectedImageUri != null ? selectedImageUri.toString() : null;
            String category = binding.categorySpinner.getSelectedItem().toString();

            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a recipe title", Toast.LENGTH_SHORT).show();
                return;
            }

            Recipe r = new Recipe(title, desc, img, category, ingredients,
                    binding.vegetarianCheckbox.isChecked(),
                    binding.veganCheckbox.isChecked(),
                    binding.glutenFreeCheckbox.isChecked(),
                    binding.meatCheckbox.isChecked());

            if (editingPosition == -1)
                RecipeManager.recipes.add(r);
            else
                RecipeManager.recipes.set(editingPosition, r);

            RecipeManager.saveRecipes(this);
            finish();
        });

        binding.deleteBtn.setOnClickListener(v -> new AlertDialog.Builder(this)
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

            // Adjust text colors based on background brightness
            adjustTextColors(startColor);
        });
    }

    private void adjustTextColors(int backgroundColor) {
        // Calculate brightness of the background color
        int textColor = isColorDark(backgroundColor) ? 0xFFFFFFFF : 0xFF000000;
        int hintColor = adjustAlpha(textColor, 0.6f);

        // Apply to all text views
        binding.categoryLabel.setTextColor(textColor);
        binding.ingredientsLabel.setTextColor(textColor);
        binding.dietaryLabel.setTextColor(textColor);

        // Apply to EditTexts
        binding.titleInput.setTextColor(textColor);
        binding.titleInput.setHintTextColor(hintColor);
        binding.descInput.setTextColor(textColor);
        binding.descInput.setHintTextColor(hintColor);
        binding.ingredientInput.setTextColor(textColor);
        binding.ingredientInput.setHintTextColor(hintColor);

        // Apply to TextInputLayout borders and hints
        updateTextInputLayoutColors(binding.titleInputLayout, textColor, hintColor);
        updateTextInputLayoutColors(binding.descInputLayout, textColor, hintColor);
        updateTextInputLayoutColors(binding.ingredientInputLayout, textColor, hintColor);

        // Apply to spinner (category dropdown)
        updateSpinnerColors(binding.categorySpinner, textColor, backgroundColor);

        // Apply to checkboxes (both text and checkbox color)
        updateCheckBoxColors(binding.vegetarianCheckbox, textColor);
        updateCheckBoxColors(binding.veganCheckbox, textColor);
        updateCheckBoxColors(binding.glutenFreeCheckbox, textColor);
        updateCheckBoxColors(binding.meatCheckbox, textColor);

        // Apply to buttons
        updateButtonColors(binding.chooseImageBtn, textColor, backgroundColor);
        updateButtonColors(binding.addIngredientBtn, textColor, backgroundColor);
        updateButtonColors(binding.saveBtn, textColor, backgroundColor);
        updateButtonColors(binding.deleteBtn, textColor, backgroundColor);

        // Apply to ingredients list
        updateIngredientsTextColor(textColor);
    }

    private void updateTextInputLayoutColors(com.google.android.material.textfield.TextInputLayout layout, int textColor, int hintColor) {
        if (layout != null) {
            // Set box stroke color (border color)
            layout.setBoxStrokeColor(textColor);

            // Set hint text color
            layout.setDefaultHintTextColor(ColorStateList.valueOf(hintColor));

            // Set the hint text color when focused
            layout.setHintTextColor(ColorStateList.valueOf(textColor));
        }
    }

    private void updateSpinnerColors(Spinner spinner, int textColor, int backgroundColor) {
        if (spinner != null) {
            // Determine the spinner background color (lighter or darker version of the gradient)
            int spinnerBgColor = isColorDark(backgroundColor) ?
                    ColorUtils.adjustBrightness(backgroundColor, 1.3f) :
                    ColorUtils.adjustBrightness(backgroundColor, 0.9f);

            // Set spinner background
            spinner.setBackgroundColor(spinnerBgColor);
            spinner.setPadding(16, 16, 16, 16);

            // Update the spinner's text color by refreshing its adapter
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
            if (adapter != null) {
                // Create a custom adapter that uses the dynamic text color
                ArrayAdapter<String> newAdapter = new ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_item,
                        new String[]{"Appetizers", "Main Course", "Desserts", "Beverages", "Salads", "Other"}) {
                    @Override
                    public View getView(int position, View convertView, android.view.ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView textView = (TextView) view;
                        textView.setTextColor(textColor);
                        return view;
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                        View view = super.getDropDownView(position, convertView, parent);
                        TextView textView = (TextView) view;
                        textView.setTextColor(textColor);
                        textView.setBackgroundColor(spinnerBgColor);
                        textView.setPadding(32, 24, 32, 24);
                        return view;
                    }
                };
                newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                int selectedPosition = spinner.getSelectedItemPosition();
                spinner.setAdapter(newAdapter);
                spinner.setSelection(selectedPosition);
            }
        }
    }

    private void updateCheckBoxColors(CheckBox checkBox, int textColor) {
        if (checkBox != null) {
            // Set the text color
            checkBox.setTextColor(textColor);

            // Create color state list for the checkbox
            // When checked, the box will be textColor and the tick will be the opposite
            int tickColor = isColorDark(currentStartColor) ? 0xFF000000 : 0xFFFFFFFF;

            // For the checkbox button (the box and tick)
            int[][] states = new int[][] {
                    new int[] { android.R.attr.state_checked },  // checked
                    new int[] { -android.R.attr.state_checked }  // unchecked
            };

            int[] colors = new int[] {
                    textColor,  // checked: box color
                    textColor   // unchecked: box color
            };

            ColorStateList colorStateList = new ColorStateList(states, colors);
            checkBox.setButtonTintList(colorStateList);

            // Set the compound button tint mode to change the tick color
            checkBox.setButtonTintMode(PorterDuff.Mode.SRC_IN);

            // For API 21+, we can use CompoundButtonCompat to set tick color
            // The tick color will automatically contrast with the box color
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                // Create a custom drawable for the checkbox to control tick color
                android.graphics.drawable.Drawable drawable = checkBox.getButtonDrawable();
                if (drawable != null) {
                    drawable.setTint(textColor);
                }
            }
        }
    }

    private void updateButtonColors(com.google.android.material.button.MaterialButton button, int textColor, int backgroundColor) {
        if (button != null) {
            // Determine appropriate button colors
            boolean isDark = isColorDark(backgroundColor);

            if (button == binding.saveBtn) {
                // Save button: filled button with contrasting colors
                button.setBackgroundTintList(ColorStateList.valueOf(textColor));
                button.setTextColor(backgroundColor);
            } else {
                // Outlined buttons (choose image, add ingredient, delete)
                button.setStrokeColor(ColorStateList.valueOf(textColor));
                button.setTextColor(textColor);
                button.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));

                // Special color for delete button (keep it reddish if possible)
                if (button == binding.deleteBtn) {
                    int deleteColor = isDark ? 0xFFFF6B6B : 0xFFD32F2F;
                    button.setStrokeColor(ColorStateList.valueOf(deleteColor));
                    button.setTextColor(deleteColor);
                }
            }
        }
    }

    private int adjustAlpha(int color, float alpha) {
        int a = Math.round(255 * alpha);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private boolean isColorDark(int color) {
        // Calculate luminance using the relative luminance formula
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // Calculate perceived brightness
        double brightness = (0.299 * r + 0.587 * g + 0.114 * b);

        return brightness < 128;
    }

    private void updateIngredientsTextColor(int textColor) {
        for (int i = 0; i < binding.ingredientsList.getChildCount(); i++) {
            View child = binding.ingredientsList.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout row = (LinearLayout) child;
                if (row.getChildCount() > 0 && row.getChildAt(0) instanceof TextView) {
                    TextView ingredientText = (TextView) row.getChildAt(0);
                    ingredientText.setTextColor(textColor);
                }
                // Update remove button color
                if (row.getChildCount() > 1 && row.getChildAt(1) instanceof Button) {
                    Button removeBtn = (Button) row.getChildAt(1);
                    removeBtn.setTextColor(textColor);
                }
            }
        }
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
            binding.dynamicGradientBackground.setBackground(newGradient);
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

            binding.dynamicGradientBackground.setBackground(gradient);
        });

        animator.start();
    }

    private void displayIngredients() {
        binding.ingredientsList.removeAllViews();

        // Determine current text color based on background
        int textColor = isColorDark(currentStartColor) ? 0xFFFFFFFF : 0xFF000000;

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
            ingredientText.setTextColor(textColor);

            Button removeBtn = new Button(this);
            removeBtn.setText("Remove");
            removeBtn.setTextColor(textColor);
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
            binding.ingredientsList.addView(ingredientRow);
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
            binding.recipeImageView.setImageURI(selectedImageUri);

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