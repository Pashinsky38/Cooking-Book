package com.example.cookingbook;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Recipe> recipes;
    private final ArrayList<Recipe> filteredRecipes;

    public RecipeAdapter(Context ctx, ArrayList<Recipe> list) {
        this.context = ctx;
        this.recipes = list;
        this.filteredRecipes = new ArrayList<>(list); // Initialize filtered list with all recipes
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
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

        // Improved Glide configuration for better quality
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fitCenter() // Better quality than centerCrop for varied aspect ratios
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both original and resized
                .skipMemoryCache(false) // Enable memory cache for better performance
                .dontTransform(); // Preserve original quality when possible

        if (r.getImageUri() != null && !r.getImageUri().isEmpty()) {
            Glide.with(context)
                    .load(Uri.parse(r.getImageUri()))
                    .apply(requestOptions)
                    .into(holder.image);
        } else {
            Glide.with(context)
                    .load(R.drawable.placeholder)
                    .apply(requestOptions)
                    .into(holder.image);
        }

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

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        // Clear Glide requests for recycled views
        Glide.with(context).clear(holder.image);
    }

    // Method to filter recipes based on search query
    public void filter(String query) {
        ArrayList<Recipe> newFilteredRecipes = new ArrayList<>();

        if (query.isEmpty()) {
            newFilteredRecipes.addAll(recipes);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Recipe recipe : recipes) {
                // Search in title and description
                if ((recipe.getTitle() != null && recipe.getTitle().toLowerCase().contains(lowerCaseQuery)) ||
                        (recipe.getDescription() != null && recipe.getDescription().toLowerCase().contains(lowerCaseQuery))) {
                    newFilteredRecipes.add(recipe);
                }
            }
        }

        // Calculate differences and use specific notify methods
        int oldSize = filteredRecipes.size();
        int newSize = newFilteredRecipes.size();

        filteredRecipes.clear();
        filteredRecipes.addAll(newFilteredRecipes);

        if (oldSize > newSize) {
            notifyItemRangeRemoved(newSize, oldSize - newSize);
            if (newSize > 0) {
                notifyItemRangeChanged(0, newSize);
            }
        } else if (oldSize < newSize) {
            if (oldSize > 0) {
                notifyItemRangeChanged(0, oldSize);
            }
            notifyItemRangeInserted(oldSize, newSize - oldSize);
        } else {
            // Same size, just changed content
            notifyItemRangeChanged(0, newSize);
        }
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