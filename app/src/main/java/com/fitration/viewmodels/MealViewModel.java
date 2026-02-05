package com.fitration.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.fitration.models.Meal;
import com.fitration.models.Nutrition;
import com.fitration.repository.MealRepository;

import java.util.Date;
import java.util.List;

public class MealViewModel extends ViewModel {
    private MealRepository mealRepository;
    private MutableLiveData<List<Meal>> todayMeals;
    private MutableLiveData<Nutrition> todayNutrition;

    public MealViewModel() {
        mealRepository = new MealRepository();
        todayMeals = new MutableLiveData<>();
        todayNutrition = new MutableLiveData<>();
    }

    public LiveData<List<Meal>> getTodayMeals() {
        return todayMeals;
    }

    public LiveData<Boolean> generateDailyMealPlan(String userId, int targetCalories) {
        return mealRepository.generateDailyMealPlan(userId, targetCalories);
    }

    public LiveData<Boolean> updateMeal(String mealId, Meal updatedMeal) {
        return mealRepository.updateMeal(mealId, updatedMeal);
    }

    public LiveData<Nutrition> getDailyNutrition(String userId, Date date) {
        return mealRepository.getDailyNutrition(userId, date);
    }

    public void refreshTodayMeals(String userId) {
        mealRepository.getTodayMeals(userId).observeForever(meals -> {
            todayMeals.setValue(meals);
            // После загрузки блюд обновляем питание
            if (userId != null) {
                loadTodayNutrition(userId);
            }
        });
    }

    public void loadTodayNutrition(String userId) {
        mealRepository.getDailyNutrition(userId, new Date()).observeForever(nutrition -> {
            todayNutrition.setValue(nutrition);
        });
    }

    public LiveData<Boolean> markMealAsEaten(String mealId, boolean isEaten) {
        return mealRepository.markMealAsEaten(mealId, isEaten);
    }

    public LiveData<Nutrition> getTodayNutrition() {
        return todayNutrition;
    }
}