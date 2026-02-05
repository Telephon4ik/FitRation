package com.fitration.ui.user.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fitration.R;
import com.fitration.models.Meal;
import com.fitration.models.User;
import com.fitration.ui.user.adapters.MealAdapter;
import com.fitration.viewmodels.MealViewModel;
import com.fitration.viewmodels.UserViewModel;

import java.util.List;

public class DietFragment extends Fragment {

    private MealViewModel mealViewModel;
    private UserViewModel userViewModel;

    private RecyclerView rvMeals;
    private MealAdapter mealAdapter;
    private Button btnGenerateMeals;
    private Button btnRefresh;
    private ProgressBar loadingProgress;
    private TextView tvEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diet, container, false);

        initViews(view);
        setupRecyclerView();
        setupViewModels();
        setupObservers();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        rvMeals = view.findViewById(R.id.rv_meals);
        btnGenerateMeals = view.findViewById(R.id.btn_generate_meals);
        btnRefresh = view.findViewById(R.id.btn_refresh);
        loadingProgress = view.findViewById(R.id.loading_progress);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
    }

    private void setupRecyclerView() {
        mealAdapter = new MealAdapter();
        mealAdapter.setIsForUser(true);
        rvMeals.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMeals.setAdapter(mealAdapter);

        mealAdapter.setOnMealClickListener(new MealAdapter.OnMealClickListener() {
            @Override
            public void onMealClick(Meal meal) {
                showMealDetails(meal);
            }

            @Override
            public void onMarkAsEaten(Meal meal, boolean isEaten) {
                markMealAsEaten(meal, isEaten);
            }
        });
    }

    private void setupViewModels() {
        mealViewModel = new ViewModelProvider(requireActivity()).get(MealViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
    }

    private void setupObservers() {
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                loadTodayMeals(user.getUid());
            }
        });

        mealViewModel.getTodayMeals().observe(getViewLifecycleOwner(), this::updateMealsList);
    }

    private void setupListeners() {
        btnGenerateMeals.setOnClickListener(v -> generateMealPlan());
        btnRefresh.setOnClickListener(v -> refreshMeals());
    }

    private void loadTodayMeals(String userId) {
        loadingProgress.setVisibility(View.VISIBLE);
        mealViewModel.refreshTodayMeals(userId);
    }

    private void updateMealsList(List<Meal> meals) {
        loadingProgress.setVisibility(View.GONE);

        if (meals == null || meals.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvMeals.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvMeals.setVisibility(View.VISIBLE);
            mealAdapter.setMeals(meals);
        }
    }

    private void generateMealPlan() {
        User user = userViewModel.getCurrentUser().getValue();
        if (user == null) return;

        btnGenerateMeals.setEnabled(false);
        btnGenerateMeals.setText("Генерация...");

        mealViewModel.generateDailyMealPlan(user.getUid(), user.getDailyCalories())
                .observe(getViewLifecycleOwner(), success -> {
                    btnGenerateMeals.setEnabled(true);
                    btnGenerateMeals.setText("Сгенерировать рацион");

                    if (success) {
                        Toast.makeText(requireContext(), "Рацион успешно сгенерирован!",
                                Toast.LENGTH_SHORT).show();
                        loadTodayMeals(user.getUid());
                    } else {
                        Toast.makeText(requireContext(), "Ошибка генерации рациона",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void refreshMeals() {
        User user = userViewModel.getCurrentUser().getValue();
        if (user != null) {
            loadTodayMeals(user.getUid());
        }
    }

    private void showMealDetails(Meal meal) {
        Toast.makeText(requireContext(), "Блюдо: " + meal.getName(),
                Toast.LENGTH_SHORT).show();
    }

    private void markMealAsEaten(Meal meal, boolean isEaten) {
        if (meal.getId() == null || meal.getId().isEmpty()) {
            Toast.makeText(requireContext(), "Ошибка: ID блюда не найден",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mealViewModel.markMealAsEaten(meal.getId(), isEaten)
                .observe(getViewLifecycleOwner(), success -> {
                    if (success) {
                        String message = isEaten ? "Блюдо отмечено как съеденное" : "Блюдо отмечено как не съеденное";
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                        // Обновляем прогресс на главной
                        updateHomeProgress();
                    } else {
                        Toast.makeText(requireContext(), "Ошибка обновления статуса блюда",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateHomeProgress() {
        User user = userViewModel.getCurrentUser().getValue();
        if (user != null) {
            // Обновляем питание для главной страницы
            mealViewModel.loadTodayNutrition(user.getUid());
        }
    }
}