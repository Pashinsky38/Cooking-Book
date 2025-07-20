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

    public RecipeAdapter(Context ctx, ArrayList<Recipe> list) {
        this.context = ctx;
        this.recipes = list;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView description;
        final ImageView image;
        final Button editBtn;
        final Button deleteBtn;

        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.recipeTitle);
            description = v.findViewById(R.id.recipeDesc);
            image = v.findViewById(R.id.recipeImage);
            editBtn = v.findViewById(R.id.editBtn);
            deleteBtn = v.findViewById(R.id.deleteBtn);
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
        final Recipe r = recipes.get(position);
        holder.title.setText(r.getTitle());
        holder.description.setText(r.getDescription());

        if (r.getImageUri() != null)
            holder.image.setImageURI(Uri.parse(r.getImageUri()));
        else
            holder.image.setImageResource(R.drawable.placeholder); // add placeholder image

        holder.editBtn.setOnClickListener(view -> {
            Intent intent = new Intent(context, RecipeFormActivity.class);
            intent.putExtra("position", holder.getAdapterPosition());
            context.startActivity(intent);
        });

        holder.deleteBtn.setOnClickListener(view -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                recipes.remove(currentPosition);
                RecipeManager.saveRecipes(context); // Save changes
                notifyItemRemoved(currentPosition);
            }
        });

    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }
}
