package com.fitration.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.fitration.models.Meal;
import com.fitration.models.Nutrition;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SpoonacularService {
    private static final String TAG = "SpoonacularService";
    private static final String API_KEY = "6e940b2d12f64a0896999740d8fd5a3b";
    private static final String BASE_URL = "https://api.spoonacular.com";

    private static SpoonacularService instance;
    private final OkHttpClient client;
    private final Gson gson;

    private SpoonacularService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        gson = new Gson();
    }

    public static synchronized SpoonacularService getInstance() {
        if (instance == null) {
            instance = new SpoonacularService();
        }
        return instance;
    }

    public interface MealPlanCallback {
        void onSuccess(List<Meal> meals);
        void onError(String error);
    }

    public void generateMealPlan(int targetCalories, MealPlanCallback callback) {
        String url = BASE_URL + "/mealplanner/generate?apiKey=" + API_KEY +
                "&timeFrame=day&targetCalories=" + targetCalories;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, IOException e) {
                Log.e(TAG, "Generate meal plan failed: " + e.getMessage());
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Request failed: " + response.code());
                    return;
                }

                assert response.body() != null;
                String responseBody = response.body().string();
                try {
                    JsonObject json = gson.fromJson(responseBody, JsonObject.class);
                    List<Meal> meals = parseMealPlan(json);
                    callback.onSuccess(meals);
                } catch (Exception e) {
                    Log.e(TAG, "Parse meal plan failed: " + e.getMessage());
                    callback.onError("Parse failed");
                }
            }
        });
    }

    private List<Meal> parseMealPlan(JsonObject json) {
        List<Meal> meals = new ArrayList<>();

        if (json.has("meals")) {
            JsonArray mealsArray = json.getAsJsonArray("meals");
            for (int i = 0; i < mealsArray.size(); i++) {
                JsonObject mealJson = mealsArray.get(i).getAsJsonObject();
                Meal meal = new Meal();

                meal.setId(mealJson.get("id").getAsString());
                meal.setName(mealJson.get("title").getAsString());

                // Исправленный URL для изображений
                String imageId = mealJson.get("id").getAsString();
                meal.setImageUrl("https://img.spoonacular.com/recipes/" + imageId + "-312x231.jpg");

                // Получаем информацию о питательной ценности
                if (mealJson.has("nutrition")) {
                    JsonObject nutritionJson = mealJson.getAsJsonObject("nutrition");
                    Nutrition nutrition = new Nutrition();

                    JsonArray nutrients = nutritionJson.getAsJsonArray("nutrients");
                    for (int j = 0; j < nutrients.size(); j++) {
                        JsonObject nutrient = nutrients.get(j).getAsJsonObject();
                        String name = nutrient.get("name").getAsString();
                        double amount = nutrient.get("amount").getAsDouble();

                        switch (name) {
                            case "Calories":
                                nutrition.setCalories((int) amount);
                                break;
                            case "Protein":
                                nutrition.setProtein(amount);
                                break;
                            case "Carbohydrates":
                                nutrition.setCarbs(amount);
                                break;
                            case "Fat":
                                nutrition.setFat(amount);
                                break;
                        }
                    }

                    meal.setNutrition(nutrition);
                }

                // Определяем тип приёма пищи
                String[] mealTypes = {"breakfast", "lunch", "dinner", "snack"};
                if (i < mealTypes.length) {
                    meal.setMealType(mealTypes[i]);
                } else {
                    meal.setMealType("snack");
                }

                meal.setCustom(false); // Автоматически сгенерированные блюда
                meal.setEaten(false); // По умолчанию не съедено

                meals.add(meal);
            }
        }

        return meals;
    }
}