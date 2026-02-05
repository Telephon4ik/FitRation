package com.fitration.ui.user.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.fitration.R;
import com.fitration.models.Meal;
import com.fitration.models.Nutrition;
import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private List<Meal> meals;
    private OnMealClickListener listener;
    private boolean isForUser = true; // true = пользователь, false = тренер

    public interface OnMealClickListener {
        void onMealClick(Meal meal);
        void onMarkAsEaten(Meal meal, boolean isEaten);
    }

    public MealAdapter() {
        // Конструктор по умолчанию
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
        notifyDataSetChanged();
    }

    public void setOnMealClickListener(OnMealClickListener listener) {
        this.listener = listener;
    }

    public void setIsForUser(boolean isForUser) {
        this.isForUser = isForUser;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = meals.get(position);
        holder.bind(meal, isForUser, listener);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMealClick(meal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return meals != null ? meals.size() : 0;
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMealType;
        private final TextView tvMealName;
        private final TextView tvMealDescription;
        private final TextView tvNutrition;
        private final ImageView ivMealImage;
        private final CheckBox cbEaten;
        private final TextView tvCoachComment;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMealType = itemView.findViewById(R.id.tv_meal_type);
            tvMealName = itemView.findViewById(R.id.tv_meal_name);
            tvMealDescription = itemView.findViewById(R.id.tv_meal_description);
            tvNutrition = itemView.findViewById(R.id.tv_nutrition);
            ivMealImage = itemView.findViewById(R.id.iv_meal_image);
            cbEaten = itemView.findViewById(R.id.cb_eaten);
            tvCoachComment = itemView.findViewById(R.id.tv_coach_comment);
        }

        public void bind(Meal meal, boolean isForUser, OnMealClickListener listener) {
            tvMealType.setText(getMealTypeText(meal.getMealType()));
            tvMealName.setText(meal.getName());
            tvMealDescription.setText(meal.getDescription());
            if (isForUser) {
                cbEaten.setVisibility(View.VISIBLE);
                // Используем getEaten() вместо isEaten()
                cbEaten.setChecked(meal.getEaten() != null && meal.getEaten());
                cbEaten.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (listener != null) {
                        listener.onMarkAsEaten(meal, isChecked);
                    }
                });
            } else {
                cbEaten.setVisibility(View.GONE);
            }
            if (meal.getImageUrl() != null && !meal.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(meal.getImageUrl())
                        .placeholder(R.drawable.ic_meal_placeholder)
                        .error(R.drawable.ic_meal_placeholder) // Добавлено для ошибок
                        .into(ivMealImage);
                ivMealImage.setVisibility(View.VISIBLE);
            } else {
                ivMealImage.setVisibility(View.GONE);
            }
            if (meal.getNutrition() != null) {
                Nutrition nutrition = meal.getNutrition();
                String nutritionText = itemView.getContext().getString(R.string.nutrition_format,
                        nutrition.getCalories(),
                        nutrition.getProtein(),
                        nutrition.getCarbs(),
                        nutrition.getFat());
                tvNutrition.setText(nutritionText);
            } else {
                tvNutrition.setText(itemView.getContext().getString(R.string.meal_nutrition_placeholder));
            }

            if (meal.getImageUrl() != null && !meal.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(meal.getImageUrl())
                        .placeholder(R.drawable.ic_meal_placeholder)
                        .into(ivMealImage);
            } else {
                ivMealImage.setVisibility(View.GONE);
            }

            if (meal.getCoachComment() != null && !meal.getCoachComment().isEmpty()) {
                tvCoachComment.setVisibility(View.VISIBLE);
                String commentText = itemView.getContext().getString(R.string.meal_comment, meal.getCoachComment());
                tvCoachComment.setText(commentText);
            } else {
                tvCoachComment.setVisibility(View.GONE);
            }

            // Настройка чекбокса "Съедено"
            if (isForUser) {
                cbEaten.setVisibility(View.VISIBLE);
                // Используем getEaten() или isMealEaten()
                cbEaten.setChecked(meal.getEaten() != null && meal.getEaten());
                cbEaten.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (listener != null) {
                        listener.onMarkAsEaten(meal, isChecked);
                    }
                });
            } else {
                cbEaten.setVisibility(View.GONE);
            }
        }

        private String getMealTypeText(String mealType) {
            if (mealType == null) {
                return itemView.getContext().getString(R.string.meal_type_default);
            }

            return switch (mealType) {
                case "breakfast" -> itemView.getContext().getString(R.string.meal_breakfast);
                case "lunch" -> itemView.getContext().getString(R.string.meal_lunch);
                case "dinner" -> itemView.getContext().getString(R.string.meal_dinner);
                case "snack" -> itemView.getContext().getString(R.string.meal_snack);
                default -> itemView.getContext().getString(R.string.meal_type_default);
            };
        }
    }
}