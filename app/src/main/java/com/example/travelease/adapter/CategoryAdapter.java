package com.example.travelease.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.travelease.R;
import com.example.travelease.databinding.ItemCategoryBinding;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private final List<String> categories;
    private final List<Integer> icons; 
    private int selectedPosition = 0;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(String category, int position);
    }

    public CategoryAdapter(List<String> categories, List<Integer> icons, OnCategoryClickListener listener) {
        this.categories = categories;
        this.icons = icons;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(categories.get(position), icons.get(position), position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;

        ViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String category, int iconRes, boolean isSelected) {
            binding.categoryName.setText(category);
            binding.categoryIcon.setImageResource(iconRes);

            if (isSelected) {
                binding.cardContainer.setStrokeColor(binding.getRoot().getContext().getColor(R.color.primary));
                binding.categoryIcon.setColorFilter(binding.getRoot().getContext().getColor(R.color.primary));
                binding.categoryName.setTextColor(binding.getRoot().getContext().getColor(R.color.primary));
                binding.cardContainer.setCardBackgroundColor(binding.getRoot().getContext().getColor(R.color.primary_light_bg));
            } else {
                binding.cardContainer.setStrokeColor(Color.TRANSPARENT);
                binding.categoryIcon.setColorFilter(binding.getRoot().getContext().getColor(R.color.text_medium));
                binding.categoryName.setTextColor(binding.getRoot().getContext().getColor(R.color.text_medium));
                binding.cardContainer.setCardBackgroundColor(binding.getRoot().getContext().getColor(R.color.surface_container_high));
            }

            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    int oldSelected = selectedPosition;
                    selectedPosition = position;
                    notifyItemChanged(oldSelected);
                    notifyItemChanged(selectedPosition);
                    if (listener != null) {
                        listener.onCategoryClick(category, selectedPosition);
                    }
                }
            });
        }
    }
}
