package com.fitration.ui.coach.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitration.R;
import com.fitration.models.CoachRequest;
import com.fitration.models.User;
import com.fitration.ui.coach.adapters.ClientAdapter;
import com.fitration.ui.coach.adapters.CoachRequestsAdapter;
import com.fitration.viewmodels.CoachRequestViewModel;
import com.fitration.viewmodels.CoachViewModel;
import com.fitration.viewmodels.UserViewModel;

import java.util.List;

public class ClientsFragment extends Fragment {

    private CoachViewModel coachViewModel;
    private UserViewModel userViewModel;
    private CoachRequestViewModel requestViewModel;

    private RecyclerView rvClients;
    private RecyclerView rvIncomingRequests;
    private ClientAdapter clientAdapter;
    private CoachRequestsAdapter requestsAdapter;
    private TextView tvEmptyClients;
    private TextView tvEmptyRequests;
    private Button btnToggleView;

    private boolean showingClients = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_clients, container, false);

        initViews(view);
        setupRecyclerViews();
        setupViewModels();
        setupObservers();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        rvClients = view.findViewById(R.id.rv_clients);
        rvIncomingRequests = view.findViewById(R.id.rv_incoming_requests);
        tvEmptyClients = view.findViewById(R.id.tv_empty_clients);
        tvEmptyRequests = view.findViewById(R.id.tv_empty_incoming_requests);
        btnToggleView = view.findViewById(R.id.btn_view_incoming_requests);

        // Изначально показываем клиентов
        showClientsView();
    }

    private void setupRecyclerViews() {
        // Адаптер для клиентов
        clientAdapter = new ClientAdapter();
        clientAdapter.setOnClientClickListener(new ClientAdapter.OnClientClickListener() {
            @Override
            public void onClientClick(User client) {
                // Обработка клика на клиента (можно открыть детали)
                Toast.makeText(requireContext(), "Клиент: " + client.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onViewStatsClick(User client) {
                // Обработка клика на статистику
                showClientStats(client);
            }
        });
        rvClients.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvClients.setAdapter(clientAdapter);

        // Адаптер для входящих заявок
        requestsAdapter = new CoachRequestsAdapter();
        requestsAdapter.setOnRequestActionListener(new CoachRequestsAdapter.OnRequestActionListener() {
            @Override
            public void onAcceptRequest(CoachRequest request) {
                respondToRequest(request.getId(), true);
            }

            @Override
            public void onRejectRequest(CoachRequest request) {
                respondToRequest(request.getId(), false);
            }
        });
        rvIncomingRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvIncomingRequests.setAdapter(requestsAdapter);
    }

    private void setupViewModels() {
        coachViewModel = new ViewModelProvider(requireActivity()).get(CoachViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        requestViewModel = new ViewModelProvider(requireActivity()).get(CoachRequestViewModel.class);
    }

    private void setupObservers() {
        // Наблюдаем за текущим пользователем
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.isCoach()) {
                String coachPublicId = user.getPublicId();
                Log.d("ClientsFragment", "Coach public ID: " + coachPublicId);

                // Загружаем клиентов
                coachViewModel.getClients(coachPublicId).observe(getViewLifecycleOwner(),
                        this::updateClientsList);

                // Загружаем входящие заявки
                requestViewModel.getIncomingRequests(coachPublicId).observe(getViewLifecycleOwner(),
                        this::updateRequestsList);
            }
        });
    }

    private void setupListeners() {
        btnToggleView.setOnClickListener(v -> {
            if (showingClients) {
                showRequestsView();
                // Обновляем заявки при переключении
                User user = userViewModel.getCurrentUser().getValue();
                if (user != null) {
                    requestViewModel.refreshIncomingRequests(user.getPublicId());
                }
            } else {
                showClientsView();
            }
        });
    }

    private void updateClientsList(List<User> clients) {
        Log.d("ClientsFragment", "updateClientsList called with " +
                (clients != null ? clients.size() : 0) + " clients");

        if (clients == null || clients.isEmpty()) {
            tvEmptyClients.setVisibility(View.VISIBLE);
            rvClients.setVisibility(View.GONE);
            Log.d("ClientsFragment", "No clients to display, showing empty state");
        } else {
            tvEmptyClients.setVisibility(View.GONE);
            rvClients.setVisibility(View.VISIBLE);
            clientAdapter.setClients(clients);
            Log.d("ClientsFragment", clients.size() + " clients set to adapter");
        }
    }

    private void updateRequestsList(List<CoachRequest> requests) {
        Log.d("ClientsFragment", "updateRequestsList called with " +
                (requests != null ? requests.size() : 0) + " requests");

        if (requests == null || requests.isEmpty()) {
            tvEmptyRequests.setVisibility(View.VISIBLE);
            rvIncomingRequests.setVisibility(View.GONE);
            Log.d("ClientsFragment", "No requests to display");
        } else {
            tvEmptyRequests.setVisibility(View.GONE);
            rvIncomingRequests.setVisibility(View.VISIBLE);
            requestsAdapter.setRequests(requests);
            Log.d("ClientsFragment", requests.size() + " requests set to adapter");
        }
    }

    private void showClientsView() {
        showingClients = true;
        rvClients.setVisibility(View.VISIBLE);
        rvIncomingRequests.setVisibility(View.GONE);
        tvEmptyClients.setVisibility(View.VISIBLE);
        tvEmptyRequests.setVisibility(View.GONE);
        btnToggleView.setText("Показать заявки");
    }

    private void showRequestsView() {
        showingClients = false;
        rvClients.setVisibility(View.GONE);
        rvIncomingRequests.setVisibility(View.VISIBLE);
        tvEmptyClients.setVisibility(View.GONE);
        tvEmptyRequests.setVisibility(View.VISIBLE);
        btnToggleView.setText("Показать клиентов");
    }

    private void respondToRequest(String requestId, boolean accept) {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage(accept ? "Принятие заявки..." : "Отклонение заявки...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        requestViewModel.respondToRequest(requestId, accept).observe(getViewLifecycleOwner(), success -> {
            progressDialog.dismiss();

            if (success) {
                Toast.makeText(requireContext(),
                        accept ? "Заявка принята, пользователь добавлен в клиенты" : "Заявка отклонена",
                        Toast.LENGTH_SHORT).show();

                // Обновляем данные
                User user = userViewModel.getCurrentUser().getValue();
                if (user != null) {
                    coachViewModel.refreshClients(user.getPublicId());
                    requestViewModel.refreshIncomingRequests(user.getPublicId());
                }

                // Если заявка принята, переключаемся на клиентов
                if (accept) {
                    showClientsView();
                }
            } else {
                Toast.makeText(requireContext(),
                        "Ошибка обработки заявки",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showClientStats(User client) {
        // TODO: Реализовать показ статистики клиента
        // Можно открыть новый фрагмент, диалог или активность
        Toast.makeText(requireContext(),
                "Статистика клиента: " + client.getName() + "\n" +
                        "Email: " + client.getEmail() + "\n" +
                        "Цель: " + getGoalText(client.getGoal()) + "\n" +
                        "Норма калорий: " + client.getDailyCalories() + " ккал",
                Toast.LENGTH_LONG).show();

        // Пример открытия диалога со статистикой:
        showStatsDialog(client);
    }

    private void showStatsDialog(User client) {
        // Создаем простой диалог с информацией
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Статистика: " + client.getName());

        String statsText = "Email: " + client.getEmail() + "\n" +
                "ID: " + client.getPublicId() + "\n" +
                "Цель: " + getGoalText(client.getGoal()) + "\n" +
                "Норма калорий: " + client.getDailyCalories() + " ккал/день\n" +
                "Возраст: " + client.getAge() + " лет\n" +
                "Вес: " + client.getWeight() + " кг\n" +
                "Рост: " + client.getHeight() + " см";

        builder.setMessage(statsText);
        builder.setPositiveButton("Закрыть", (dialog, which) -> dialog.dismiss());

        // Кнопка для просмотра питания клиента
        builder.setNeutralButton("Питание клиента", (dialog, which) -> {
            viewClientMeals(client);
        });

        builder.show();
    }

    private void viewClientMeals(User client) {
        // Открываем фрагмент с питанием клиента
        ClientMealsFragment clientMealsFragment = ClientMealsFragment.newInstance(
                client.getUid(),
                client.getName(),
                client.getEmail()
        );

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, clientMealsFragment)
                .addToBackStack("client_meals")
                .commit();

        // Обновляем заголовок в ActionBar если используется AppCompatActivity
        try {
            if (requireActivity() instanceof AppCompatActivity activity) {
                if (activity.getSupportActionBar() != null) {
                    activity.getSupportActionBar().setTitle("Питание: " + client.getName());
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибку, если ActionBar не доступен
        }
    }

    private String getGoalText(String goal) {
        if (goal == null) return "Не указана";
        switch (goal) {
            case "LOSE": return "Похудение";
            case "MAINTAIN": return "Поддержание";
            case "GAIN": return "Набор массы";
            default: return "Не указана";
        }
    }
}