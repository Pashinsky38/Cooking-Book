package com.example.cookingbook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    RecipeAdapter adapter;
    RecyclerView recipeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecipeManager.loadRecipes(this); // Load saved recipes

        recipeList = findViewById(R.id.recipeList);
        recipeList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, RecipeManager.recipes);
        recipeList.setAdapter(adapter);

        Button addBtn = findViewById(R.id.addBtn);
        addBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RecipeFormActivity.class)));
    }


    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged(); // Refresh list to reflect any changes
    }
}
