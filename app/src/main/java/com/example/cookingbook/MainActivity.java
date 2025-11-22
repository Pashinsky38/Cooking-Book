package com.example.cookingbook;

import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {

    private RecipeAdapter adapter;
    private ActivityMainBinding binding;
    private LinearLayout emptyStateLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        emptyStateLayout = findViewById(R.id.emptyStateLayout);

        RecipeManager.loadRecipes(this);

        setupRecyclerView();
        setupSearch();
        setupAddButton();
        setupFocusAndCursorManagement();
        setupCategoryFilter();
        updateEmptyState();
    }

    private void setupRecyclerView() {
        binding.recipeList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, RecipeManager.recipes);
        binding.recipeList.setAdapter(adapter);
    }

    private void setupAddButton() {
        binding.addBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RecipeFormActivity.class)));
        emptyStateLayout.findViewById(R.id.emptyStateAddBtn).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RecipeFormActivity.class)));
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

    private void setupFocusAndCursorManagement() {
        binding.searchInput.setCursorVisible(false);

        binding.searchInput.setOnFocusChangeListener((v, hasFocus) -> binding.searchInput.setCursorVisible(hasFocus));

        binding.recipeList.setOnTouchListener((v, event) -> {
            if (binding.searchInput.hasFocus()) {
                binding.searchInput.clearFocus();
                binding.searchInput.setCursorVisible(false);
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return false;
        });
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
        }
        updateEmptyState();
    }
}