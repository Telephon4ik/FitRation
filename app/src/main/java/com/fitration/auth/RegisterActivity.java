package com.fitration.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.fitration.MainActivity;
import com.fitration.R;
import com.fitration.models.User;
import com.fitration.viewmodels.AuthViewModel;
import com.fitration.viewmodels.UserViewModel;

public class RegisterActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;

    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private EditText etCoachId;
    private EditText etAge;
    private EditText etWeight;
    private EditText etHeight;
    private Spinner spinnerRole;
    private Spinner spinnerActivity;
    private Spinner spinnerGoal;
    private RadioGroup rgGender;
    private Button btnRegister;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupViewModels();
        setupSpinners();
        setupListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etCoachId = findViewById(R.id.et_coach_id);
        etAge = findViewById(R.id.et_age);
        etWeight = findViewById(R.id.et_weight);
        etHeight = findViewById(R.id.et_height);
        spinnerRole = findViewById(R.id.spinner_role);
        spinnerActivity = findViewById(R.id.spinner_activity);
        spinnerGoal = findViewById(R.id.spinner_goal);
        rgGender = findViewById(R.id.rg_gender);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
    }

    private void setupViewModels() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }

    private void setupSpinners() {
        // Роли
        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(roleAdapter);

        // Уровень активности
        ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(this,
                R.array.activity_levels, android.R.layout.simple_spinner_item);
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivity.setAdapter(activityAdapter);

        // Цели
        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(this,
                R.array.goals, android.R.layout.simple_spinner_item);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(goalAdapter);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> navigateToLogin());

        spinnerRole.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // USER
                    findViewById(R.id.layout_profile_details).setVisibility(View.VISIBLE);
                    findViewById(R.id.layout_coach_id).setVisibility(View.VISIBLE);
                } else { // COACH
                    findViewById(R.id.layout_profile_details).setVisibility(View.GONE);
                    findViewById(R.id.layout_coach_id).setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String coachId = etCoachId.getText().toString().trim();
        String role = spinnerRole.getSelectedItemPosition() == 0 ? "USER" : "COACH";

        // Проверка общих полей
        if (TextUtils.isEmpty(name)) {
            etName.setError("Введите имя");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Введите email");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Введите корректный email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Введите пароль");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Пароль должен быть не менее 6 символов");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Пароли не совпадают");
            return;
        }

        if (role.equals("USER") && TextUtils.isEmpty(coachId)) {
            coachId = null; // Можно оставить пустым
        }

        // Проверка полей профиля для USER
        if (role.equals("USER")) {
            String ageStr = etAge.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();
            String heightStr = etHeight.getText().toString().trim();

            if (TextUtils.isEmpty(ageStr)) {
                etAge.setError("Введите возраст");
                return;
            }

            if (TextUtils.isEmpty(weightStr)) {
                etWeight.setError("Введите вес");
                return;
            }

            if (TextUtils.isEmpty(heightStr)) {
                etHeight.setError("Введите рост");
                return;
            }

            int selectedGenderId = rgGender.getCheckedRadioButtonId();
            if (selectedGenderId == -1) {
                Toast.makeText(this, "Выберите пол", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Регистрация...");

        // Если USER, передаем дополнительные данные
        if (role.equals("USER")) {
            int age = Integer.parseInt(etAge.getText().toString().trim());
            double weight = Double.parseDouble(etWeight.getText().toString().trim());
            double height = Double.parseDouble(etHeight.getText().toString().trim());
            String gender = ((RadioButton)findViewById(rgGender.getCheckedRadioButtonId()))
                    .getText().toString().equals("Мужской") ? "male" : "female";
            String activityLevel = getActivityLevelKey(spinnerActivity.getSelectedItemPosition());
            String goal = getGoalKey(spinnerGoal.getSelectedItemPosition());

            authViewModel.registerWithProfile(email, password, name, role, coachId,
                    gender, age, weight, height, activityLevel, goal).observe(this, user -> {
                handleRegistrationResult(user, email);
            });
        } else {
            // Для COACH используем базовую регистрацию
            authViewModel.register(email, password, name, role, coachId)
                    .observe(this, user -> {
                        handleRegistrationResult(user, email);
                    });
        }
    }

    private String getActivityLevelKey(int position) {
        switch (position) {
            case 0: return "sedentary";
            case 1: return "light";
            case 2: return "moderate";
            case 3: return "active";
            case 4: return "very_active";
            default: return "sedentary";
        }
    }

    private String getGoalKey(int position) {
        switch (position) {
            case 0: return "LOSE";
            case 1: return "MAINTAIN";
            case 2: return "GAIN";
            default: return "MAINTAIN";
        }
    }

    // Обновленный метод handleRegistrationResult
    private void handleRegistrationResult(User user, String email) {
        btnRegister.setEnabled(true);
        btnRegister.setText("Зарегистрироваться");

        if (user != null) {
            userViewModel.setCurrentUser(user);

            // Проверяем, сохранился ли пользователь в Firestore
            if (user.getPublicId() != null && !user.getPublicId().startsWith("ERROR")) {
                Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                navigateToMain();
            } else {
                // Если не сохранился в Firestore, всё равно пускаем в приложение
                Toast.makeText(this,
                        "Регистрация завершена, но есть проблемы с сохранением данных. Вы можете войти.",
                        Toast.LENGTH_LONG).show();
                navigateToMain();
            }
        } else {
            Toast.makeText(this,
                    "Ошибка регистрации. Возможно, email уже используется.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}