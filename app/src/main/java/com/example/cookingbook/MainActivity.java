package com.example.cookingbook;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private RecipeAdapter adapter;
    private RecyclerView recipeList;
    private EditText searchInput;
    private Spinner categorySpinner;
    private String currentCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecipeManager.loadRecipes(this); // Load saved recipes

        initializeViews();
        setupRecyclerView();
        setupCategorySpinner();
        setupSearch();
        setupAddButton();
        setupFocusAndCursorManagement();
    }

    private void initializeViews() {
        searchInput = findViewById(R.id.searchInput);
        recipeList = findViewById(R.id.recipeList);
        categorySpinner = findViewById(R.id.categorySpinner);
    }

    private void setupRecyclerView() {
        recipeList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, RecipeManager.recipes);
        recipeList.setAdapter(adapter);
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Recipe.CATEGORIES
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setSelection(0); // Set "All" as default

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCategory = Recipe.CATEGORIES[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupAddButton() {
        Button addBtn = findViewById(R.id.addBtn);
        addBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RecipeFormActivity.class)));
    }

    private void setupSearch() {
        // Add search functionality
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    private void applyFilters() {
        String searchQuery = searchInput.getText().toString().trim();

        if (currentCategory.equals("All") && searchQuery.isEmpty()) {
            // Show all recipes
            adapter.filter("");
        } else if (currentCategory.equals("All")) {
            // Only search filter
            adapter.filter(searchQuery);
        } else if (searchQuery.isEmpty()) {
            // Only category filter
            adapter.filterByCategory(currentCategory);
        } else {
            // Both filters: first filter by category, then by search
            adapter.filterByCategory(currentCategory);
            adapter.filter(searchQuery);
        }
    }

    private void setupFocusAndCursorManagement() {
        // Handle focus changes to control cursor visibility
        searchInput.setOnFocusChangeListener((v, hasFocus) -> searchInput.setCursorVisible(hasFocus));

        // Set cursor initially invisible
        searchInput.setCursorVisible(false);

        // Handle clicks on RecyclerView to clear focus from search input
        recipeList.setOnTouchListener((v, event) -> {
            if (searchInput.hasFocus()) {
                searchInput.clearFocus();
            }
            return false; // Don't consume the touch event
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged(); // Refresh list to reflect any changes
        }
        if (searchInput != null) {
            searchInput.setText(""); // Clear search when returning to main activity
            searchInput.clearFocus(); // Make sure cursor is hidden when returning
            searchInput.setCursorVisible(false);
        }
        if (categorySpinner != null) {
            categorySpinner.setSelection(0); // Reset to "All" category
            currentCategory = "All";
        }
    }
}