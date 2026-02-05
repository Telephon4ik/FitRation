package com.fitration.repository;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.fitration.models.CoachRequest;
import com.fitration.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoachRequestRepository {
    private static final String TAG = "CoachRequestRepository";
    private final FirebaseFirestore firestore;

    public CoachRequestRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    // Отправить заявку тренеру (для пользователя)
    public MutableLiveData<Boolean> sendCoachRequest(CoachRequest request) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        // Проверяем, нет ли уже активной заявки
        firestore.collection("coach_requests")
                .whereEqualTo("coachId", request.getCoachId())
                .whereEqualTo("userId", request.getUserId())
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        result.setValue(false); // Уже есть активная заявка
                        Log.d(TAG, "Active request already exists");
                    } else {
                        // Сохраняем новую заявку
                        firestore.collection("coach_requests").add(request)
                                .addOnSuccessListener(documentReference -> {
                                    result.setValue(true);
                                    Log.d(TAG, "Request sent successfully");
                                })
                                .addOnFailureListener(e -> {
                                    result.setValue(false);
                                    Log.e(TAG, "Send request failed: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    result.setValue(false);
                    Log.e(TAG, "Check request failed: " + e.getMessage());
                });

        return result;
    }

    // Получить входящие заявки для тренера
    public MutableLiveData<List<CoachRequest>> getIncomingRequests(String coachId) {
        MutableLiveData<List<CoachRequest>> result = new MutableLiveData<>();

        firestore.collection("coach_requests")
                .whereEqualTo("coachId", coachId)
                .whereEqualTo("status", "PENDING")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CoachRequest> requests = new ArrayList<>();
                    for (var document : queryDocumentSnapshots) {
                        CoachRequest request = document.toObject(CoachRequest.class);
                        request.setId(document.getId());
                        requests.add(request);
                    }
                    Log.d(TAG, "Loaded " + requests.size() + " incoming requests for coach");
                    result.setValue(requests);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Get incoming requests failed: " + e.getMessage());
                    result.setValue(new ArrayList<>());
                });

        return result;
    }

    // Ответить на заявку (принять/отклонить)
    public MutableLiveData<Boolean> respondToRequest(String requestId, boolean accept) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", accept ? "ACCEPTED" : "REJECTED");

        firestore.collection("coach_requests").document(requestId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (accept) {
                        // Если принято, связываем пользователя с тренером
                        linkUserWithCoach(requestId, result);
                    } else {
                        result.setValue(true);
                        Log.d(TAG, "Request rejected");
                    }
                })
                .addOnFailureListener(e -> {
                    result.setValue(false);
                    Log.e(TAG, "Respond to request failed: " + e.getMessage());
                });

        return result;
    }

    private void linkUserWithCoach(String requestId, MutableLiveData<Boolean> result) {
        firestore.collection("coach_requests").document(requestId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        CoachRequest request = documentSnapshot.toObject(CoachRequest.class);

                        // Обновляем пользователя, устанавливаем coachId
                        Map<String, Object> userUpdate = new HashMap<>();
                        userUpdate.put("coachId", request.getCoachId());

                        firestore.collection("users").document(request.getUserId())
                                .update(userUpdate)
                                .addOnSuccessListener(aVoid -> {
                                    result.setValue(true);
                                    Log.d(TAG, "User linked with coach successfully");
                                })
                                .addOnFailureListener(e -> {
                                    result.setValue(false);
                                    Log.e(TAG, "Link user with coach failed: " + e.getMessage());
                                });
                    } else {
                        result.setValue(false);
                        Log.e(TAG, "Request not found");
                    }
                })
                .addOnFailureListener(e -> {
                    result.setValue(false);
                    Log.e(TAG, "Get request for linking failed: " + e.getMessage());
                });
    }

    // Отменить заявку (для пользователя)
    public MutableLiveData<Boolean> cancelRequest(String requestId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "CANCELLED");

        firestore.collection("coach_requests").document(requestId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    result.setValue(true);
                    Log.d(TAG, "Request cancelled successfully");
                })
                .addOnFailureListener(e -> {
                    result.setValue(false);
                    Log.e(TAG, "Cancel request failed: " + e.getMessage());
                });

        return result;
    }

    // Поиск пользователей по publicId (для тренера)
    public MutableLiveData<List<User>> searchUsersByPublicId(String publicId) {
        MutableLiveData<List<User>> result = new MutableLiveData<>();

        firestore.collection("users")
                .whereEqualTo("role", "USER")
                .whereEqualTo("publicId", publicId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (var document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        user.setUid(document.getId());
                        users.add(user);
                    }
                    result.setValue(users);
                })
                .addOnFailureListener(e -> {
                    result.setValue(new ArrayList<>());
                    Log.e(TAG, "Search users failed: " + e.getMessage());
                });

        return result;
    }
}