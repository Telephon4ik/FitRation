package com.fitration.ui.coach.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitration.R;
import com.fitration.models.Meal;
import com.fitration.models.User;
import com.fitration.ui.user.adapters.MealAdapter;
import com.fitration.viewmodels.CoachViewModel;
import com.fitration.viewmodels.MealViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClientMealsFragment extends Fragment {

    private CoachViewModel coachViewModel;
    private MealViewModel mealViewModel;

    private String clientId;
    private String clientName;
    private String clientEmail;

    private RecyclerView rvClientMeals;
    private MealAdapter mealAdapter;
    private ProgressBar loadingProgress;
    private TextView tvEmptyState;
    private TextView tvClientName;
    private TextView tvSelectedDate;
    private Button btnPrevDay;
    private Button btnNextDay;
    private Button btnToday;

    private Date currentDate;
    private SimpleDateFormat dateFormat;

    public static ClientMealsFragment newInstance(User client) {
        ClientMealsFragment fragment = new ClientMealsFragment();
        Bundle args = new Bundle();
        // Передаем данные клиента по отдельности
        args.putString("clientId", client.getUid());
        args.putString("clientName", client.getName());
        args.putString("clientEmail", client.getEmail());
        args.putString("clientPublicId", client.getPublicId());
        fragment.setArguments(args);
        return fragment;
    }

    // Альтернативный конструктор для передачи данных напрямую
    public static ClientMealsFragment newInstance(String clientId, String clientName, String clientEmail) {
        ClientMealsFragment fragment = new ClientMealsFragment();
        Bundle args = new Bundle();
        args.putString("clientId", clientId);
        args.putString("clientName", clientName);
        args.putString("clientEmail", clientEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            clientId = getArguments().getString("clientId");
            clientName = getArguments().getString("clientName");
            clientEmail = getArguments().getString("clientEmail");
        }

        dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru", "RU"));
        currentDate = new Date();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_meals, container, false);

        initViews(view);
        setupRecyclerView();
        setupViewModels();
        setupObservers();
        setupListeners();

        loadClientMeals();

        return view;
    }

    private void initViews(View view) {
        rvClientMeals = view.findViewById(R.id.rv_client_meals);
        loadingProgress = view.findViewById(R.id.loading_progress);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        tvClientName = view.findViewById(R.id.tv_client_name);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        btnPrevDay = view.findViewById(R.id.btn_prev_day);
        btnNextDay = view.findViewById(R.id.btn_next_day);
        btnToday = view.findViewById(R.id.btn_today);

        if (clientName != null) {
            tvClientName.setText("Питание: " + clientName);
        }

        updateDateDisplay();
    }

    private void setupRecyclerView() {
        mealAdapter = new MealAdapter();
        mealAdapter.setIsForUser(false); // false = для тренера (скрываем чекбокс "Съедено")
        mealAdapter.setOnMealClickListener(new MealAdapter.OnMealClickListener() {
            @Override
            public void onMealClick(Meal meal) {
                showMealDetails(meal);
            }

            @Override
            public void onMarkAsEaten(Meal meal, boolean isEaten) {
                // Для тренера эта функция не используется
            }
        });
        rvClientMeals.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvClientMeals.setAdapter(mealAdapter);
    }

    private void setupViewModels() {
        coachViewModel = new ViewModelProvider(requireActivity()).get(CoachViewModel.class);
        mealViewModel = new ViewModelProvider(requireActivity()).get(MealViewModel.class);
    }

    private void setupObservers() {
        // Наблюдаем за блюдами клиента
        mealViewModel.getTodayMeals().observe(getViewLifecycleOwner(), this::updateMealsList);
    }

    private void setupListeners() {
        btnPrevDay.setOnClickListener(v -> {
            changeDate(-1);
        });

        btnNextDay.setOnClickListener(v -> {
            changeDate(1);
        });

        btnToday.setOnClickListener(v -> {
            currentDate = new Date();
            updateDateDisplay();
            loadClientMeals();
        });
    }

    private void changeDate(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        currentDate = calendar.getTime();
        updateDateDisplay();
        loadClientMeals();
    }

    private void updateDateDisplay() {
        tvSelectedDate.setText(dateFormat.format(currentDate));

        // Проверяем, сегодня ли выбранная дата
        Calendar today = Calendar.getInstance();
        Calendar selected = Calendar.getInstance();
        selected.setTime(currentDate);

        boolean isToday = today.get(Calendar.YEAR) == selected.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == selected.get(Calendar.DAY_OF_YEAR);

        btnToday.setEnabled(!isToday);
    }

    private void loadClientMeals() {
        if (clientId == null) {
            return;
        }

        loadingProgress.setVisibility(View.VISIBLE);
        mealViewModel.refreshTodayMeals(clientId);
    }

    private void updateMealsList(List<Meal> meals) {
        loadingProgress.setVisibility(View.GONE);

        if (meals == null || meals.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("На " + dateFormat.format(currentDate) + " питание не запланировано");
            rvClientMeals.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvClientMeals.setVisibility(View.VISIBLE);
            mealAdapter.setMeals(meals);
        }
    }

    private void showMealDetails(Meal meal) {
        // Диалог с деталями блюда и возможностью добавить комментарий
        showMealDetailsDialog(meal);
    }

    private void showMealDetailsDialog(Meal meal) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle(meal.getName());

        String mealInfo = "Тип: " + getMealTypeText(meal.getMealType()) + "\n" +
                "Описание: " + (meal.getDescription() != null ? meal.getDescription() : "нет") + "\n" +
                "Калории: " + (meal.getNutrition() != null ? meal.getNutrition().getCalories() : 0) + " ккал\n" +
                "Белки: " + (meal.getNutrition() != null ? String.format("%.1f", meal.getNutrition().getProtein()) : "0") + " г\n" +
                "Углеводы: " + (meal.getNutrition() != null ? String.format("%.1f", meal.getNutrition().getCarbs()) : "0") + " г\n" +
                "Жиры: " + (meal.getNutrition() != null ? String.format("%.1f", meal.getNutrition().getFat()) : "0") + " г\n" +
                "Статус: " + (meal.getEaten() != null && meal.getEaten() ? "Съедено" : "Не съедено") + "\n" +
                "Комментарий тренера: " + (meal.getCoachComment() != null ? meal.getCoachComment() : "нет");

        builder.setMessage(mealInfo);

        // Кнопка для добавления/редактирования комментария
        builder.setPositiveButton("Добавить комментарий", (dialog, which) -> {
            showAddCommentDialog(meal);
        });

        builder.setNegativeButton("Закрыть", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void showAddCommentDialog(Meal meal) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Комментарий к блюду");

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_comment, null);
        android.widget.EditText etComment = view.findViewById(R.id.et_comment);

        // Заполняем текущий комментарий если есть
        if (meal.getCoachComment() != null) {
            etComment.setText(meal.getCoachComment());
        }

        builder.setView(view);

        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String comment = etComment.getText().toString().trim();
            if (!comment.isEmpty()) {
                addCommentToMeal(meal, comment);
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void addCommentToMeal(Meal meal, String comment) {
        if (meal.getId() == null) {
            Toast.makeText(requireContext(), "Ошибка: ID блюда не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        meal.setCoachComment(comment);

        coachViewModel.updateMealWithComment(meal.getId(), comment).observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), "Комментарий сохранен", Toast.LENGTH_SHORT).show();
                // Обновляем список блюд
                loadClientMeals();
            } else {
                Toast.makeText(requireContext(), "Ошибка сохранения комментария", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getMealTypeText(String mealType) {
        if (mealType == null) return "Приём пищи";
        switch (mealType) {
            case "breakfast": return "Завтрак";
            case "lunch": return "Обед";
            case "dinner": return "Ужин";
            case "snack": return "Перекус";
            default: return "Приём пищи";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Очищаем observer чтобы избежать утечек памяти
        mealViewModel.getTodayMeals().removeObservers(getViewLifecycleOwner());
    }
}