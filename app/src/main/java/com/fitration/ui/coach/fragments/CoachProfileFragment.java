package com.fitration.ui.coach.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fitration.R;
import com.fitration.auth.LoginActivity;
import com.fitration.models.User;
import com.fitration.viewmodels.AuthViewModel;
import com.fitration.viewmodels.UserViewModel;

public class CoachProfileFragment extends Fragment {

    private UserViewModel userViewModel;
    private AuthViewModel authViewModel;

    private TextView tvCoachName;
    private TextView tvCoachEmail;
    private TextView tvCoachPublicId;
    private TextView tvClientCount;
    private TextView tvAvgProgress;
    private Button btnLogoutCoach;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coach_profile, container, false);

        initViews(view);
        setupViewModels();
        setupObservers();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        tvCoachName = view.findViewById(R.id.tv_coach_name);
        tvCoachEmail = view.findViewById(R.id.tv_coach_email);
        tvCoachPublicId = view.findViewById(R.id.tv_coach_public_id);
        tvClientCount = view.findViewById(R.id.tv_client_count);
        tvAvgProgress = view.findViewById(R.id.tv_avg_progress);
        btnLogoutCoach = view.findViewById(R.id.btn_logout_coach);
    }

    private void setupViewModels() {
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
    }

    private void setupObservers() {
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), this::updateProfile);
    }

    private void setupListeners() {
        btnLogoutCoach.setOnClickListener(v -> logout());
    }

    private void updateProfile(User user) {
        if (user == null) return;

        tvCoachName.setText(user.getName());
        tvCoachEmail.setText(user.getEmail());
        tvCoachPublicId.setText("Ваш ID: " + user.getPublicId());

        // В реальном приложении нужно загрузить статистику тренера
        tvClientCount.setText("0");
        tvAvgProgress.setText("0%");
    }

    private void logout() {
        authViewModel.logout();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}