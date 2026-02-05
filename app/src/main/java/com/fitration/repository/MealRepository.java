package com.fitration.repository;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.fitration.models.Meal;
import com.fitration.models.Nutrition;
import com.fitration.utils.SpoonacularService;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MealRepository {
    private static final String TAG = "MealRepository";
    private final FirebaseFirestore firestore;
    private final SpoonacularService spoonacularService;

    public MealRepository() {
        firestore = FirebaseFirestore.getInstance();
        spoonacularService = SpoonacularService.getInstance();
    }

    // В методе getTodayMeals() временно уберите сортировку:
    public MutableLiveData<List<Meal>> getTodayMeals(String userId) {
        MutableLiveData<List<Meal>> result = new MutableLiveData<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = calendar.getTime();

        firestore.collection("meals")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThan("date", endOfDay)
                // Временно убираем сортировку пока не создан индекс
                // .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Meal> meals = new ArrayList<>();
                    for (var document : queryDocumentSnapshots) {
                        Meal meal = document.toObject(Meal.class);
                        meal.setId(document.getId());
                        meals.add(meal);
                    }
                    // Сортируем вручную
                    meals.sort((m1, m2) -> m1.getDate().compareTo(m2.getDate()));
                    result.setValue(meals);
                })
                .addOnFailureListener(e -> {
                    result.setValue(new ArrayList<>());
                    Log.e(TAG, "Get meals failed: " + e.getMessage());
                });

        return result;
    }

    public MutableLiveData<Boolean> generateDailyMealPlan(String userId, int targetCalories) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        spoonacularService.generateMealPlan(targetCalories, new SpoonacularService.MealPlanCallback() {
            @Override
            public void onSuccess(List<Meal> meals) {
                saveGeneratedMeals(userId, meals, result);
            }

            @Override
            public void onError(String error) {
                result.setValue(false);
                Log.e(TAG, "Generate meal plan failed: " + error);
            }
        });

        return result;
    }

    private void saveGeneratedMeals(String userId, List<Meal> meals, MutableLiveData<Boolean> result) {
        List<Map<String, Object>> mealMaps = new ArrayList<>();

        for (Meal meal : meals) {
            meal.setUserId(userId);
            meal.setDate(new Date());

            Map<String, Object> mealMap = new HashMap<>();
            mealMap.put("userId", meal.getUserId());
            mealMap.put("date", meal.getDate());
            mealMap.put("mealType", meal.getMealType());
            mealMap.put("name", meal.getName());
            mealMap.put("description", meal.getDescription());
            mealMap.put("imageUrl", meal.getImageUrl());
            mealMap.put("nutrition", meal.getNutrition());
            mealMap.put("spoonacularId", meal.getSpoonacularId());
            mealMap.put("custom", meal.isCustom()); // Измените на "custom"
            mealMap.put("eaten", meal.getEaten()); // Добавьте поле eaten
            mealMap.put("eatenAt", meal.getEatenAt()); // Добавьте поле eatenAt

            mealMaps.add(mealMap);
        }

        // Удаляем старые автоматически сгенерированные блюда
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = calendar.getTime();

        firestore.collection("meals")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isCustom", false)
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThan("date", endOfDay)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Удаляем старые записи
                    for (var document : queryDocumentSnapshots) {
                        firestore.collection("meals").document(document.getId()).delete();
                    }

                    // Добавляем новые блюда
                    for (Map<String, Object> mealMap : mealMaps) {
                        firestore.collection("meals").add(mealMap);
                    }

                    result.setValue(true);
                })
                .addOnFailureListener(e -> {
                    result.setValue(false);
                    Log.e(TAG, "Save generated meals failed: " + e.getMessage());
                });
    }

    public MutableLiveData<Boolean> updateMeal(String mealId, Meal updatedMeal) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", updatedMeal.getName());
        updates.put("description", updatedMeal.getDescription());
        updates.put("nutrition", updatedMeal.getNutrition());
        updates.put("isCustom", true);
        updates.put("coachComment", updatedMeal.getCoachComment());

        firestore.collection("meals").document(mealId)
                .update(updates)
                .addOnSuccessListener(aVoid -> result.setValue(true))
                .addOnFailureListener(e -> {
                    result.setValue(false);
                    Log.e(TAG, "Update meal failed: " + e.getMessage());
                });

        return result;
    }
    public MutableLiveData<List<Meal>> getUserMeals(String userId) {
        MutableLiveData<List<Meal>> result = new MutableLiveData<>();

        firestore.collection("meals")
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(50) // Ограничиваем количество
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Meal> meals = new ArrayList<>();
                    for (var document : queryDocumentSnapshots) {
                        Meal meal = document.toObject(Meal.class);
                        meal.setId(document.getId());
                        meals.add(meal);
                    }
                    result.setValue(meals);
                })
                .addOnFailureListener(e -> {
                    result.setValue(new ArrayList<>());
                    Log.e(TAG, "Get user meals failed: " + e.getMessage());
                });

        return result;
    }
    public MutableLiveData<Nutrition> getDailyNutrition(String userId, Date date) {
        MutableLiveData<Nutrition> result = new MutableLiveData<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = calendar.getTime();

        firestore.collection("meals")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThan("date", endOfDay)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Nutrition dailyNutrition = new Nutrition(0, 0, 0, 0);

                    for (var document : queryDocumentSnapshots) {
                        Meal meal = document.toObject(Meal.class);
                        if (meal.getNutrition() != null) {
                            Nutrition mealNutrition = meal.getNutrition();
                            dailyNutrition.setCalories(dailyNutrition.getCalories() + mealNutrition.getCalories());
                            dailyNutrition.setProtein(dailyNutrition.getProtein() + mealNutrition.getProtein());
                            dailyNutrition.setCarbs(dailyNutrition.getCarbs() + mealNutrition.getCarbs());
                            dailyNutrition.setFat(dailyNutrition.getFat() + mealNutrition.getFat());
                        }
                    }

                    result.setValue(dailyNutrition);
                })
                .addOnFailureListener(e -> {
                    result.setValue(new Nutrition(0, 0, 0, 0));
                    Log.e(TAG, "Get daily nutrition failed: " + e.getMessage());
                });

        return result;
    }
    public MutableLiveData<Boolean> markMealAsEaten(String mealId, boolean eaten) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        Map<String, Object> updates = new HashMap<>();
        updates.put("eaten", eaten);
        updates.put("eatenAt", eaten ? new Date() : null);

        firestore.collection("meals").document(mealId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Meal marked as eaten: " + mealId + ", eaten: " + eaten);
                    result.setValue(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Mark meal as eaten failed: " + e.getMessage());
                    result.setValue(false);
                });

        return result;
    }
}