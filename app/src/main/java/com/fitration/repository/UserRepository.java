package com.fitration.repository;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.fitration.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private final FirebaseFirestore firestore;

    public UserRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public MutableLiveData<User> getUser(String userId) {
        MutableLiveData<User> result = new MutableLiveData<>();

        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        result.setValue(user);
                    } else {
                        result.setValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    result.setValue(null);
                    Log.e(TAG, "Get user failed: " + e.getMessage());
                });

        return result;
    }

    public MutableLiveData<Boolean> updateUserProfile(String userId, Map<String, Object> updates) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        firestore.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> result.setValue(true))
                .addOnFailureListener(e -> {
                    result.setValue(false);
                    Log.e(TAG, "Update user failed: " + e.getMessage());
                });

        return result;
    }

    public MutableLiveData<List<User>> getClientsForCoach(String coachPublicId) {
        MutableLiveData<List<User>> result = new MutableLiveData<>();

        firestore.collection("users")
                .whereEqualTo("role", "USER")
                .whereEqualTo("coachId", coachPublicId)  // Используем publicId тренера
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> clients = new ArrayList<>();
                    for (var document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        user.setUid(document.getId());
                        clients.add(user);
                    }
                    result.setValue(clients);
                })
                .addOnFailureListener(e -> {
                    result.setValue(new ArrayList<>());
                    Log.e(TAG, "Get clients failed: " + e.getMessage());
                });

        return result;
    }

    public MutableLiveData<Boolean> updateCoachId(String userId, String coachId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        Map<String, Object> updates = new HashMap<>();
        updates.put("coachId", coachId);
        updates.put("updatedAt", new Date());

        firestore.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> result.setValue(true))
                .addOnFailureListener(e -> {
                    result.setValue(false);
                    Log.e(TAG, "Update coachId failed: " + e.getMessage());
                });

        return result;
    }
}