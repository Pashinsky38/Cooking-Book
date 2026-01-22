package com.example.cookingbook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.cookingbook.databinding.ActivityMainBinding;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private RecipeAdapter adapter;
    private ActivityMainBinding binding;
    private LinearLayout emptyStateLayout;
    private MaterialButton toggleDisplayBtn;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "display_preferences";
    private static final String KEY_COMPACT_MODE = "compact_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        toggleDisplayBtn = findViewById(R.id.toggleDisplayBtn);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        RecipeManager.loadRecipes(this);

        setupRecyclerView();
        setupSearch();
        setupAddButton();
        setupFocusAndCursorManagement();
        setupCategoryFilter();
        setupDietaryFilter();
        setupToggleDisplayButton();
        updateEmptyState();
    }

    private void setupRecyclerView() {
        binding.recipeList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, RecipeManager.recipes);

        // Load saved display mode preference
        boolean compactMode = prefs.getBoolean(KEY_COMPACT_MODE, false);
        adapter.setDisplayMode(compactMode);
        updateToggleButtonText();

        binding.recipeList.setAdapter(adapter);
    }

    private void setupAddButton() {
        binding.addBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RecipeFormActivity.class)));
        emptyStateLayout.findViewById(R.id.emptyStateAddBtn).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RecipeFormActivity.class)));
    }

    private void setupToggleDisplayButton() {
        toggleDisplayBtn.setOnClickListener(v -> {
            adapter.toggleDisplayMode();
            updateToggleButtonText();

            // Save preference
            prefs.edit().putBoolean(KEY_COMPACT_MODE, adapter.isCompactMode()).apply();
        });
    }

    private void updateToggleButtonText() {
        if (adapter.isCompactMode()) {
            toggleDisplayBtn.setText(R.string.full_view);
            toggleDisplayBtn.setIconResource(android.R.drawable.ic_menu_view);
        } else {
            toggleDisplayBtn.setText(R.string.compact_view);
            toggleDisplayBtn.setIconResource(android.R.drawable.ic_menu_sort_by_size);
        }
    }

    private void setupSearch() {
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupCategoryFilter() {
        String[] categories = {"All", "Appetizers", "Main Course", "Desserts", "Beverages", "Salads", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.categorySpinner.setAdapter(categoryAdapter);

        binding.categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categories[position];
                adapter.filterByCategory(selectedCategory);
                updateEmptyState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupDietaryFilter() {
        String[] dietaryOptions = {"All", "🌱 Vegetarian", "🌿 Vegan", "🌾 Gluten-Free", "🥩 Meat"};
        ArrayAdapter<String> dietaryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dietaryOptions);
        dietaryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.dietarySpinner.setAdapter(dietaryAdapter);

        binding.dietarySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDietary = dietaryOptions[position];
                adapter.filterByDietary(selectedDietary);
                updateEmptyState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupFocusAndCursorManagement() {
        // Initially hide cursor
        binding.searchInput.setCursorVisible(false);

        // Show/hide cursor based on focus
        binding.searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            binding.searchInput.setCursorVisible(hasFocus);

            // Hide keyboard when focus is lost
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });

        // Clear focus when tapping on the RecyclerView
        binding.recipeList.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (binding.searchInput.hasFocus()) {
                    binding.searchInput.clearFocus();
                    // Cursor visibility is now handled by OnFocusChangeListener
                }
            }
            return false; // Allow the touch event to continue for scrolling
        });
    }

    // Helper method to hide the keyboard
    private void hideKeyboard(View view) {
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            binding.recipeList.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            binding.recipeList.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            int itemCount = adapter.getItemCount();
            if (itemCount > 0) {
                adapter.notifyItemRangeChanged(0, itemCount);
            }
        }
        if (binding != null) {
            binding.searchInput.setText("");
            binding.searchInput.clearFocus();
            binding.searchInput.setCursorVisible(false);
            binding.categorySpinner.setSelection(0);
            binding.dietarySpinner.setSelection(0);
        }
        updateEmptyState();
    }
}