package com.fitration.ui.coach.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fitration.R;
import com.fitration.models.CoachRequest;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CoachRequestsAdapter extends RecyclerView.Adapter<CoachRequestsAdapter.RequestViewHolder> {

    private List<CoachRequest> requests;
    private OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAcceptRequest(CoachRequest request);
        void onRejectRequest(CoachRequest request);
    }

    public CoachRequestsAdapter() {
        // Пустой конструктор
    }

    public void setRequests(List<CoachRequest> requests) {
        this.requests = requests;
        notifyDataSetChanged();
    }

    public void setOnRequestActionListener(OnRequestActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coach_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        CoachRequest request = requests.get(position);
        holder.bind(request);

        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAcceptRequest(request);
            }
        });

        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRejectRequest(request);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requests != null ? requests.size() : 0;
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserName;
        private final TextView tvUserEmail;
        private final TextView tvRequestDate;
        private final TextView tvRequestStatus;
        private final TextView tvMessage;
        private final Button btnAccept;
        private final Button btnReject;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserEmail = itemView.findViewById(R.id.tv_user_email);
            tvRequestDate = itemView.findViewById(R.id.tv_request_date);
            tvRequestStatus = itemView.findViewById(R.id.tv_request_status);
            tvMessage = itemView.findViewById(R.id.tv_message);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);

            // Убедимся, что кнопка cancel удалена из кода, если её нет в layout
            // btnCancel = itemView.findViewById(R.id.btn_cancel); // Убрать если нет в XML
        }

        public void bind(CoachRequest request) {
            // Для тренера показываем информацию о пользователе
            tvUserName.setText(request.getUserName() != null ? request.getUserName() : "Пользователь");
            tvUserEmail.setText(request.getUserEmail() != null ? request.getUserEmail() : "Нет email");
            tvRequestStatus.setText(getStatusText(request.getStatus()));

            // Проверяем message на null
            String message = request.getMessage();
            if (message != null && !message.isEmpty()) {
                tvMessage.setText(message);
                tvMessage.setVisibility(View.VISIBLE);
            } else {
                tvMessage.setVisibility(View.GONE);
            }

            if (request.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                tvRequestDate.setText(sdf.format(request.getCreatedAt()));
            } else {
                tvRequestDate.setText("Дата не указана");
            }

            // Устанавливаем background для статуса
            tvRequestStatus.setBackgroundResource(getStatusBackground(request.getStatus()));

            // Настройка видимости кнопок
            if ("PENDING".equals(request.getStatus())) {
                btnAccept.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
            } else {
                btnAccept.setVisibility(View.GONE);
                btnReject.setVisibility(View.GONE);
            }
        }

        private String getStatusText(String status) {
            if (status == null) {
                return "Неизвестно";
            }
            switch (status) {
                case "PENDING": return "Ожидает";
                case "ACCEPTED": return "Принята";
                case "REJECTED": return "Отклонена";
                case "CANCELLED": return "Отменена";
                default: return status;
            }
        }

        private int getStatusBackground(String status) {
            if (status == null) {
                return R.drawable.status_background_pending;
            }
            return switch (status) {
                case "ACCEPTED" -> R.drawable.status_background_accepted;
                case "REJECTED" -> R.drawable.status_background_rejected;
                case "CANCELLED" -> R.drawable.status_background_cancelled;
                default -> R.drawable.status_background_pending;
            };
        }
    }
}