package com.fitration.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.fitration.models.User;
import com.fitration.repository.UserRepository;
import com.fitration.utils.Calculator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserViewModel extends ViewModel {
    private UserRepository userRepository;
    private MutableLiveData<User> currentUser;
    private MutableLiveData<List<User>> clients;

    public UserViewModel() {
        userRepository = new UserRepository();
        currentUser = new MutableLiveData<>();
        clients = new MutableLiveData<>();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        currentUser.setValue(user);
    }

    public LiveData<User> loadUser(String userId) {
        return userRepository.getUser(userId);
    }

    public LiveData<Boolean> updateProfile(String userId, String name, String gender,
                                           int age, double weight, double height,
                                           String activityLevel, String goal) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        // Рассчитываем нормы
        double bmr = Calculator.calculateBMR(gender, weight, height, age);
        double tdee = Calculator.calculateTDEE(bmr, activityLevel);
        int dailyCalories = Calculator.calculateDailyCalories(tdee, goal);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("gender", gender);
        updates.put("age", age);
        updates.put("weight", weight);
        updates.put("height", height);
        updates.put("activityLevel", activityLevel);
        updates.put("goal", goal);
        updates.put("dailyCalories", dailyCalories);
        updates.put("nutritionGoals", Calculator.calculateNutritionGoals(dailyCalories, goal));

        return userRepository.updateUserProfile(userId, updates);
    }
    public LiveData<Boolean> updateProfileWithMap(String userId, Map<String, Object> updates) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        // Извлекаем данные из Map
        String name = (String) updates.get("name");
        String gender = (String) updates.get("gender");
        Integer ageObj = (Integer) updates.get("age");
        Double weightObj = (Double) updates.get("weight");
        Double heightObj = (Double) updates.get("height");
        String activityLevel = (String) updates.get("activityLevel");
        String goal = (String) updates.get("goal");

        if (ageObj == null || weightObj == null || heightObj == null) {
            result.setValue(false);
            return result;
        }

        int age = ageObj;
        double weight = weightObj;
        double height = heightObj;

        // Рассчитываем нормы
        double bmr = Calculator.calculateBMR(gender, weight, height, age);
        double tdee = Calculator.calculateTDEE(bmr, activityLevel);
        int dailyCalories = Calculator.calculateDailyCalories(tdee, goal);

        // Создаем новый Map с рассчитанными значениями
        Map<String, Object> fullUpdates = new HashMap<>();
        fullUpdates.put("name", name);
        fullUpdates.put("gender", gender);
        fullUpdates.put("age", age);
        fullUpdates.put("weight", weight);
        fullUpdates.put("height", height);
        fullUpdates.put("activityLevel", activityLevel);
        fullUpdates.put("goal", goal);
        fullUpdates.put("dailyCalories", dailyCalories);
        fullUpdates.put("nutritionGoals", Calculator.calculateNutritionGoals(dailyCalories, goal));

        return userRepository.updateUserProfile(userId, fullUpdates);
    }
    public LiveData<List<User>> getClients(String coachPublicId) {
        userRepository.getClientsForCoach(coachPublicId).observeForever(clientsList -> {
            clients.setValue(clientsList);
        });
        return clients;
    }

    public LiveData<Boolean> updateCoachId(String userId, String coachId) {
        return userRepository.updateCoachId(userId, coachId);
    }

    public void refreshClients(String coachPublicId) {
        userRepository.getClientsForCoach(coachPublicId).observeForever(clientsList -> {
            clients.setValue(clientsList);
        });
    }
}