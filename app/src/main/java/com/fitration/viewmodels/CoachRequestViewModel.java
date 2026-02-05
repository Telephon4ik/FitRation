package com.fitration.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fitration.models.CoachRequest;
import com.fitration.repository.CoachRequestRepository;

import java.util.List;

public class CoachRequestViewModel extends ViewModel {
    private CoachRequestRepository requestRepository;
    private MutableLiveData<List<CoachRequest>> incomingRequests;
    private MutableLiveData<List<CoachRequest>> sentRequests;

    public CoachRequestViewModel() {
        requestRepository = new CoachRequestRepository();
        incomingRequests = new MutableLiveData<>();
        sentRequests = new MutableLiveData<>();
    }

    // =============== ДЛЯ ПОЛЬЗОВАТЕЛЯ ===============

    public LiveData<Boolean> sendCoachRequest(CoachRequest request) {
        return requestRepository.sendCoachRequest(request);
    }

    // =============== ДЛЯ ТРЕНЕРА ===============

    // Входящие заявки для тренера (от пользователей)
    public LiveData<List<CoachRequest>> getIncomingRequests(String coachId) {
        requestRepository.getIncomingRequests(coachId).observeForever(requests -> {
            Log.d("CoachRequestViewModel", "Incoming requests for coach " + coachId +
                    ": " + (requests != null ? requests.size() : 0));
            incomingRequests.setValue(requests);
        });
        return incomingRequests;
    }

    public LiveData<Boolean> respondToRequest(String requestId, boolean accept) {
        return requestRepository.respondToRequest(requestId, accept);
    }

    // =============== ОБЩИЕ МЕТОДЫ ===============

    public LiveData<Boolean> cancelRequest(String requestId) {
        return requestRepository.cancelRequest(requestId);
    }

    public LiveData<List<com.fitration.models.User>> searchUsersByPublicId(String publicId) {
        return requestRepository.searchUsersByPublicId(publicId);
    }

    // Методы для принудительного обновления
    public void refreshIncomingRequests(String coachId) {
        requestRepository.getIncomingRequests(coachId).observeForever(requests -> {
            Log.d("CoachRequestViewModel", "Refreshed incoming requests for coach: " +
                    (requests != null ? requests.size() : 0));
            incomingRequests.setValue(requests);
        });
    }
}