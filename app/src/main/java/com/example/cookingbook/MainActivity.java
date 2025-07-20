package com.example.cookingbook;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.cookingbook.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private RecipeAdapter adapter;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        RecipeManager.loadRecipes(this); // Load saved recipes

        setupRecyclerView();
        setupSearch();
        setupAddButton();
        setupFocusAndCursorManagement();
        setupCategoryFilter();
    }

    private void setupRecyclerView() {
        binding.recipeList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, RecipeManager.recipes);
        binding.recipeList.setAdapter(adapter);
    }

    private void setupAddButton() {
        binding.addBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RecipeFormActivity.class)));
    }

    private void setupSearch() {

        // Add search functionality
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    private void setupCategoryFilter() {
        // Create category spinner
        String[] categories = {"All", "Appetizers", "Main Course", "Desserts", "Beverages", "Salads", "Other"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.categorySpinner.setAdapter(categoryAdapter);

        binding.categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categories[position];
                adapter.filterByCategory(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupFocusAndCursorManagement() {
        // Set cursor initially invisible
        binding.searchInput.setCursorVisible(false);

        // Handle focus and touch events to manage cursor and focus efficiently
        binding.searchInput.setOnFocusChangeListener((v, hasFocus) -> binding.searchInput.setCursorVisible(hasFocus));

        binding.recipeList.setOnTouchListener((v, event) -> {
            if (binding.searchInput.hasFocus()) {
                binding.searchInput.clearFocus();
                binding.searchInput.setCursorVisible(false);
            }
            // Accessibility: call performClick for touch events as recommended
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            // Use more specific change event instead of notifyDataSetChanged
            int itemCount = adapter.getItemCount();
            if (itemCount > 0) {
                adapter.notifyItemRangeChanged(0, itemCount);
            }
        }
        if (binding != null) {
            binding.searchInput.setText(""); // Clear search when returning to main activity
            binding.searchInput.clearFocus(); // Make sure cursor is hidden when returning
            binding.searchInput.setCursorVisible(false);
        }
    }
}