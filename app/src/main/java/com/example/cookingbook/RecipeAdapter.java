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
    private String currentSearchQuery = "";
    private String currentCategory = "All";
    private String currentDietary = "All";

    public RecipeAdapter(Context ctx, ArrayList<Recipe> list) {
        this.context = ctx;
        this.recipes = list;
        this.filteredRecipes = new ArrayList<>(list);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView description;
        final TextView ingredientsPreview;
        final LinearLayout dietaryTagsContainer;
        final ImageView image;
        final Button editBtn;
        final Button shareBtn;

        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.recipeTitle);
            description = v.findViewById(R.id.recipeDesc);
            ingredientsPreview = v.findViewById(R.id.ingredientsPreview);
            dietaryTagsContainer = v.findViewById(R.id.dietaryTagsContainer);
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

        // Display ingredients preview
        if (r.getIngredients() != null && !r.getIngredients().isEmpty()) {
            StringBuilder ingredientsText = new StringBuilder("Ingredients: ");
            for (int i = 0; i < Math.min(3, r.getIngredients().size()); i++) {
                if (i > 0) ingredientsText.append(", ");
                ingredientsText.append(r.getIngredients().get(i));
            }
            if (r.getIngredients().size() > 3) {
                ingredientsText.append(", +").append(r.getIngredients().size() - 3).append(" more");
            }
            holder.ingredientsPreview.setText(ingredientsText.toString());
            holder.ingredientsPreview.setVisibility(View.VISIBLE);
        } else {
            holder.ingredientsPreview.setVisibility(View.GONE);
        }

        // Display dietary tags
        holder.dietaryTagsContainer.removeAllViews();
        if (r.isVegetarian()) {
            addDietaryTag(holder.dietaryTagsContainer, "🌱 Vegetarian");
        }
        if (r.isVegan()) {
            addDietaryTag(holder.dietaryTagsContainer, "🌿 Vegan");
        }
        if (r.isGlutenFree()) {
            addDietaryTag(holder.dietaryTagsContainer, "🌾 Gluten-Free");
        }
        if (r.hasMeat()) {
            addDietaryTag(holder.dietaryTagsContainer, "🥩 Meat");
        }

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .dontTransform();

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
            int originalPosition = recipes.indexOf(r);
            Intent intent = new Intent(context, RecipeFormActivity.class);
            intent.putExtra("position", originalPosition);
            context.startActivity(intent);
        });

        holder.shareBtn.setOnClickListener(view -> shareRecipe(r));
    }

    private void addDietaryTag(LinearLayout container, String tagText) {
        TextView tag = new TextView(context);
        tag.setText(tagText);
        tag.setTextSize(12);
        tag.setTextColor(0xFFFFFFFF);
        tag.setBackgroundColor(0xFF4CAF50);
        tag.setPadding(12, 6, 12, 6);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(4, 0, 4, 0);
        tag.setLayoutParams(params);
        container.addView(tag);
    }

    @Override
    public int getItemCount() {
        return filteredRecipes.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(context).clear(holder.image);
    }

    public void filter(String query) {
        currentSearchQuery = query;
        applyFilters();
    }

    public void filterByCategory(String category) {
        currentCategory = category;
        applyFilters();
    }

    public void filterByDietary(String dietary) {
        currentDietary = dietary;
        applyFilters();
    }

    private void applyFilters() {
        ArrayList<Recipe> newFilteredRecipes = new ArrayList<>();

        for (Recipe recipe : recipes) {
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    (recipe.getTitle() != null && recipe.getTitle().toLowerCase().contains(currentSearchQuery.toLowerCase())) ||
                    (recipe.getDescription() != null && recipe.getDescription().toLowerCase().contains(currentSearchQuery.toLowerCase()));

            boolean matchesCategory = currentCategory.equals("All") ||
                    (recipe.getCategory() != null && recipe.getCategory().equals(currentCategory));

            boolean matchesDietary = currentDietary.equals("All");
            if (!matchesDietary) {
                if (currentDietary.contains("Vegetarian")) {
                    matchesDietary = recipe.isVegetarian();
                } else if (currentDietary.contains("Vegan")) {
                    matchesDietary = recipe.isVegan();
                } else if (currentDietary.contains("Gluten-Free")) {
                    matchesDietary = recipe.isGlutenFree();
                } else if (currentDietary.contains("Meat")) {
                    matchesDietary = recipe.hasMeat();
                }
            }

            if (matchesSearch && matchesCategory && matchesDietary) {
                newFilteredRecipes.add(recipe);
            }
        }

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
            notifyItemRangeChanged(0, newSize);
        }
    }

    private void shareRecipe(Recipe recipe) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        StringBuilder shareText = new StringBuilder();
        shareText.append(context.getString(R.string.recipe_prefix)).append(recipe.getTitle()).append("\n\n");
        shareText.append(context.getString(R.string.description_prefix)).append(recipe.getDescription()).append("\n\n");

        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            shareText.append("📋 Ingredients:\n");
            for (String ingredient : recipe.getIngredients()) {
                shareText.append("• ").append(ingredient).append("\n");
            }
            shareText.append("\n");
        }

        shareText.append(context.getString(R.string.share_suffix));

        if (recipe.getImageUri() != null) {
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(recipe.getImageUri()));
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_subject_prefix) + recipe.getTitle());
        } else {
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