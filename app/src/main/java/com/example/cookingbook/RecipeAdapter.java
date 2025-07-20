package com.example.cookingbook;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Recipe> recipes;
    private ArrayList<Recipe> filteredRecipes;

    public RecipeAdapter(Context ctx, ArrayList<Recipe> list) {
        this.context = ctx;
        this.recipes = list;
        this.filteredRecipes = new ArrayList<>(list); // Initialize filtered list with all recipes
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView description;
        final ImageView image;
        final Button editBtn;
        final Button shareBtn;

        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.recipeTitle);
            description = v.findViewById(R.id.recipeDesc);
            image = v.findViewById(R.id.recipeImage);
            editBtn = v.findViewById(R.id.editBtn);
            shareBtn = v.findViewById(R.id.shareBtn);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Recipe r = filteredRecipes.get(position);
        holder.title.setText(r.getTitle());
        holder.description.setText(r.getDescription());

        if (r.getImageUri() != null)
            holder.image.setImageURI(Uri.parse(r.getImageUri()));
        else
            holder.image.setImageResource(R.drawable.placeholder); // add placeholder image

        holder.editBtn.setOnClickListener(view -> {
            // Find the original position in the main recipes list
            int originalPosition = recipes.indexOf(r);
            Intent intent = new Intent(context, RecipeFormActivity.class);
            intent.putExtra("position", originalPosition);
            context.startActivity(intent);
        });

        holder.shareBtn.setOnClickListener(view -> shareRecipe(r));
    }

    @Override
    public int getItemCount() {
        return filteredRecipes.size();
    }

    // Method to filter recipes based on search query
    public void filter(String query) {
        filteredRecipes.clear();

        if (query.isEmpty()) {
            filteredRecipes.addAll(recipes);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Recipe recipe : recipes) {
                // Search in title and description
                if ((recipe.getTitle() != null && recipe.getTitle().toLowerCase().contains(lowerCaseQuery)) ||
                        (recipe.getDescription() != null && recipe.getDescription().toLowerCase().contains(lowerCaseQuery))) {
                    filteredRecipes.add(recipe);
                }
            }
        }

        notifyDataSetChanged();
    }

    // Method to refresh the filtered list (call this when recipes are added/removed)
    public void refreshFilter() {
        filteredRecipes.clear();
        filteredRecipes.addAll(recipes);
        notifyDataSetChanged();
    }

    private void shareRecipe(Recipe recipe) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        // Create the text to share
        StringBuilder shareText = new StringBuilder();
        shareText.append(context.getString(R.string.recipe_prefix)).append(recipe.getTitle()).append("\n\n");
        shareText.append(context.getString(R.string.description_prefix)).append(recipe.getDescription());
        shareText.append(context.getString(R.string.share_suffix));

        if (recipe.getImageUri() != null) {
            // Share with image
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(recipe.getImageUri()));
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_subject_prefix) + recipe.getTitle());
        } else {
            // Share text only
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_subject_prefix) + recipe.getTitle());
        }

        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_recipe_chooser)));
        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.share_error), Toast.LENGTH_SHORT).show();
        }
    }
}