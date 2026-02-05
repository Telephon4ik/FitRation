package com.fitration.ui.user.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fitration.R;
import com.fitration.auth.LoginActivity;
import com.fitration.models.CoachRequest;
import com.fitration.models.User;
import com.fitration.ui.user.dialogs.EditProfileDialog;
import com.fitration.ui.user.dialogs.SendCoachRequestDialog;
import com.fitration.viewmodels.AuthViewModel;
import com.fitration.viewmodels.CoachRequestViewModel;
import com.fitration.viewmodels.UserViewModel;

import java.util.Map;

public class ProfileFragment extends Fragment {

    private UserViewModel userViewModel;
    private AuthViewModel authViewModel;
    private CoachRequestViewModel requestViewModel;

    // UI элементы профиля
    private TextView tvUserName, tvUserEmail, tvUserPublicId, tvUserRole,
            tvUserCoachId, tvUserGoal, tvUserCalories, tvUserNutrition;

    // Основные кнопки
    private Button btnEditProfile, btnLogout, btnSendCoachRequest;

    // Ссылки на View для секции заявок
    private View requestsCard;

    // Флаги
    private boolean isDialogShowing = false;
    private boolean isProcessingRequest = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViewModels();
        setupObservers();
        setupListeners();
    }

    private void initViews(View view) {
        // Профиль пользователя
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvUserPublicId = view.findViewById(R.id.tv_user_public_id);
        tvUserRole = view.findViewById(R.id.tv_user_role);
        tvUserCoachId = view.findViewById(R.id.tv_user_coach_id);
        tvUserGoal = view.findViewById(R.id.tv_user_goal);
        tvUserCalories = view.findViewById(R.id.tv_user_calories);
        tvUserNutrition = view.findViewById(R.id.tv_user_nutrition);

        // Основные кнопки
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnSendCoachRequest = view.findViewById(R.id.btn_send_coach_request);

        // Секция заявок
        requestsCard = view.findViewById(R.id.card_requests);

        // Скрываем секцию заявок у пользователя
        hideUserRequestsSection();
    }

    private void setupViewModels() {
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        requestViewModel = new ViewModelProvider(requireActivity()).get(CoachRequestViewModel.class);
    }

    private void setupObservers() {
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                updateProfile(user);
            }
        });
    }

    private void setupListeners() {
        // Редактирование профиля
        btnEditProfile.setOnClickListener(v -> {
            if (!isDialogShowing) {
                showEditProfileDialog();
            }
        });

        // Выход
        btnLogout.setOnClickListener(v -> logout());

        // Отправка заявки тренеру
        btnSendCoachRequest.setOnClickListener(v -> {
            if (!isDialogShowing) {
                showSendRequestDialog();
            }
        });
    }

    private void hideUserRequestsSection() {
        if (requestsCard != null) {
            requestsCard.setVisibility(View.GONE);
        }
    }

    private void updateProfile(User user) {
        if (getView() == null || user == null) {
            return;
        }

        // Обновляем все поля профиля
        tvUserName.setText(user.getName());
        tvUserEmail.setText(user.getEmail());
        tvUserPublicId.setText(getString(R.string.profile_id, user.getPublicId()));

        String roleText = user.isCoach() ?
                getString(R.string.role_coach) :
                getString(R.string.role_user);
        tvUserRole.setText(getString(R.string.profile_role, roleText));

        if (user.getCoachId() != null && !user.getCoachId().isEmpty()) {
            tvUserCoachId.setText(getString(R.string.profile_coach, user.getCoachId()));
            tvUserCoachId.setVisibility(View.VISIBLE);
        } else {
            tvUserCoachId.setVisibility(View.GONE);
        }

        String goalText = getGoalText(user.getGoal());
        tvUserGoal.setText(getString(R.string.profile_goal, goalText));
        tvUserCalories.setText(getString(R.string.profile_calories, user.getDailyCalories()));

        if (user.getNutritionGoals() != null) {
            String nutritionText = getString(R.string.profile_nutrition,
                    user.getNutritionGoals().getProtein(),
                    user.getNutritionGoals().getCarbs(),
                    user.getNutritionGoals().getFat());
            tvUserNutrition.setText(nutritionText);
        } else {
            tvUserNutrition.setText("");
        }
    }

    private String getGoalText(String goal) {
        if (goal == null) return getString(R.string.goal_unknown);

        switch (goal) {
            case "LOSE": return getString(R.string.goal_lose);
            case "MAINTAIN": return getString(R.string.goal_maintain);
            case "GAIN": return getString(R.string.goal_gain);
            default: return getString(R.string.goal_unknown);
        }
    }

    private void showEditProfileDialog() {
        if (getActivity() == null || getActivity().isFinishing() ||
                getParentFragmentManager().isStateSaved()) {
            return;
        }

        User user = userViewModel.getCurrentUser().getValue();
        if (user == null) return;

        // Проверяем, не открыт ли уже диалог
        Fragment existingDialog = getParentFragmentManager()
                .findFragmentByTag("edit_profile_dialog");
        if (existingDialog != null && existingDialog.isAdded()) {
            return;
        }

        EditProfileDialog dialog = new EditProfileDialog(user);
        dialog.setOnProfileSavedListener(updates ->
                saveProfileChanges(user.getUid(), updates));

        dialog.show(getParentFragmentManager(), "edit_profile_dialog");
    }

    private void showSendRequestDialog() {
        if (getActivity() == null || getActivity().isFinishing() ||
                getParentFragmentManager().isStateSaved()) {
            return;
        }

        Fragment existingDialog = getParentFragmentManager()
                .findFragmentByTag("send_coach_request");
        if (existingDialog != null && existingDialog.isAdded()) {
            return;
        }

        SendCoachRequestDialog dialog = new SendCoachRequestDialog(null, null);
        dialog.setOnRequestSentListener(this::sendCoachRequest);
        dialog.show(getParentFragmentManager(), "send_coach_request");
    }

    private void sendCoachRequest(String coachId, String message) {
        if (isProcessingRequest) {
            return;
        }

        User user = userViewModel.getCurrentUser().getValue();
        if (user == null) {
            return;
        }

        // Проверяем, не является ли пользователь уже привязанным к тренеру
        if (user.getCoachId() != null && !user.getCoachId().isEmpty()) {
            Toast.makeText(requireContext(),
                    "Вы уже привязаны к тренеру " + user.getCoachId(),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        CoachRequest request = new CoachRequest();
        request.setCoachId(coachId);
        request.setUserId(user.getUid());
        request.setUserName(user.getName());
        request.setUserEmail(user.getEmail());
        request.setMessage(message);

        isProcessingRequest = true;

        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Отправка заявки...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        requestViewModel.sendCoachRequest(request).observe(getViewLifecycleOwner(), success -> {
            progressDialog.dismiss();
            isProcessingRequest = false;

            if (success) {
                Toast.makeText(requireContext(),
                        "Заявка отправлена тренеру " + coachId,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(),
                        "Ошибка отправки заявки или заявка уже существует",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileChanges(String userId, Map<String, Object> updates) {
        if (isProcessingRequest) {
            return;
        }

        isProcessingRequest = true;

        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Сохранение изменений...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Извлекаем данные из Map
        String name = (String) updates.get("name");
        String gender = (String) updates.get("gender");
        Integer age = (Integer) updates.get("age");
        Double weight = (Double) updates.get("weight");
        Double height = (Double) updates.get("height");
        String activityLevel = (String) updates.get("activityLevel");
        String goal = (String) updates.get("goal");

        if (name == null || gender == null || age == null ||
                weight == null || height == null || activityLevel == null || goal == null) {
            progressDialog.dismiss();
            isProcessingRequest = false;
            Toast.makeText(requireContext(), "Ошибка: некорректные данные", Toast.LENGTH_SHORT).show();
            return;
        }

        userViewModel.updateProfile(userId, name, gender, age, weight, height, activityLevel, goal)
                .observe(getViewLifecycleOwner(), success -> {
                    progressDialog.dismiss();
                    isProcessingRequest = false;

                    if (success) {
                        Toast.makeText(requireContext(), "Профиль обновлен", Toast.LENGTH_SHORT).show();
                        // Обновляем данные пользователя
                        userViewModel.loadUser(userId).observe(getViewLifecycleOwner(),
                                updatedUser -> {
                                    if (updatedUser != null) {
                                        userViewModel.setCurrentUser(updatedUser);
                                    }
                                });
                    } else {
                        Toast.makeText(requireContext(), "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logout() {
        if (isProcessingRequest) {
            return;
        }

        isProcessingRequest = true;
        authViewModel.logout();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Очищаем ссылки на View чтобы избежать утечек памяти
        tvUserName = null;
        tvUserEmail = null;
        tvUserPublicId = null;
        tvUserRole = null;
        tvUserCoachId = null;
        tvUserGoal = null;
        tvUserCalories = null;
        tvUserNutrition = null;

        btnEditProfile = null;
        btnLogout = null;
        btnSendCoachRequest = null;
        requestsCard = null;

        // Сбрасываем флаги
        isDialogShowing = false;
        isProcessingRequest = false;
    }
}