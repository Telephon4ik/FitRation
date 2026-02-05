package com.fitration.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.fitration.models.User;
import com.fitration.repository.AuthRepository;
import com.fitration.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {
    private AuthRepository authRepository;
    private UserRepository userRepository;
    private MutableLiveData<User> currentUser;

    public AuthViewModel() {
        authRepository = new AuthRepository();
        userRepository = new UserRepository();
        currentUser = new MutableLiveData<>();
    }

    public LiveData<User> login(String email, String password) {
        MutableLiveData<User> result = new MutableLiveData<>();

        authRepository.login(email, password).observeForever(user -> {
            if (user != null) {
                currentUser.setValue(user);
                result.setValue(user);
            } else {
                result.setValue(null);
            }
        });

        return result;
    }

    public LiveData<User> register(String email, String password, String name, String role, String coachId) {
        MutableLiveData<User> result = new MutableLiveData<>();

        authRepository.register(email, password, name, role, coachId).observeForever(user -> {
            if (user != null) {
                currentUser.setValue(user);
                result.setValue(user);
            } else {
                result.setValue(null);
            }
        });

        return result;
    }

    public void logout() {
        authRepository.logout();
        currentUser.setValue(null);
    }

    public LiveData<User> registerWithProfile(String email, String password, String name,
                                              String role, String coachId, String gender, int age, double weight, double height,
                                              String activityLevel, String goal) {
        return authRepository.registerWithProfile(email, password, name, role, coachId,
                gender, age, weight, height, activityLevel, goal);
    }
    public FirebaseUser getCurrentUserSync() {
        return authRepository.getCurrentUser();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        currentUser.setValue(user);
    }
}