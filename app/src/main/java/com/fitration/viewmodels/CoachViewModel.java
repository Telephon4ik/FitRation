package com.fitration.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fitration.models.CoachRequest;
import com.fitration.models.Meal;
import com.fitration.models.User;
import com.fitration.repository.CoachRequestRepository;
import com.fitration.repository.MealRepository;
import com.fitration.repository.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoachViewModel extends ViewModel {
    private UserRepository userRepository;
    private MealRepository mealRepository;
    private CoachRequestRepository coachRequestRepository;

    private MutableLiveData<List<User>> clients;
    private MutableLiveData<List<Meal>> clientMeals;
    private MutableLiveData<List<User>> searchResults;

    public CoachViewModel() {
        userRepository = new UserRepository();
        mealRepository = new MealRepository();
        coachRequestRepository = new CoachRequestRepository();

        clients = new MutableLiveData<>();
        clientMeals = new MutableLiveData<>();
        searchResults = new MutableLiveData<>();
    }

    public LiveData<List<User>> getClients(String coachPublicId) {
        return userRepository.getClientsForCoach(coachPublicId);
    }
    public LiveData<List<Meal>> getClientMeals(String userId) {
        return mealRepository.getUserMeals(userId);
    }
    public LiveData<Boolean> updateMealWithComment(String mealId, String comment) {
        Meal meal = new Meal();
        meal.setCoachComment(comment);
        return mealRepository.updateMeal(mealId, meal);
    }
    public LiveData<Boolean> addCommentToMeal(String mealId, String comment) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        Map<String, Object> updates = new HashMap<>();
        updates.put("coachComment", comment);

        FirebaseFirestore.getInstance().collection("meals")
                .document(mealId)
                .update(updates)
                .addOnSuccessListener(aVoid -> result.setValue(true))
                .addOnFailureListener(e -> {
                    Log.e("CoachViewModel", "Add comment failed: " + e.getMessage());
                    result.setValue(false);
                });

        return result;
    }
    public LiveData<Boolean> updateClientMeal(String mealId, Meal updatedMeal) {
        return mealRepository.updateMeal(mealId, updatedMeal);
    }

    public void refreshClients(String coachPublicId) {
        userRepository.getClientsForCoach(coachPublicId).observeForever(clientsList -> {
            clients.setValue(clientsList);
        });
    }

    public void searchUsers(String publicId) {
        coachRequestRepository.searchUsersByPublicId(publicId).observeForever(users -> {
            searchResults.setValue(users);
        });
    }

    public LiveData<List<User>> getSearchResults() {
        return searchResults;
    }
}