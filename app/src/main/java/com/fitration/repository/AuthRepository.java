package com.fitration.repository;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.fitration.models.User;
import com.fitration.utils.Calculator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    public MutableLiveData<User> login(String email, String password) {
        MutableLiveData<User> result = new MutableLiveData<>();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            getUserFromFirestore(firebaseUser.getUid(), result);
                        } else {
                            result.setValue(null);
                        }
                    } else {
                        result.setValue(null);
                        Log.e(TAG, "Login failed: " + task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    result.setValue(null);
                    Log.e(TAG, "Login failed with exception: " + e.getMessage());
                });

        return result;
    }

    public MutableLiveData<User> register(String email, String password, String name, String role, String coachId) {
        MutableLiveData<User> result = new MutableLiveData<>();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            User user = new User();
                            user.setUid(firebaseUser.getUid());
                            user.setEmail(email);
                            user.setName(name);
                            user.setRole(role);
                            user.setCoachId(coachId);
                            user.setPublicId(generatePublicId());
                            user.setCreatedAt(new Date());

                            // Устанавливаем дефолтные значения
                            if ("USER".equals(role)) {
                                user.setGender("male");
                                user.setAge(25);
                                user.setWeight(70.0);
                                user.setHeight(175.0);
                                user.setActivityLevel("moderate");
                                user.setGoal("MAINTAIN");
                                user.setDailyCalories(2000);
                                user.setNutritionGoals(Calculator.calculateNutritionGoals(2000, "MAINTAIN"));
                            }

                            saveUserToFirestore(user, result);
                        } else {
                            result.setValue(null);
                        }
                    } else {
                        result.setValue(null);
                        Log.e(TAG, "Registration failed: " + task.getException());
                    }
                })
                .addOnFailureListener(e -> {
                    result.setValue(null);
                    Log.e(TAG, "Registration failed with exception: " + e.getMessage());
                });

        return result;
    }

    public MutableLiveData<User> registerWithProfile(String email, String password, String name,
                                                     String role, String coachId, String gender, int age,
                                                     double weight, double height, String activityLevel,
                                                     String goal) {
        MutableLiveData<User> result = new MutableLiveData<>();

        // Сначала проверяем, существует ли пользователь в Firestore по email
        checkUserExistsByEmail(email, new UserExistsCallback() {
            @Override
            public void onExists(boolean exists) {
                if (exists) {
                    // Пользователь уже существует в Firestore
                    result.setValue(null);
                    Log.e(TAG, "User already exists in Firestore with email: " + email);
                    return;
                }

                // Если не существует в Firestore, пробуем создать в Auth
                createUserWithProfile(email, password, name, role, coachId, gender, age,
                        weight, height, activityLevel, goal, result);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Check user exists error: " + error);
                // В случае ошибки проверки, всё равно пробуем создать пользователя
                createUserWithProfile(email, password, name, role, coachId, gender, age,
                        weight, height, activityLevel, goal, result);
            }
        });

        return result;
    }
    private interface UserExistsCallback {
        void onExists(boolean exists);
        void onError(String error);
    }
    private void checkUserExistsByEmail(String email, UserExistsCallback callback) {
        firestore.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onExists(!queryDocumentSnapshots.isEmpty());
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    private void getUserFromFirestore(String uid, MutableLiveData<User> result) {
        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        result.setValue(user);
                    } else {
                        // Если пользователя нет в Firestore, создаем нового
                        createDefaultUser(uid, result);
                    }
                })
                .addOnFailureListener(e -> {
                    result.setValue(null);
                    Log.e(TAG, "Get user failed: " + e.getMessage());
                });
    }
    private void createUserWithProfile(String email, String password, String name, String role,
                                       String coachId, String gender, int age, double weight,
                                       double height, String activityLevel, String goal,
                                       MutableLiveData<User> result) {

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            User user = createUserObject(firebaseUser.getUid(), email, name, role,
                                    coachId, gender, age, weight, height,
                                    activityLevel, goal);
                            saveUserToFirestore(user, result);
                        } else {
                            result.setValue(null);
                        }
                    } else {
                        // Обработка ошибки создания пользователя
                        handleRegistrationError(task.getException(), email, result);
                    }
                })
                .addOnFailureListener(e -> {
                    result.setValue(null);
                    Log.e(TAG, "Registration with profile failed with exception: " + e.getMessage());
                });
    }
    private User createUserObject(String uid, String email, String name, String role, String coachId,
                                  String gender, int age, double weight, double height,
                                  String activityLevel, String goal) {

        User user = new User();
        user.setUid(uid);
        user.setEmail(email);
        user.setName(name);
        user.setRole(role);
        user.setCoachId(coachId);
        user.setPublicId(generatePublicId());
        user.setCreatedAt(new Date());

        // Устанавливаем данные профиля
        user.setGender(gender);
        user.setAge(age);
        user.setWeight(weight);
        user.setHeight(height);
        user.setActivityLevel(activityLevel);
        user.setGoal(goal);

        // Рассчитываем КБЖУ
        double bmr = Calculator.calculateBMR(gender, weight, height, age);
        double tdee = Calculator.calculateTDEE(bmr, activityLevel);
        int dailyCalories = Calculator.calculateDailyCalories(tdee, goal);

        user.setDailyCalories(dailyCalories);
        user.setNutritionGoals(Calculator.calculateNutritionGoals(dailyCalories, goal));

        return user;
    }
    private interface GetUserCallback {
        void onUserFound(User user);
        void onError(String error);
    }

    private void getUserByEmail(String email, GetUserCallback callback) {
        firestore.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                        callback.onUserFound(user);
                    } else {
                        callback.onUserFound(null);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    private void createFirestoreUserForExistingAuth(String email, MutableLiveData<User> result) {
        // Получаем текущего пользователя из Auth
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null && firebaseUser.getEmail() != null &&
                firebaseUser.getEmail().equals(email)) {

            // Создаём базовую запись пользователя
            User user = new User();
            user.setUid(firebaseUser.getUid());
            user.setEmail(email);
            user.setName(firebaseUser.getDisplayName() != null ?
                    firebaseUser.getDisplayName() : "User");
            user.setRole("USER");
            user.setPublicId(generatePublicId());
            user.setCreatedAt(new Date());

            // Устанавливаем дефолтные значения
            user.setGender("male");
            user.setAge(25);
            user.setWeight(70.0);
            user.setHeight(175.0);
            user.setActivityLevel("moderate");
            user.setGoal("MAINTAIN");
            user.setDailyCalories(2000);
            user.setNutritionGoals(Calculator.calculateNutritionGoals(2000, "MAINTAIN"));

            saveUserToFirestore(user, result);
            Log.i(TAG, "Created Firestore record for existing Auth user");
        } else {
            // Пользователь не авторизован с этим email
            result.setValue(null);
            Log.e(TAG, "User not authenticated with email: " + email);
        }
    }

    // Улучшенный метод saveUserToFirestore
    private void saveUserToFirestore(User user, MutableLiveData<User> result) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("email", user.getEmail());
        userData.put("name", user.getName());
        userData.put("role", user.getRole());
        userData.put("publicId", user.getPublicId());
        userData.put("coachId", user.getCoachId());
        userData.put("createdAt", user.getCreatedAt());

        // Только для пользователей добавляем профиль
        if ("USER".equals(user.getRole())) {
            userData.put("gender", user.getGender());
            userData.put("age", user.getAge());
            userData.put("weight", user.getWeight());
            userData.put("height", user.getHeight());
            userData.put("activityLevel", user.getActivityLevel());
            userData.put("goal", user.getGoal());
            userData.put("dailyCalories", user.getDailyCalories());
            userData.put("nutritionGoals", user.getNutritionGoals());
        }

        userData.put("acceptCoachRequests", true);

        firestore.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.i(TAG, "User saved to Firestore: " + user.getEmail());
                    result.setValue(user);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Save user failed: " + e.getMessage());
                    // Все равно возвращаем пользователя, но показываем ошибку
                    user.setPublicId("ERROR_SAVING");
                    result.setValue(user);
                });
    }
    private void handleRegistrationError(Exception exception, String email,
                                         MutableLiveData<User> result) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            // Email уже используется - пробуем войти
            Log.w(TAG, "Email already in use, trying to login: " + email);

            // Проверяем, есть ли пользователь в Firestore
            getUserByEmail(email, new GetUserCallback() {
                @Override
                public void onUserFound(User user) {
                    if (user != null) {
                        // Пользователь найден в Firestore
                        result.setValue(user);
                        Log.i(TAG, "Existing user found in Firestore, returning it");
                    } else {
                        // Пользователь есть в Auth, но нет в Firestore - создаём запись
                        createFirestoreUserForExistingAuth(email, result);
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Get user by email error: " + error);
                    result.setValue(null);
                }
            });
        } else {
            result.setValue(null);
            Log.e(TAG, "Registration failed: " + exception);
        }
    }
    private void createDefaultUser(String uid, MutableLiveData<User> result) {
        User user = new User();
        user.setUid(uid);
        user.setPublicId(generatePublicId());
        user.setRole("USER");
        user.setCreatedAt(new Date());

        saveUserToFirestore(user, result);
    }

    private String generatePublicId() {
        return "FR-" + String.format("%06d", (int)(Math.random() * 1000000));
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
}