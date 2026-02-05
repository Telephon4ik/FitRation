package com.fitration.ui.coach.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fitration.R;
import com.fitration.models.User;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {

    private List<User> clients;
    private OnClientClickListener listener;

    public interface OnClientClickListener {
        void onClientClick(User client);
        void onViewStatsClick(User client); // Добавлен метод для статистики
    }

    public ClientAdapter() {
        // Конструктор по умолчанию
    }

    public void setClients(List<User> clients) {
        this.clients = clients;
        notifyDataSetChanged();
    }

    public void setOnClientClickListener(OnClientClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        User client = clients.get(position);
        holder.bind(client);

        // Обработчик клика на всю карточку
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClientClick(client);
            }
        });

        // Обработчик клика на кнопку статистики
        holder.btnViewStats.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewStatsClick(client);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clients != null ? clients.size() : 0;
    }

    static class ClientViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvClientName;
        private final Button btnViewStats;
        private final TextView tvClientEmail;
        private final TextView tvClientGoal;
        private final TextView tvClientCalories;
        private final TextView tvClientSince;

        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            btnViewStats = itemView.findViewById(R.id.btn_view_stats);
            tvClientName = itemView.findViewById(R.id.tv_client_name);
            tvClientEmail = itemView.findViewById(R.id.tv_client_email);
            tvClientGoal = itemView.findViewById(R.id.tv_client_goal);
            tvClientCalories = itemView.findViewById(R.id.tv_client_calories);
            tvClientSince = itemView.findViewById(R.id.tv_client_since);
        }

        public void bind(User client) {
            tvClientName.setText(client.getName());
            tvClientEmail.setText(client.getEmail());

            String goalText = getGoalText(client.getGoal());
            tvClientGoal.setText("Цель: " + goalText);

            // Убедимся, что кнопка видима
            btnViewStats.setVisibility(View.VISIBLE);
            btnViewStats.setText("Статистика");

            tvClientCalories.setText("Норма: " + client.getDailyCalories() + " ккал/день");

            if (client.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                tvClientSince.setText("С: " + sdf.format(client.getCreatedAt()));
            } else {
                tvClientSince.setText("С: неизвестно");
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
}