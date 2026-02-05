package com.fitration.ui.user.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.fitration.R;
import com.fitration.models.User;

import java.util.HashMap;
import java.util.Map;

public class EditProfileDialog extends DialogFragment {

    private EditText etName;
    private EditText etAge;
    private EditText etWeight;
    private EditText etHeight;
    private RadioGroup rgGender;
    private Spinner spinnerActivity;
    private Spinner spinnerGoal;
    private Button btnSave;
    private Button btnCancel;

    private User currentUser;
    private OnProfileSavedListener listener;

    // Флаг для защиты от множественных нажатий
    private boolean isSaving = false;

    public interface OnProfileSavedListener {
        void onProfileSaved(Map<String, Object> updates);
    }

    public EditProfileDialog(User user) {
        this.currentUser = user;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_profile, null);

        initViews(view);
        populateFields();
        setupListeners();

        builder.setView(view)
                .setTitle("Редактировать профиль");

        return builder.create();
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.et_edit_name);
        etAge = view.findViewById(R.id.et_edit_age);
        etWeight = view.findViewById(R.id.et_edit_weight);
        etHeight = view.findViewById(R.id.et_edit_height);
        rgGender = view.findViewById(R.id.rg_edit_gender);
        spinnerActivity = view.findViewById(R.id.spinner_edit_activity);
        spinnerGoal = view.findViewById(R.id.spinner_edit_goal);
        btnSave = view.findViewById(R.id.btn_save_profile);
        btnCancel = view.findViewById(R.id.btn_cancel_edit);

        setupSpinners();
    }

    private void setupSpinners() {
        // Уровень активности
        ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.activity_levels, android.R.layout.simple_spinner_item);
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivity.setAdapter(activityAdapter);

        // Цели
        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.goals, android.R.layout.simple_spinner_item);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(goalAdapter);
    }

    private void populateFields() {
        if (currentUser == null) return;

        etName.setText(currentUser.getName());
        etAge.setText(String.valueOf(currentUser.getAge()));

        // Используем точку как разделитель для веса
        String weightStr = String.valueOf(currentUser.getWeight());
        weightStr = weightStr.replace(',', '.'); // Заменяем запятую на точку
        etWeight.setText(weightStr);

        // Для роста используем целое число
        String heightStr = String.valueOf((int) currentUser.getHeight());
        etHeight.setText(heightStr);

        // Пол
        if ("male".equals(currentUser.getGender())) {
            rgGender.check(R.id.rb_edit_male);
        } else {
            rgGender.check(R.id.rb_edit_female);
        }

        // Уровень активности
        String activityKey = currentUser.getActivityLevel();
        int activityPosition = getActivityPosition(activityKey);
        spinnerActivity.setSelection(activityPosition);

        // Цель
        String goalKey = currentUser.getGoal();
        int goalPosition = getGoalPosition(goalKey);
        spinnerGoal.setSelection(goalPosition);
    }

    private int getActivityPosition(String key) {
        switch (key) {
            case "sedentary": return 0;
            case "light": return 1;
            case "moderate": return 2;
            case "active": return 3;
            case "very_active": return 4;
            default: return 0;
        }
    }

    private int getGoalPosition(String key) {
        switch (key) {
            case "LOSE": return 0;
            case "MAINTAIN": return 1;
            case "GAIN": return 2;
            default: return 1;
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveProfile());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void saveProfile() {
        // Защита от множественных нажатий
        if (isSaving) {
            return;
        }
        isSaving = true;

        // Отключаем кнопку на время сохранения
        btnSave.setEnabled(false);
        btnSave.setText("Сохранение...");

        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Введите имя");
            resetSaveButton();
            return;
        }

        if (TextUtils.isEmpty(ageStr)) {
            etAge.setError("Введите возраст");
            resetSaveButton();
            return;
        }

        if (TextUtils.isEmpty(weightStr)) {
            etWeight.setError("Введите вес");
            resetSaveButton();
            return;
        }

        if (TextUtils.isEmpty(heightStr)) {
            etHeight.setError("Введите рост");
            resetSaveButton();
            return;
        }

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(requireContext(), "Выберите пол", Toast.LENGTH_SHORT).show();
            resetSaveButton();
            return;
        }

        try {
            // Преобразуем возраст
            int age = Integer.parseInt(ageStr);

            if (age < 10 || age > 100) {
                etAge.setError("Возраст должен быть от 10 до 100 лет");
                resetSaveButton();
                return;
            }

            // Преобразуем вес, заменяя запятую на точку
            weightStr = weightStr.replace(',', '.');
            double weight = Double.parseDouble(weightStr);

            if (weight < 20 || weight > 300) {
                etWeight.setError("Вес должен быть от 20 до 300 кг");
                resetSaveButton();
                return;
            }

            // Преобразуем рост
            heightStr = heightStr.replace(',', '.');
            double height = Double.parseDouble(heightStr);

            if (height < 100 || height > 250) {
                etHeight.setError("Рост должен быть от 100 до 250 см");
                resetSaveButton();
                return;
            }

            String gender = ((RadioButton)getView().findViewById(selectedGenderId))
                    .getText().toString().equals("Мужской") ? "male" : "female";
            String activityLevel = getActivityKey(spinnerActivity.getSelectedItemPosition());
            String goal = getGoalKey(spinnerGoal.getSelectedItemPosition());

            // Создаем Map с обновленными данными
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("age", age);
            updates.put("weight", weight);
            updates.put("height", height);
            updates.put("gender", gender);
            updates.put("activityLevel", activityLevel);
            updates.put("goal", goal);

            if (listener != null) {
                listener.onProfileSaved(updates);
            }

            dismiss();
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(),
                    "Проверьте правильность ввода чисел. Используйте точку как разделитель (62.5)",
                    Toast.LENGTH_LONG).show();
            resetSaveButton();
        }
    }

    private void resetSaveButton() {
        isSaving = false;
        btnSave.setEnabled(true);
        btnSave.setText("Сохранить");
    }

    private String getActivityKey(int position) {
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

    public void setOnProfileSavedListener(OnProfileSavedListener listener) {
        this.listener = listener;
    }
}