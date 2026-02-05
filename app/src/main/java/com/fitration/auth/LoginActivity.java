package com.fitration.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.fitration.MainActivity;
import com.fitration.R;
import com.fitration.models.User;
import com.fitration.viewmodels.AuthViewModel;
import com.fitration.viewmodels.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupViewModels();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
    }

    private void setupViewModels() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegister.setOnClickListener(v -> navigateToRegister());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Введите email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Введите пароль");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Вход...");

        authViewModel.login(email, password).observe(this, user -> {
            btnLogin.setEnabled(true);
            btnLogin.setText("Войти");

            if (user != null) {
                userViewModel.setCurrentUser(user);
                navigateToMain();
            } else {
                // Проверяем, есть ли пользователь в Auth
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Пользователь есть в Auth, но нет в Firestore
                                // Создаём запись в Firestore
                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (firebaseUser != null) {
                                    createUserInFirestore(firebaseUser);
                                }
                            } else {
                                Toast.makeText(this,
                                        "Ошибка входа. Проверьте email и пароль.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
    private void createUserInFirestore(FirebaseUser firebaseUser) {
        User user = new User();
        user.setUid(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
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

        // Сохраняем в Firestore
        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    userViewModel.setCurrentUser(user);
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    // Все равно пускаем в приложение
                    Toast.makeText(this,
                            "Вход выполнен, но есть проблемы с данными профиля",
                            Toast.LENGTH_LONG).show();
                    userViewModel.setCurrentUser(user);
                    navigateToMain();
                });
    }
    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
    private String generatePublicId() {
        return "FR-" + String.format("%06d", (int)(Math.random() * 1000000));
    }
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}