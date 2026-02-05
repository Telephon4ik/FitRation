package com.fitration.utils;

import com.fitration.models.Nutrition;

public class Calculator {

    public static double calculateBMR(String gender, double weight, double height, int age) {
        if ("male".equalsIgnoreCase(gender)) {
            return 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            return 10 * weight + 6.25 * height - 5 * age - 161;
        }
    }

    public static double getActivityMultiplier(String activityLevel) {
        switch (activityLevel) {
            case "sedentary":
                return 1.2;
            case "light":
                return 1.375;
            case "moderate":
                return 1.55;
            case "active":
                return 1.725;
            case "very_active":
                return 1.9;
            default:
                return 1.2;
        }
    }

    public static double calculateTDEE(double bmr, String activityLevel) {
        return bmr * getActivityMultiplier(activityLevel);
    }

    public static int calculateDailyCalories(double tdee, String goal) {
        switch (goal) {
            case "LOSE":
                return (int) (tdee * 0.8); // -20%
            case "GAIN":
                return (int) (tdee * 1.15); // +15%
            case "MAINTAIN":
            default:
                return (int) tdee;
        }
    }

    public static Nutrition calculateNutritionGoals(int dailyCalories, String goal) {
        Nutrition nutrition = new Nutrition();
        nutrition.setCalories(dailyCalories);

        switch (goal) {
            case "LOSE":
                // Белки: 40%, Углеводы: 30%, Жиры: 30%
                nutrition.setProtein((dailyCalories * 0.4) / 4); // 4 ккал/г белка
                nutrition.setCarbs((dailyCalories * 0.3) / 4); // 4 ккал/г углеводов
                nutrition.setFat((dailyCalories * 0.3) / 9); // 9 ккал/г жира
                break;

            case "MAINTAIN":
                // Белки: 30%, Углеводы: 40%, Жиры: 30%
                nutrition.setProtein((dailyCalories * 0.3) / 4);
                nutrition.setCarbs((dailyCalories * 0.4) / 4);
                nutrition.setFat((dailyCalories * 0.3) / 9);
                break;

            case "GAIN":
                // Белки: 30%, Углеводы: 45%, Жиры: 25%
                nutrition.setProtein((dailyCalories * 0.3) / 4);
                nutrition.setCarbs((dailyCalories * 0.45) / 4);
                nutrition.setFat((dailyCalories * 0.25) / 9);
                break;
        }

        return nutrition;
    }

    public static int calculateProgress(int consumed, int target) {
        if (target == 0) return 0;
        return (int) ((double) consumed / target * 100);
    }
}