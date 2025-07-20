package com.example.cookingbook;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    RecipeAdapter adapter;
    RecyclerView recipeList;
    EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecipeManager.loadRecipes(this); // Load saved recipes

        searchInput = findViewById(R.id.searchInput);
        recipeList = findViewById(R.id.recipeList);
        recipeList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, RecipeManager.recipes);
        recipeList.setAdapter(adapter);

        Button addBtn = findViewById(R.id.addBtn);
        addBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RecipeFormActivity.class)));

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

        // Handle focus changes to control cursor visibility
        searchInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // Hide cursor when EditText loses focus
                    searchInput.setCursorVisible(false);
                } else {
                    // Show cursor when EditText gains focus
                    searchInput.setCursorVisible(true);
                }
            }
        });

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
        adapter.notifyDataSetChanged(); // Refresh list to reflect any changes
        // Clear search when returning to main activity
        searchInput.setText("");
        // Make sure cursor is hidden when returning
        searchInput.clearFocus();
        searchInput.setCursorVisible(false);
    }
}