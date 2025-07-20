package com.example.cookingbook;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private RecipeAdapter adapter;
    private RecyclerView recipeList;
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecipeManager.loadRecipes(this); // Load saved recipes

        initializeViews();
        setupRecyclerView();
        setupSearch();
        setupAddButton();
        setupFocusAndCursorManagement();
    }

    private void initializeViews() {
        searchInput = findViewById(R.id.searchInput);
        recipeList = findViewById(R.id.recipeList);
    }

    private void setupRecyclerView() {
        recipeList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, RecipeManager.recipes);
        recipeList.setAdapter(adapter);
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
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
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
    }
}